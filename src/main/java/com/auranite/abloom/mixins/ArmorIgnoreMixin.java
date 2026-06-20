package com.auranite.abloom.mixins;

import com.auranite.abloom.ElementDamageHandler;
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
public class ArmorIgnoreMixin {

    @Redirect(
            method = "getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getArmorValue()I")
    )
    private int breakBypassesArmor(LivingEntity target, DamageSource source) {
        int baseArmorValue = target.getArmorValue();
        
        if (target.hasEffect(AbloomModEffects.BREAK)) {
            return 0;
        }
        
        float armorMultiplier = 1.0f;
        
        if (target.hasEffect(AbloomModEffects.RUPTURE)) {
            armorMultiplier *= 0.7f;
        }
        
        if (target.hasEffect(AbloomModEffects.ECLIPSE)) {
            float reduction = 0.9f;
            
            for (MobEffectInstance effect : target.getActiveEffects()) {
                MobEffect effectInstance = effect.getEffect().value();
                if (effectInstance.getCategory() == MobEffectCategory.HARMFUL && 
                        effectInstance != AbloomModEffects.ECLIPSE.get()) {
                    reduction -= 0.10f;
                }
            }
            
            reduction = Math.max(0.5f, reduction);
            armorMultiplier *= reduction;
        }
        
        int affectedArmorValue = (int) (baseArmorValue * armorMultiplier);
        return affectedArmorValue;
    }
}
