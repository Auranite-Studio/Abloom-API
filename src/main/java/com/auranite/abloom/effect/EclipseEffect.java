package com.auranite.abloom.effect;

import com.auranite.abloom.AbloomMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class EclipseEffect extends MobEffect {
    public EclipseEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
        this.addAttributeModifier(Attributes.MAX_HEALTH, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.eclipse_0"), -0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        this.addAttributeModifier(Attributes.ARMOR, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.eclipse_1"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
