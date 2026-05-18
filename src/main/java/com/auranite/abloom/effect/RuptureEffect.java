package com.auranite.abloom.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class RuptureEffect extends MobEffect {
    public RuptureEffect(int color) {
        super(ResonanceEffectCategory.RESONANCE, color);
    }
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
