package com.auranite.abloom.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class ErosionEffect extends MobEffect {

    public ErosionEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
    
    /**
     * Returns the duration of the Erosion effect in ticks.
     * Default duration is 8 seconds (160 ticks).
     */
    public static int getDurationSeconds() {
        return 8;
    }
    
    public static int getDurationTicks() {
        return getDurationSeconds() * 20;
    }
}
