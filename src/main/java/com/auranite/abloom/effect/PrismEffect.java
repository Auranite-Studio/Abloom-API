package com.auranite.abloom.effect;

import com.auranite.abloom.AbloomModAttachments;
import com.auranite.abloom.ElementType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Effect applied when prismatic damage triggers on a target with existing elemental resonance.
 * This effect converts incoming prismatic damage to the resonant element type.
 */
public class PrismEffect extends MobEffect {
    
    public PrismEffect() {
        super(MobEffectCategory.NEUTRAL, 0xFFFFFF);
    }
    
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
    
    /**
     * Gets the stored resonance element type for this entity.
     * This is the element type that prismatic damage will be converted to.
     */
    public static ElementType getStoredResonanceType(LivingEntity entity) {
        if (entity == null) return null;
        return AbloomModAttachments.getPrismResonanceType(entity);
    }
    
    /**
     * Sets the stored resonance type for the prism effect.
     * This determines what element prismatic damage will be converted to.
     */
    public static void setStoredResonanceType(LivingEntity entity, ElementType type) {
        if (entity == null) return;
        AbloomModAttachments.setPrismResonanceType(entity, type);
    }
    
    /**
     * Clears the stored resonance type.
     */
    public static void clearStoredResonanceType(LivingEntity entity) {
        if (entity == null) return;
        AbloomModAttachments.clearPrismResonanceType(entity);
    }
}
