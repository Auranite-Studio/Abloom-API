package com.auranite.abloom.handler;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.init.AbloomModEffects;
import com.auranite.abloom.util.ElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * Handles threshold effect application for different element types.
 * Contains the logic for applying status effects and damage multipliers when accumulation threshold is reached.
 */
public class ThresholdEffectHandler {
    
    private static final int STANDARD_DURATION_MULTIPLIER = 20;
    private static final int EROSION_DURATION_MULTIPLIER = 10; // half of standard

    /**
     * Applies threshold effect based on element type.
     * @param target the entity that reached threshold
     * @param type the element type
     * @param originalDamage the original damage value
     * @param isErosionTrigger whether this is triggered by erosion (Windswept)
     * @return modified damage after applying threshold effect
     */
    public static float applyThresholdEffect(LivingEntity target, ElementType type, 
                                              float originalDamage, boolean isErosionTrigger) {
        int durationMultiplier = isErosionTrigger ? EROSION_DURATION_MULTIPLIER : STANDARD_DURATION_MULTIPLIER;

        return switch (type) {
            case FIRE -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.BURN, 12 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.overheating"), 0xFF5500);
                yield originalDamage * 1.25f;
            }
            case PHYSICAL -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.RUPTURE, 12 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.rupture"), 0xC0C0C0);
                yield originalDamage * 2.0f;
            }
            case WIND -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.WINDSWEPT, 15 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.wind_whirlwind"), 0x00FFFF);
                yield originalDamage * 1.5f;
            }
            case WATER -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.WETNESS, 15 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.water_flood"), 0x0080FF);
                yield originalDamage * 1.5f;
            }
            case EARTH -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.STUN, 5 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.earth_petrify"), 0x8B4513);
                yield originalDamage * 1.5f;
            }
            case ICE -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.FREEZE, 14 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.ice_freeze"), 0x00BFFF);
                yield originalDamage * 1.25f;
            }
            case ELECTRIC -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.SHOCK, 14 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.electric_shock"), 0xFF19FF);
                yield originalDamage * 1.5f;
            }
            case ENERGY -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.OVERLOAD, 14 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.energy_overload"), 0xFFFF00);
                yield originalDamage * 1.5f;
            }
            case NATURAL -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.BLOOM, 12 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.natural_bloom"), 0x32CD32);
                yield originalDamage * 1.25f;
            }
            case QUANTUM -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.BREAK, 8 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.quantum_flux"), 0xFF00FF);
                yield originalDamage * 1.25f;
            }
            case ETHER -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.CORRUPTION, 12 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.ether_resonance"), 0x24B3A7);
                yield originalDamage * 1.25f;
            }
            case LIGHT -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.DISPERSION, 14 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.light_dispersion"), 0xFFFFE0);
                yield originalDamage * 1.5f;
            }
            case SHADOW -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.ECLIPSE, 14 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.shadow_eclipse"), 0x4B0082);
                yield originalDamage * 1.5f;
            }
            default -> originalDamage;
        };
    }

    /**
     * Applies threshold effect without erosion trigger.
     */
    public static float applyThresholdEffect(LivingEntity target, ElementType type, float originalDamage) {
        return applyThresholdEffect(target, type, originalDamage, false);
    }

    /**
     * Gets the dispersion bonus multiplier for a specific element type.
     */
    public static float getDispersionBonus(ElementType type) {
        return switch (type) {
            case PHYSICAL -> 0.15f;
            case FIRE -> 0.15f;
            case WIND -> 0.30f;
            case WATER -> 0.30f;
            case EARTH -> 0.15f;
            case ICE -> 0.15f;
            case ELECTRIC -> 0.30f;
            case ENERGY -> 0.15f;
            case NATURAL -> 0.15f;
            case QUANTUM -> 0.15f;
            case ETHER -> 0.15f;
            case LIGHT -> 0.30f;
            case SHADOW -> 0.20f;
            case PRISMATIC -> 0.30f;
            default -> 0.00f;
        };
    }
}
