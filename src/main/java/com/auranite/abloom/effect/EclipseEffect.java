package com.auranite.abloom.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class EclipseEffect extends MobEffect {
    public EclipseEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
