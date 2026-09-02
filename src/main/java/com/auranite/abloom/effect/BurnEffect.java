package com.auranite.abloom.effect;

import com.auranite.abloom.init.AbloomModEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class BurnEffect extends MobEffect {
    public BurnEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }
    
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        Level level = entity.level();
        
        // Если под водой, под дождём или под эффектом заморозки - наносим урон вместо горения
        boolean inWater = level.isWaterAt(entity.blockPosition());
        boolean inRain = level.isRaining() && level.canSeeSkyFromBelowWater(entity.blockPosition()) && !entity.isInWater();
        boolean HasFreezeEffect = entity.hasEffect(AbloomModEffects.FREEZE);
        boolean isFreezing = entity.isFreezing();

        if (inWater || inRain || isFreezing || HasFreezeEffect) {
            // Наносим урон от огня
            DamageSource damageSource = entity.damageSources().inFire();
            float damage = 1.0f + (amplifier * 0.5f); // 1, 1.5, 2.0 и т.д.
            entity.hurt(damageSource, damage);
            return true;
        }
        
        // Обычное горение
        if (!entity.isOnFire()) {
            entity.igniteForSeconds(1);
        }
        return true;
    }
}
