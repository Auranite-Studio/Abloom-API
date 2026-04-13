package com.auranite.abloom.effect;

import com.auranite.abloom.ElementDamageHandler;
import com.auranite.abloom.ElementType;
import com.auranite.abloom.AbloomModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class BloomEffect extends MobEffect {
    public BloomEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
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


        MobEffectInstance effectInstance = entity.getEffect(AbloomModEffects.BLOOM);
        if (effectInstance == null) {
            return false;
        }
        int duration = effectInstance.getDuration();


            if (duration % 20 == 0) {
                float damage = 1.0f + amplifier * 0.5f;
                ElementDamageHandler.dealElementDamage(entity, ElementType.NATURAL, damage, 0);
            }

        return true;
    }
}