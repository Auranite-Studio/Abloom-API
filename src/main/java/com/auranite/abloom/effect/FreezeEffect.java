package com.auranite.abloom.effect;

import com.auranite.abloom.handler.ElementDamageHandler;
import com.auranite.abloom.util.ElementType;
import com.auranite.abloom.init.AbloomModEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class FreezeEffect extends MobEffect {
    public FreezeEffect(int color) {
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

        MobEffectInstance effectInstance = entity.getEffect(AbloomModEffects.FREEZE);
        if (effectInstance == null) {
            return false;
        }

        int remainingDuration = effectInstance.getDuration();
        int remainingSeconds = (remainingDuration + 19) / 20; // Округляем вверх до секунд

        // Если цель горит - прерываем заморозку и наносим урон от льда (15% за каждую оставшуюся секунду)
        if (entity.isOnFire()) {
            float burnDamage = 1.0f + amplifier * 0.5f;
            DamageSource burnSource = entity.damageSources().inFire();
            entity.hurt(burnSource, burnDamage);

            // Наносим урон от льда за оставшиеся секунды (15% за каждую)
            float freezeDamage = remainingSeconds * 0.15f;
            ElementDamageHandler.dealElementDamage(entity, ElementType.ICE, freezeDamage, 0);

            // Явно снимаем эффект заморозки
            entity.setTicksFrozen(0);
            entity.removeEffect(AbloomModEffects.FREEZE);
            return true;
        }

        int currentFrozen = entity.getTicksFrozen();
        int required = entity.getTicksRequiredToFreeze();

        if (currentFrozen < required) {
            entity.setTicksFrozen(Math.min(currentFrozen + 3 + amplifier, required));
        }

        if (entity.isFullyFrozen()) {
            if (remainingDuration % 20 == 0) {
                float damage = 1.0f + amplifier * 0.5f;
                entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.85, 1.0, 0.85));
                ElementDamageHandler.dealElementDamage(entity, ElementType.ICE, damage, 0);
            }
        }

        return true;
    }
}
