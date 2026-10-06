package com.auranite.abloom.effect;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.init.AbloomModAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class ResonanceGainEffect extends MobEffect {
    public ResonanceGainEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
        this.addAttributeModifier(AbloomModAttributes.RESONANCE_FREQUENCY_BONUS, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.resonance_gain_0"), 60, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(AbloomModAttributes.RESONANCE_ACCUMULATION_BUILDUP, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.resonance_gain_1"), 0.2, AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        return true;
    }
}