package com.auranite.abloom.handler;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.datapack.ElementalProjectileRegistry;
import com.auranite.abloom.datapack.ElementalWeaponRegistry;
import com.auranite.abloom.component.ElementalWeaponComponent;
import com.auranite.abloom.init.AbloomModAttributes;
import com.auranite.abloom.init.AbloomModEffects;
import com.auranite.abloom.util.ElementResistanceManager;
import com.auranite.abloom.util.ElementType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Handles damage calculation modifiers including armor resistance, critical hits, and elemental multipliers.
 */
public class DamageCalculator {
    
    /**
     * Applies all damage modifiers and returns the final damage value.
     */
    public static float calculateFinalDamage(LivingEntity target, LivingEntity attacker, DamageSource source,
                                              float baseDamage, ElementType type, boolean erosionActive) {
        float damage = applyModifiers(target, attacker, baseDamage, type);
        
        // Apply armor resistance
        float armorResistanceBonus = getArmorResistanceBonus(target, type);
        damage = applyArmorResistance(damage, armorResistanceBonus);
        
        // Apply elemental resistance
        damage = ElementResistanceManager.calculateReducedDamage(target, type, damage);
        
        return damage;
    }
    
    /**
     * Applies attacker and target effect modifiers to damage.
     */
    private static float applyModifiers(LivingEntity target, LivingEntity attacker, 
                                         float baseDamage, ElementType type) {
        float damageMultiplier = 1.0f;
        
        // Attacker modifiers
        if (attacker != null && attacker.hasEffect(AbloomModEffects.SHOCK)) {
            int amplifier = attacker.getEffect(AbloomModEffects.SHOCK).getAmplifier();
            float reduction = 1.0f - ((amplifier + 1) * 0.20f);
            damageMultiplier *= Math.max(0.1f, reduction);
        }
        
        // Target modifiers
        if (target.hasEffect(AbloomModEffects.OVERLOAD)) {
            int amplifier = target.getEffect(AbloomModEffects.OVERLOAD).getAmplifier();
            damageMultiplier *= 1.0f + (amplifier + 1) * 0.20f;
        }
        if (target.hasEffect(AbloomModEffects.BLOOM)) {
            int amplifier = target.getEffect(AbloomModEffects.BLOOM).getAmplifier();
            damageMultiplier *= 1.0f + (amplifier + 1) * 0.20f;
        }
        if (target.hasEffect(AbloomModEffects.DISPERSION)) {
            float dispersionBonus = ThresholdEffectHandler.getDispersionBonus(type);
            if (AbloomMod.LOGGER.isDebugEnabled()) {
                AbloomMod.LOGGER.debug("Applying dispersion bonus of {} for type {}", dispersionBonus, type);
            }
            damageMultiplier *= 1.0f + dispersionBonus;
        }
        
        return baseDamage * damageMultiplier;
    }
    
    /**
     * Calculates accumulation multiplier based on weapon, projectile, and target effects.
     */
    public static float calculateAccumulationMultiplier(LivingEntity attacker, LivingEntity target,
                                                         DamageSource source, float stageAccumMultiplier) {
        float effectiveAccumMultiplier = 0.0f;
        
        // Check projectile accumulation
        if (source.getDirectEntity() != null) {
            Optional<Float> projectileAccum = ElementalProjectileRegistry.getAccumulationMultiplierForEntity(source.getDirectEntity());
            if (projectileAccum.isPresent()) {
                effectiveAccumMultiplier = projectileAccum.get();
            }
        }
        
        // Check weapon accumulation
        if (effectiveAccumMultiplier == 0.0f && source.getEntity() instanceof LivingEntity attackerEntity) {
            ItemStack weapon = attackerEntity.getMainHandItem();
            if (stageAccumMultiplier > 1.0f) {
                effectiveAccumMultiplier = stageAccumMultiplier;
            } else if (ElementalWeaponComponent.hasElement(weapon)) {
                effectiveAccumMultiplier = ElementalWeaponComponent.getAccumMultiplier(weapon);
            } else if (ElementalWeaponRegistry.getWeaponData(weapon) != null) {
                effectiveAccumMultiplier = ElementalWeaponRegistry.getAccumulationMultiplier(weapon);
            }
        }
        
        // Apply target effect bonuses
        if (target.hasEffect(AbloomModEffects.BLOOM)) {
            int amplifier = target.getEffect(AbloomModEffects.BLOOM).getAmplifier();
            effectiveAccumMultiplier *= 1.20f * (amplifier + 1);
        }
        if (target.hasEffect(AbloomModEffects.WETNESS)) {
            int amplifier = target.getEffect(AbloomModEffects.WETNESS).getAmplifier();
            effectiveAccumMultiplier *= 1.0f + (amplifier + 1) * 0.5f;
        }
        
        // Apply Resonance Accumulation Buildup attribute
        if (attacker != null) {
            ResourceKey<Attribute> buildupKey = AbloomModAttributes.RESONANCE_ACCUMULATION_BUILDUP.getKey();
            AttributeInstance buildupAttr = attacker.getAttribute(BuiltInRegistries.ATTRIBUTE.getHolderOrThrow(buildupKey));
            if (buildupAttr != null) {
                double buildupValue = buildupAttr.getValue();
                effectiveAccumMultiplier *= (1.0f + (float) buildupValue);
            }
        }
        
        return effectiveAccumMultiplier;
    }
    
    /**
     * Gets armor resistance bonus for an entity and element type.
     */
    private static float getArmorResistanceBonus(LivingEntity entity, ElementType type) {
        // Placeholder - implement based on your armor system
        return 0.0f;
    }
    
    /**
     * Applies armor resistance to damage.
     */
    private static float applyArmorResistance(float damage, float resistanceBonus) {
        return damage * (1.0f - resistanceBonus);
    }
    
    /**
     * Record containing crit calculation result.
     */
    public record CritResult(float damage, boolean isCrit, boolean isMultiCrit) {}
}
