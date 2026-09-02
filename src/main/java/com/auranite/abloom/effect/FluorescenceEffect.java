package com.auranite.abloom.effect;

import com.auranite.abloom.init.AbloomModAttachments;
import com.auranite.abloom.util.ElementType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class FluorescenceEffect extends MobEffect {
    public FluorescenceEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public String getDescriptionId() {
        return "effect.abloom.fluorescence";
    }

    public String getDescriptionId(LivingEntity entity) {
        if (entity != null) {
            ElementType type = AbloomModAttachments.getFluorescenceType(entity);
            if (type != null) {
                return "effect.abloom.fluorescence." + type.name().toLowerCase();
            }
        }
        return getDescriptionId();
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
        return true;
    }
}
