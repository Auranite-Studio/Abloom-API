package com.yanghao.effect_display.network;

import java.util.ArrayList;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(
   modid = "effect_display"
)
public class EffectDisplayNetworking {
   public static void register(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar registrar = event.registrar("1");
      registrar.playBidirectional(SyncEntityEffectsMessage.TYPE, SyncEntityEffectsMessage.STREAM_CODEC, SyncEntityEffectsMessage::handle);
   }

   @SubscribeEvent
   public static void onLivingDeath(LivingDeathEvent event) {
      LivingEntity entity = event.getEntity();
      if (!entity.level().isClientSide) {
         PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new SyncEntityEffectsMessage(entity.getId(), new ArrayList()), new CustomPacketPayload[0]);
      }

   }
}
