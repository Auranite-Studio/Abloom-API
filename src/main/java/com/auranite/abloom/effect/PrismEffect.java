package com.auranite.abloom.effect;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.AbloomModAttachments;
import com.auranite.abloom.AbloomModEffects;
import com.auranite.abloom.ElementDamageHandler;
import com.auranite.abloom.ElementType;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Effect that converts incoming prismatic damage to elemental damage of the type that caused resonance.
 * When a target with Prism effect takes prismatic damage, the damage is converted to the type of the active resonance.
 */
public class PrismEffect extends MobEffect {

    public PrismEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFFFFF);
    }

    /**
     * Gets the damage type that should be used for prismatic damage conversion.
     * Checks both Prism effect data and active resonance effects.
     */
    public static ElementType getResonanceTypeForPrism(LivingEntity entity) {
        // Check Prism effect data first - this stores the type even after resonance effect ends
        ElementType storedType = entity.getData(AbloomModAttachments.PRISM_RESONANCE_TYPE.get());
        if (storedType != null) {
            return storedType;
        }

        // Check which resonance effect is currently active
        if (entity.hasEffect(AbloomModEffects.BURN)) return ElementType.FIRE;
        if (entity.hasEffect(AbloomModEffects.RUPTURE)) return ElementType.PHYSICAL;
        if (entity.hasEffect(AbloomModEffects.WINDSWEPT)) return ElementType.WIND;
        if (entity.hasEffect(AbloomModEffects.WETNESS)) return ElementType.WATER;
        if (entity.hasEffect(AbloomModEffects.STUN)) return ElementType.EARTH;
        if (entity.hasEffect(AbloomModEffects.FREEZE)) return ElementType.ICE;
        if (entity.hasEffect(AbloomModEffects.SHOCK)) return ElementType.ELECTRIC;
        if (entity.hasEffect(AbloomModEffects.OVERLOAD)) return ElementType.ENERGY;
        if (entity.hasEffect(AbloomModEffects.BLOOM)) return ElementType.NATURAL;
        if (entity.hasEffect(AbloomModEffects.BREAK)) return ElementType.QUANTUM;
        if (entity.hasEffect(AbloomModEffects.CORRUPTION)) return ElementType.ETHER;
        if (entity.hasEffect(AbloomModEffects.DISPERSION)) return ElementType.LIGHT;
        if (entity.hasEffect(AbloomModEffects.ECLIPSE)) return ElementType.SHADOW;

        return null;
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        if (source == null) return;

        // Check if source is prismatic damage
        String damageTypeId = source.type().msgId();
        if (damageTypeId == null || !damageTypeId.contains("prismatic")) return;

        // Check if target has Prism effect
        if (!entity.hasEffect(AbloomModEffects.PRISM)) return;

        // Get the active resonance type
        ElementType resonanceType = getResonanceTypeForPrism(entity);
        if (resonanceType != null) {
            // Update Prism effect duration and store the resonance type
            // This ensures Prism effect is extended when new resonance appears
            entity.addEffect(new MobEffectInstance(AbloomModEffects.PRISM, 400, 0, false, true));
            entity.setData(AbloomModAttachments.PRISM_RESONANCE_TYPE.get(), resonanceType);

            // Spawn status text indicating resonance conversion
            ElementDamageHandler.spawnStatusText(entity, Component.translatable("elemental.tooltip.conversion"), -1);

            // Convert prismatic damage to elemental damage of the resonance type
            convertPrismaticDamage(entity, source, resonanceType, event.getNewDamage());
        }
    }

    /**
     * Converts prismatic damage to elemental damage.
     */
    private static void convertPrismaticDamage(LivingEntity target, DamageSource originalSource, ElementType elementType, float damageAmount) {
        if (target.level().isClientSide) return;

        ServerLevel level = (ServerLevel) target.level();

        // Create a new damage source with the elemental type
        var damageTypeRegistry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, elementType.getDamageTypeId());

        // In NeoForge, getHolder returns Optional<Holder<T>>
        var damageTypeHolder = damageTypeRegistry.getHolder(rl);

        if (damageTypeHolder.isPresent()) {
            // Use entity() and getDirectEntity() for NeoForge DamageSource
            DamageSource elementalSource = new DamageSource(damageTypeHolder.get(), originalSource.getEntity(), originalSource.getDirectEntity());

            // Apply the damage with the new source type
            // The ElementDamageHandler will process this as elemental damage
            target.hurt(elementalSource, damageAmount);
        }
    }

    /**
     * Clears the stored resonance type when effect is removed.
     */
    public void onRemove(LivingEntity entity, int amplifier) {
        // Clear the stored resonance type when effect is removed
        entity.setData(AbloomModAttachments.PRISM_RESONANCE_TYPE.get(), null);
    }

    @Override
    public void onEffectAdded(LivingEntity entity, int amplifier) {
        ElementDamageHandler.spawnStatusText(entity, Component.translatable("elemental.tooltip.conversion"), -1);
    }
}
