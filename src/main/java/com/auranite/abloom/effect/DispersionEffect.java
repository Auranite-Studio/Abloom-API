package com.auranite.abloom.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class DispersionEffect extends MobEffect {
    public DispersionEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }
}
