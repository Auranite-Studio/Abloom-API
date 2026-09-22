package com.auranite.abloom.handler;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.init.AbloomModAttachments;
import com.auranite.abloom.init.AbloomModEffects;
import com.auranite.abloom.util.ElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

/**
 * Handles Prism damage conversion and resonance type detection.
 */
public class PrismHandler {

    private PrismHandler() {
        // Utility class
    }

    /**
     * Gets the stored prism conversion type from the target's attachment.
     * This is the element type that prism damage should be converted to.
     */
    public static ElementType getConvertedPrismType(LivingEntity target) {
        return AbloomModAttachments.getPrismConversionType(target);
    }

    /**
     * Gets the active resonance effect type on the target.
     * Resonance effects are: BURN, FREEZE, SHOCK, BLOOM, OVERLOAD, WETNESS, STUN, RUPTURE, BREAK, WINDSWEPT, CORRUPTION, DISPERSION, ECLIPSE
     */
    public static ElementType getActiveResonanceType(LivingEntity target) {
        if (target.hasEffect(AbloomModEffects.BURN)) return ElementType.FIRE;
        if (target.hasEffect(AbloomModEffects.FREEZE)) return ElementType.ICE;
        if (target.hasEffect(AbloomModEffects.SHOCK)) return ElementType.ELECTRIC;
        if (target.hasEffect(AbloomModEffects.BLOOM)) return ElementType.NATURAL;
        if (target.hasEffect(AbloomModEffects.OVERLOAD)) return ElementType.ENERGY;
        if (target.hasEffect(AbloomModEffects.WETNESS)) return ElementType.WATER;
        if (target.hasEffect(AbloomModEffects.STUN)) return ElementType.EARTH;
        if (target.hasEffect(AbloomModEffects.RUPTURE)) return ElementType.PHYSICAL;
        if (target.hasEffect(AbloomModEffects.BREAK)) return ElementType.QUANTUM;
        if (target.hasEffect(AbloomModEffects.WINDSWEPT)) return ElementType.WIND;
        if (target.hasEffect(AbloomModEffects.CORRUPTION)) return ElementType.ETHER;
        if (target.hasEffect(AbloomModEffects.DISPERSION)) return ElementType.LIGHT;
        if (target.hasEffect(AbloomModEffects.ECLIPSE)) return ElementType.SHADOW;
        return null;
    }

    /**
     * Checks if a target has an active prism effect.
     */
    public static boolean hasPrismEffect(LivingEntity target) {
        return target.hasEffect(AbloomModEffects.PRISM);
    }

    /**
     * Applies or extends the prism effect on a target.
     */
    public static void applyPrismEffect(LivingEntity target, int durationTicks) {
        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                AbloomModEffects.PRISM, durationTicks, 0, false, true));
    }

    /**
     * Sets the prism conversion type for a target.
     */
    public static void setPrismConversionType(LivingEntity target, ElementType type) {
        AbloomModAttachments.setPrismConversionType(target, type);
    }

    /**
     * Handles prism damage conversion logic.
     * Returns the converted element type, or the original type if no conversion applies.
     */
    public static ElementType handlePrismConversion(LivingEntity target, ElementType originalType) {
        if (originalType != ElementType.PRISMATIC) {
            return originalType;
        }

        if (hasPrismEffect(target)) {
            // PRISM is active: check if a new resonance has appeared
            ElementType storedType = getConvertedPrismType(target);
            ElementType currentResonance = getActiveResonanceType(target);
            
            if (currentResonance != null && currentResonance != storedType) {
                // New resonance appeared — switch conversion type and extend PRISM
                setPrismConversionType(target, currentResonance);
                removePrismEffect(target);
                applyPrismEffect(target, 40 * 20);
                
                if (AbloomMod.LOGGER.isDebugEnabled()) {
                    AbloomMod.LOGGER.debug("Prism conversion switched from {} to {} (new resonance), PRISM extended", 
                            storedType, currentResonance);
                }
                
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.conversion"), 0xFFFFFF);
                return currentResonance;
            } else if (currentResonance != null) {
                // Same resonance as stored — use it as-is
                return currentResonance;
            } else {
                // No active resonance — use stored type from attachment
                ElementType resultType = storedType;
                if (resultType == null) {
                    resultType = ElementType.PRISMATIC; // Fallback
                }
                return resultType;
            }
        } else {
            // No Prism effect active — activate it from current resonance
            ElementType resonanceType = getActiveResonanceType(target);
            if (resonanceType != null && resonanceType != ElementType.PRISMATIC) {
                applyPrismEffect(target, 40 * 20);
                setPrismConversionType(target, resonanceType);
                ElementDamageHandler.spawnStatusText(target, Component.translatable("elemental.tooltip.conversion"), 0xFFFFFF);
                return resonanceType;
            }
            // Prism damage does NOT accumulate resonance points
            return ElementType.PRISMATIC;
        }
    }

    /**
     * Removes the prism effect from a target.
     */
    public static void removePrismEffect(LivingEntity target) {
        target.removeEffect(AbloomModEffects.PRISM);
    }
}
