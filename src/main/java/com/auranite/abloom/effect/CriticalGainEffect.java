package com.auranite.abloom.effect;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.init.AbloomModAttributes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CriticalGainEffect extends MobEffect {
    public CriticalGainEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
        this.addAttributeModifier(AbloomModAttributes.CRIT_DMG, Identifier.fromNamespaceAndPath(AbloomMod.MODID, "effect.flash_0"), 0.3, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(AbloomModAttributes.CRIT_CHANCE, Identifier.fromNamespaceAndPath(AbloomMod.MODID, "effect.flash_1"), 0.15, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, Identifier.fromNamespaceAndPath(AbloomMod.MODID, "effect.flash_2"), 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity entity, int amplifier) {
        return true;
    }
}