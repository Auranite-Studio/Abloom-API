package com.auranite.abloom.effect;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.init.AbloomModAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class ReflectionBuffEffect extends MobEffect {
    public ReflectionBuffEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFFFFF);
        this.addAttributeModifier(
                AbloomModAttributes.RESONANCE_ACCUMULATION_BUILDUP,
                ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.reflection"),
                0.1,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
}
