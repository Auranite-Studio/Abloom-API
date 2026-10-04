package com.auranite.abloom.effect;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.handler.ElementDamageHandler;
import com.auranite.abloom.util.ElementType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class BleedingEffect extends MobEffect {
    public BleedingEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.bleeding_0"), -0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.onGround()) {
            float damage = 1.0f + (amplifier * 0.5f);
            ElementDamageHandler.dealElementDamage(entity, ElementType.PHYSICAL, damage, 0, null, true);
        }
        return true;
    }
}