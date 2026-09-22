package com.auranite.abloom.handler;

import com.auranite.abloom.init.AbloomModEffects;
import com.auranite.abloom.util.ElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

/**
 * Handles threshold effect application when resonance accumulation reaches 100 points.
 * Each element type has a unique effect and damage multiplier.
 */
public class ThresholdEffectHandler {

    private ThresholdEffectHandler() {
        // Utility class
    }

    /**
     * Applies the threshold effect for a specific element type.
     *
     * @param target the target entity
     * @param type the element type
     * @param originalDamage the damage before threshold bonus
     * @return the modified damage after applying threshold bonus
     */
    public static float applyThresholdEffect(LivingEntity target, ElementType type, float originalDamage) {
        return applyThresholdEffect(target, type, originalDamage, false);
    }

    /**
     * Applies the threshold effect for a specific element type.
     * Erosion triggers apply effects at half duration.
     *
     * @param target the target entity
     * @param type the element type
     * @param originalDamage the damage before threshold bonus
     * @param isErosionTrigger true if this is an erosion trigger (half duration)
     * @return the modified damage after applying threshold bonus
     */
    public static float applyThresholdEffect(LivingEntity target, ElementType type, float originalDamage, boolean isErosionTrigger) {
        // Erosion triggers apply effects at half duration
        int durationMultiplier = isErosionTrigger ? 10 : 20; // half of standard (20 ticks = 1 second)

        return switch (type) {
            case FIRE -> {
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(AbloomModEffects.BURN, 12 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.overheating"), 0xFF5500);
                yield originalDamage * 1.25f;
            }
            case PHYSICAL -> {
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(AbloomModEffects.RUPTURE, 12 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.rupture"), 0xC0C0C0);
                yield originalDamage * 2.0f;
            }
            case WIND -> {
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(AbloomModEffects.WINDSWEPT, 15 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.wind_whirlwind"), 0x00FFFF);
                yield originalDamage * 1.5f;
            }
            case WATER -> {
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(AbloomModEffects.WETNESS, 15 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.water_flood"), 0x0080FF);
                yield originalDamage * 1.5f;
            }
            case EARTH -> {
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(AbloomModEffects.STUN, 5 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.earth_petrify"), 0x8B4513);
                yield originalDamage * 1.5f;
            }
            case ICE -> {
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(AbloomModEffects.FREEZE, 14 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.ice_freeze"), 0x00BFFF);
                yield originalDamage * 1.25f;
            }
            case ELECTRIC -> {
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(AbloomModEffects.SHOCK, 14 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.electric_shock"), 0xFF19FF);
                yield originalDamage * 1.5f;
            }
            case ENERGY -> {
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(AbloomModEffects.OVERLOAD, 14 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.energy_overload"), 0xFFFF00);
                yield originalDamage * 1.5f;
            }
            case NATURAL -> {
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(AbloomModEffects.BLOOM, 12 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.natural_bloom"), 0x32CD32);
                yield originalDamage * 1.25f;
            }
            case QUANTUM -> {
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(AbloomModEffects.BREAK, 8 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.quantum_flux"), 0xFF00FF);
                yield originalDamage * 1.25f;
            }
            case ETHER -> {
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(AbloomModEffects.CORRUPTION, 12 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.ether_resonance"), 0x24B3A7);
                yield originalDamage * 1.25f;
            }
            case LIGHT -> {
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(AbloomModEffects.DISPERSION, 14 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.light_dispersion"), 0xFFFFE0);
                yield originalDamage * 1.5f;
            }
            case SHADOW -> {
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(AbloomModEffects.ECLIPSE, 14 * durationMultiplier, 0, false, true));
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.shadow_eclipse"), 0x4B0082);
                yield originalDamage * 1.5f;
            }
            default -> originalDamage;
        };
    }
}
