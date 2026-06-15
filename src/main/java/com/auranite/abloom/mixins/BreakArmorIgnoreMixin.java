package com.auranite.abloom.mixins;

import com.auranite.abloom.AbloomModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
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
        else if (target.hasEffect(AbloomModEffects.RUPTURE)) {
            int armorValue = target.getArmorValue();
            return (int) (armorValue * 0.7);
        }
        else if (target.hasEffect(AbloomModEffects.ECLIPSE)) {
            int armorValue = target.getArmorValue();
            float reduction = 0.9f; // 10% reduction
            
            // Calculate additional reduction for each harmful effect
            for (MobEffectInstance effect : target.getActiveEffects()) {
                MobEffect effectInstance = effect.getEffect().value();
                if (effectInstance.getCategory() == MobEffectCategory.HARMFUL && 
                        effectInstance != AbloomModEffects.ECLIPSE.get()) {
                    reduction -= 0.10f; // 10% additional reduction per harmful effect
                }
            }
            
            reduction = Math.max(0.5f, reduction); // Maximum 50% reduction
            return (int) (armorValue * reduction);
        }
        return target.getArmorValue();
    }
}
