package com.auranite.abloom.handler;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.init.AbloomModAttachments;
import com.auranite.abloom.init.AbloomModEffects;
import com.auranite.abloom.util.ElementType;
import net.minecraft.world.entity.LivingEntity;

/**
 * Handles Prism damage conversion and resonance effect tracking.
 * Manages the conversion of Prismatic damage to specific element types based on active effects.
 */
public class PrismConversionHandler {
    
    /**
     * Handles Prismatic damage conversion logic.
     * @param target the entity taking damage
     * @param originalType the original element type (should be PRISMATIC)
     * @return the converted element type, or PRISMATIC if no conversion applies
     */
    public static ElementType handlePrismConversion(LivingEntity target, ElementType originalType) {
        if (originalType != ElementType.PRISMATIC) {
            return originalType;
        }
        
        ElementType convertedType = originalType;
        
        if (target.hasEffect(AbloomModEffects.PRISM)) {
            // PRISM is active: check if a new resonance has appeared
            ElementType storedType = getConvertedPrismType(target);
            ElementType currentResonance = getActiveResonanceType(target);
            
            if (currentResonance != null && currentResonance != storedType) {
                // New resonance appeared — switch conversion type and extend PRISM
                setPrismConversionType(target, currentResonance);
                target.removeEffect(AbloomModEffects.PRISM);
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    AbloomModEffects.PRISM, 40 * 20, 0, false, true));
                
                if (AbloomMod.LOGGER.isDebugEnabled()) {
                    AbloomMod.LOGGER.debug("Prism conversion switched from {} to {} (new resonance), PRISM extended", 
                        storedType, currentResonance);
                }
                
                ElementDamageHandler.spawnStatusText(target, 
                    net.minecraft.network.chat.Component.translatable("elemental.tooltip.conversion"), 0xFFFFFF);
                convertedType = currentResonance;
            } else if (currentResonance != null) {
                // Same resonance as stored — use it as-is
                convertedType = currentResonance;
            } else {
                // No active resonance — use stored type from attachment
                convertedType = storedType;
                if (convertedType == null) {
                    convertedType = ElementType.PRISMATIC; // Fallback
                }
            }
        } else {
            // No Prism effect active — activate it from current resonance
            ElementType resonanceType = getActiveResonanceType(target);
            if (resonanceType != null && resonanceType != ElementType.PRISMATIC) {
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    AbloomModEffects.PRISM, 40 * 20, 0, false, true));
                setPrismConversionType(target, resonanceType);
                ElementDamageHandler.spawnStatusText(target, 
                    net.minecraft.network.chat.Component.translatable("elemental.tooltip.conversion"), 0xFFFFFF);
                convertedType = resonanceType;
            }
            // Prism damage does NOT accumulate resonance points in this case
        }
        
        return convertedType;
    }
    
    /**
     * Checks if damage is converted prism damage (originally PRISMATIC but converted to another type).
     */
    public static boolean isConvertedPrism(ElementType originalType, ElementType currentType) {
        return originalType == ElementType.PRISMATIC && currentType != ElementType.PRISMATIC;
    }
    
    /**
     * Checks if pure prism damage should skip accumulation.
     */
    public static boolean shouldSkipAccumulation(ElementType originalType) {
        return originalType == ElementType.PRISMATIC;
    }
    
    /**
     * Gets the stored prism conversion type from the target's attachment.
     */
    private static ElementType getConvertedPrismType(LivingEntity target) {
        return AbloomModAttachments.getPrismConversionType(target);
    }
    
    /**
     * Sets the prism conversion type for a target.
     */
    private static void setPrismConversionType(LivingEntity target, ElementType type) {
        AbloomModAttachments.setPrismConversionType(target, type);
    }
    
    /**
     * Gets the active resonance effect type on the target.
     * Resonance effects indicate which element type prism damage should convert to.
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
}
