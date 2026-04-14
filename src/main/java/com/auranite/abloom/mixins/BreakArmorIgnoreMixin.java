package com.auranite.abloom.mixins;

import com.auranite.abloom.AbloomModEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class BreakArmorIgnoreMixin {

    @Redirect(
            method = "getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getArmorValue()I")
    )
    private int breakBypassesArmor(LivingEntity target, DamageSource source) {
        if (target.hasEffect(AbloomModEffects.BREAK)) {
            return 0;
        }
        return target.getArmorValue();
    }
}