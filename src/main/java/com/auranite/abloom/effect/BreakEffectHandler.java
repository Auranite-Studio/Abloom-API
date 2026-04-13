package com.auranite.abloom.effect;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.AbloomModEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.minecraft.world.entity.LivingEntity;


@EventBusSubscriber(modid = AbloomMod.MODID)
public class BreakEffectHandler {

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();

        if (target.hasEffect(AbloomModEffects.BREAK)) {
                    target.getName().getString(), event.getAmount());
        }
    }
}