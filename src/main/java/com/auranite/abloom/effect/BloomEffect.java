package com.auranite.abloom.effect;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.handler.ElementDamageHandler;
import com.auranite.abloom.init.AbloomModAttributes;
import com.auranite.abloom.util.ElementType;
import com.auranite.abloom.init.AbloomModEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class BloomEffect extends MobEffect {
    public BloomEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
        this.addAttributeModifier(AbloomModAttributes.NATURAL_RESIST_MOD, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.bloom_resist"), -0.35, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(AbloomModAttributes.PHYSICAL_RESIST_MOD, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.bloom_resist"), -0.15, AttributeModifier.Operation.ADD_VALUE);
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
