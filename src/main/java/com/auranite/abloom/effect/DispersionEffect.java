package com.auranite.abloom.effect;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.init.AbloomModAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.LivingEntity;

public class DispersionEffect extends MobEffect {
    private int applicationCount = 0;

    public DispersionEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
        this.addAttributeModifier(
                AbloomModAttributes.INCOMING_DMG,
                ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "effect.dispersion_incoming_dmg"),
                0.15,
                AttributeModifier.Operation.ADD_VALUE
        );
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        return true;
    }

    @Override
    public void applyInstantenousEffect(net.minecraft.world.entity.Entity source,
                                         net.minecraft.world.entity.Entity attacker,
                                         LivingEntity target, int amplifier, double health) {
    }

    public int getLevel() {
        return Math.min(applicationCount, 3);
    }

    public int incrementApplication() {
        applicationCount++;
        return Math.min(applicationCount, 3);
    }

    public void resetApplicationCount() {
        applicationCount = 0;
    }
}
