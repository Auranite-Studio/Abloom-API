package com.auranite.abloom.effect;
import com.auranite.abloom.ElementDamageHandler;
import com.auranite.abloom.ElementType;
import com.auranite.abloom.AbloomModEffects;
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
        int duration = effectInstance.getDuration();
        int currentFrozen = entity.getTicksFrozen();
        int required = entity.getTicksRequiredToFreeze();
        if (currentFrozen < required) {
            entity.setTicksFrozen(Math.min(currentFrozen + 3 + amplifier, required));
        }
        if (entity.isFullyFrozen()) {
            if (duration % 20 == 0) {
                float damage = 1.0f + amplifier * 0.5f;
                entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.85, 1.0, 0.85));
                ElementDamageHandler.dealElementDamage(entity, ElementType.ICE, damage, 0);
            }
        }
        return true;
    }
}