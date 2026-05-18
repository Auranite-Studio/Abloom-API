package com.auranite.abloom.effect;

import com.auranite.abloom.AbloomModEffects;
import com.auranite.abloom.ElementDamageHandler;
import com.auranite.abloom.ElementType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class CorruptionEffect extends MobEffect {
    public CorruptionEffect(int color) {
        super(ResonanceEffectCategory.RESONANCE, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {

        if (entity.level().isClientSide) {
            return true;
        }

        MobEffectInstance effectInstance = entity.getEffect(AbloomModEffects.CORRUPTION);
        if (effectInstance == null) {
            return false;
        }
        int duration = effectInstance.getDuration();

        if (duration % 20 == 0) {
            float damage = 1.0f + amplifier * 0.5f;
            ElementDamageHandler.dealElementDamage(entity, ElementType.ETHER, damage, 0);
        }

        return true;
    }
}
