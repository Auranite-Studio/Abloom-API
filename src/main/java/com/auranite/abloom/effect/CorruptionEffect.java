package com.auranite.abloom.effect;

import com.auranite.abloom.ElementType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class CorruptionEffect extends MobEffect {
    public CorruptionEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // Deal periodic ether damage based on amplifier level
        float damageAmount = 1.0f + (amplifier * 0.5f);
        entity.hurt(entity.damageSources().magic(), damageAmount);
        
        // Apply 20% resistance reduction to all elemental damage types
        for (ElementType type : ElementType.values()) {
            if (type != ElementType.PHYSICAL) {
                // The resistance reduction is handled by checking for this effect
                // in ElementResistanceManager or ElementDamageHandler
            }
        }
        
        return true;
    }
}
