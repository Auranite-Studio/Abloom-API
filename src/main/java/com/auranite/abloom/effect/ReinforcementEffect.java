package com.auranite.abloom.effect;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.handler.ElementDamageHandler;
import com.auranite.abloom.util.ElementType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ReinforcementEffect extends MobEffect {
    public ReinforcementEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
        this.addAttributeModifier(Attributes.ARMOR, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.reinforcement_0"), 6, AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}