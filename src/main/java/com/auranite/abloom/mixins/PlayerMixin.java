package com.auranite.abloom.mixins;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class PlayerMixin {

    /**
     * Модифицирует переменную 'flag' (или аналогичную), отвечающую за критический удар.
     * В ванильном коде это обычно булева переменная, вычисляемая из fallDistance.
     *
     * Примечание: Имена локальных переменных могут отличаться в зависимости от версий маппингов.
     * Безопаснее использовать @ModifyConstant или @Redirect для проверки условий.
     */

    // Вариант 1: Через Redirect (рекомендуемый для точечной замены условия)
    @Redirect(method = "attack", at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/entity/player/Player;fallDistance:F"))
    private float abloom$redirectFallDistance(Player instance) {
        // Всегда возвращаем 0, чтобы условие "fallDistance > 0" никогда не выполнялось
        return 0.0f;
    }
}