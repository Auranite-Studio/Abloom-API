package com.auranite.abloom.effect;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.init.AbloomModAttributes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ShockEffect extends MobEffect {
    public ShockEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.shock_1"), -0.4, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        this.addAttributeModifier(AbloomModAttributes.DMG_BONUS, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.shock_dmg_0"), -0.4, AttributeModifier.Operation.ADD_VALUE);
    }
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
