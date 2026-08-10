package com.yanghao.effect_display.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(
   modid = "effect_display"
)
public record SyncEntityEffectsMessage(int entityId, List<MobEffectInstance> effects) implements CustomPacketPayload {
   public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("effect_display", "sync_entity_effects");
   public static final StreamCodec<FriendlyByteBuf, SyncEntityEffectsMessage> STREAM_CODEC = StreamCodec.ofMember(SyncEntityEffectsMessage::encode, SyncEntityEffectsMessage::new);
   public static final CustomPacketPayload.Type<SyncEntityEffectsMessage> TYPE;

   public SyncEntityEffectsMessage(FriendlyByteBuf buf) {
      this(buf.readInt(), readEffects(buf));
   }

   public void encode(FriendlyByteBuf buf) {
      buf.writeInt(this.entityId);
      buf.writeInt(this.effects.size());

      for(MobEffectInstance effect : this.effects) {
         writeEffectInstance(buf, effect);
      }

   }

   private static List<MobEffectInstance> readEffects(FriendlyByteBuf buf) {
      int size = buf.readInt();
      List<MobEffectInstance> effects = new ArrayList(size);

      for(int i = 0; i < size; ++i) {
         MobEffectInstance effect = readEffectInstance(buf);
         if (effect != null) {
            effects.add(effect);
         }
      }

      return effects;
   }

   private static MobEffectInstance readEffectInstance(FriendlyByteBuf buf) {
      CompoundTag tag = buf.readNbt();
      return tag != null ? MobEffectInstance.load(tag) : null;
   }

   private static void writeEffectInstance(FriendlyByteBuf buf, MobEffectInstance effect) {
      buf.writeNbt(effect.save());
   }

   public static SyncEntityEffectsMessage decode(FriendlyByteBuf buf) {
      return new SyncEntityEffectsMessage(buf);
   }

   public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public void handle(IPayloadContext ctx) {
      ctx.enqueueWork(() -> {
         Player player = ctx.player();
         if (player != null) {
            Level level = player.level();
            Entity entity = level.getEntity(this.entityId);
            if (entity instanceof LivingEntity) {
               LivingEntity livingEntity = (LivingEntity)entity;
               if (this.effects.isEmpty()) {
                  ClientEntityEffectsStorage.removeEntityEffects(this.entityId);
               } else {
                  ClientEntityEffectsStorage.updateEntityEffects(this.entityId, this.effects);
               }
            }
         }

      });
   }

   @SubscribeEvent
   public static void onPlayerStartTracking(PlayerEvent.StartTracking event) {
      if (!event.getEntity().level().isClientSide() && event.getTarget() instanceof LivingEntity && event.getEntity() instanceof ServerPlayer) {
         ServerPlayer player = (ServerPlayer)event.getEntity();
         LivingEntity targetEntity = (LivingEntity)event.getTarget();
         ArrayList<MobEffectInstance> effects = new ArrayList(targetEntity.getActiveEffects());
         if (effects.isEmpty()) {
            return;
         }

         PacketDistributor.sendToPlayer(player, new SyncEntityEffectsMessage(targetEntity.getId(), effects), new CustomPacketPayload[0]);
      }

   }

   @SubscribeEvent
   public static void updateAllEntityEffects(MobEffectEvent.Added event) {
      LivingEntity entity = event.getEntity();
      if (!entity.level().isClientSide && entity.level() instanceof ServerLevel) {
         List<MobEffectInstance> currentEffects = new ArrayList(entity.getActiveEffects());
         boolean effectExists = currentEffects.stream().anyMatch((effect) -> effect.getEffect().equals(((MobEffectInstance)Objects.requireNonNull(event.getEffectInstance())).getEffect()));
         if (!effectExists) {
            currentEffects.add(event.getEffectInstance());
         }

         PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new SyncEntityEffectsMessage(entity.getId(), new ArrayList(currentEffects)), new CustomPacketPayload[0]);
      }

   }

   @SubscribeEvent
   public static void updateAllEntityEffects(MobEffectEvent.Remove event) {
      syncEffectsAfterRemoval(event.getEffectInstance(), event.getEntity());
   }

   @SubscribeEvent
   public static void updateAllEntityEffects(MobEffectEvent.Expired event) {
      LivingEntity target = event.getEntity();
      syncEffectsAfterRemoval(event.getEffectInstance(), target);
   }

   public static void syncEffectsAfterRemoval(MobEffectInstance effectInstance, LivingEntity entity2) {
      if (!entity2.level().isClientSide()) {
         List<MobEffectInstance> effects = entity2.getActiveEffects().stream().filter((effect) -> effect.getEffect().value() != effectInstance.getEffect().value()).toList();
         PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity2, new SyncEntityEffectsMessage(entity2.getId(), effects), new CustomPacketPayload[0]);
      }

   }

   static {
      TYPE = new CustomPacketPayload.Type(ID);
   }
}
