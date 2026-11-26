package com.auranite.abloom.effect;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.init.AbloomModAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class OverloadEffect extends MobEffect {
    public OverloadEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
        this.addAttributeModifier(Attributes.MAX_HEALTH, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.overload_0"), -0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        this.addAttributeModifier(AbloomModAttributes.INCOMING_DMG, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.overload_dmg_0"), 0.4, AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
