package com.auranite.abloom.handler;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.init.AbloomModAttributes;
import com.auranite.abloom.init.AbloomModEffects;
import com.auranite.abloom.util.ElementType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;

/**
 * Handles critical hit calculations for damage events.
 * Combines entity attributes and weapon crit values additively.
 */
public class CriticalHitHandler {

    private CriticalHitHandler() {
        // Utility class
    }

    /**
     * Applies critical hit logic to the damage.
     * Combines entity attributes and weapon crit values additively.
     *
     * @param attacker the attacking entity (can be null)
     * @param baseDamage the damage before crit application
     * @return CritResult with modified damage and crit flag
     */
    public static CritResult applyCriticalHit(LivingEntity attacker, float baseDamage) {
        if (attacker == null) {
            return new CritResult(baseDamage, false, false);
        }

        // Get crit chance from entity attributes
        var critChanceKey = AbloomModAttributes.CRIT_CHANCE.getKey();
        AttributeInstance critChanceAttr = attacker.getAttribute(BuiltInRegistries.ATTRIBUTE.getHolderOrThrow(critChanceKey));
        double entityCritChanceVal = critChanceAttr != null ? critChanceAttr.getValue() : 0.0;
        double entityCritChance = entityCritChanceVal;

        // Get crit chance from weapon
        ItemStack weapon = attacker.getMainHandItem();
        double weaponCritChance = com.auranite.abloom.registries.ElementalWeaponRegistry.getCritChance(weapon)
                + com.auranite.abloom.component.ElementalWeaponComponent.getCritChance(weapon);

        // Get crit damage from entity attributes
        var critDamageKey = AbloomModAttributes.CRIT_DMG.getKey();
        AttributeInstance critDamageAttr = attacker.getAttribute(BuiltInRegistries.ATTRIBUTE.getHolderOrThrow(critDamageKey));
        double entityCritDamageVal = critDamageAttr != null ? critDamageAttr.getValue() : 0.0;
        double entityCritDamage = entityCritDamageVal;

        // Get crit damage from weapon
        double weaponCritDamage = com.auranite.abloom.registries.ElementalWeaponRegistry.getCritDamage(weapon)
                + com.auranite.abloom.component.ElementalWeaponComponent.getCritDamage(weapon);

        // Sum additively, clamp final totals to [0, max] so debuffs cannot fully override weapon bonuses
        // Crit chance can now go up to 2.0 (200%) to enable multi-crit
        double totalCritChance = Math.max(0.0, Math.min(2.0, entityCritChance + weaponCritChance));
        double totalCritDamage = Math.max(0.0, Math.min(10.0, entityCritDamage + weaponCritDamage));

        // Random check for normal crit (always triggers if chance >= 1.0)
        if (attacker.level().random.nextFloat() < totalCritChance) {
            float critDamage = baseDamage * (1.0f + (float) totalCritDamage);
            boolean isMultiCrit = totalCritChance > 1.0f && attacker.level().random.nextFloat() < (totalCritChance - 1.0f);
            if (isMultiCrit) {
                critDamage *= 2.0f;
            }
            return new CritResult(critDamage, true, isMultiCrit);
        }

        return new CritResult(baseDamage, false, false);
    }
}
