package com.auranite.abloom.mixins;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin that disables vanilla Minecraft critical hit damage multiplier.
 * The custom crit system in ElementDamageHandler handles all critical hits.
 */
@Mixin(LivingEntity.class)
public class CriticalMixin {

    @Shadow
    protected float getDamageAfterArmorAbsorb(DamageSource source, float amount) { return 0; }

    @Inject(
            method = "getDamageAfterMagicAbsorb",
            at = @At("HEAD"),
            cancellable = true
    )
    private void bypassVanillaCriticalStrike(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        float result = getDamageAfterArmorAbsorb(source, amount);
        cir.setReturnValue(result);
    }
}
