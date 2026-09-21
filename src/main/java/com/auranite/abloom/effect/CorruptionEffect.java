package com.auranite.abloom.effect;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.init.AbloomModAttributes;
import com.auranite.abloom.init.AbloomModEffects;
import com.auranite.abloom.handler.ElementDamageHandler;
import com.auranite.abloom.util.ElementType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CorruptionEffect extends MobEffect {
    public CorruptionEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
        float resistModValue = -0.20f;
        this.addAttributeModifier(AbloomModAttributes.FIRE_RESIST_MOD, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.corruption_resist"), resistModValue, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(AbloomModAttributes.PHYSICAL_RESIST_MOD, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.corruption_resist"), resistModValue, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(AbloomModAttributes.WIND_RESIST_MOD, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.corruption_resist"), resistModValue, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(AbloomModAttributes.EARTH_RESIST_MOD, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.corruption_resist"), resistModValue, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(AbloomModAttributes.WATER_RESIST_MOD, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.corruption_resist"), resistModValue, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(AbloomModAttributes.ICE_RESIST_MOD, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.corruption_resist"), resistModValue, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(AbloomModAttributes.ELECTRIC_RESIST_MOD, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.corruption_resist"), resistModValue, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(AbloomModAttributes.ENERGY_RESIST_MOD, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.corruption_resist"), resistModValue, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(AbloomModAttributes.NATURAL_RESIST_MOD, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.corruption_resist"), resistModValue, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(AbloomModAttributes.QUANTUM_RESIST_MOD, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.corruption_resist"), resistModValue, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(AbloomModAttributes.ETHER_RESIST_MOD, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.corruption_resist"), resistModValue, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(AbloomModAttributes.LIGHT_RESIST_MOD, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.corruption_resist"), resistModValue, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(AbloomModAttributes.SHADOW_RESIST_MOD, ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.corruption_resist"), resistModValue, AttributeModifier.Operation.ADD_VALUE);
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

        MobEffectInstance effectInstance = entity.getEffect(AbloomModEffects.CORRUPTION);
        if (effectInstance == null) {
            return false;
        }
        int duration = effectInstance.getDuration();

        if (duration % 20 == 0) {
            float damage = 1.0f + amplifier * 0.5f;
            ElementDamageHandler.dealElementDamage(entity, ElementType.ETHER, damage, 0);
        }

        return true;
    }
}
