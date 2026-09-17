package com.auranite.abloom.network;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.init.AbloomModAttachments;
import com.auranite.abloom.util.ElementType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = AbloomMod.MODID)
public record SyncResonanceAccumulationMessage(
        int entityId,
        Map<ElementType, Integer> points
) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "sync_resonance_accumulation");
    public static final Type<SyncResonanceAccumulationMessage> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, SyncResonanceAccumulationMessage> STREAM_CODEC =
            StreamCodec.ofMember(SyncResonanceAccumulationMessage::encode, SyncResonanceAccumulationMessage::new);

    public SyncResonanceAccumulationMessage(FriendlyByteBuf buf) {
        this(buf.readInt(), readPoints(buf));
    }

    private static Map<ElementType, Integer> readPoints(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<ElementType, Integer> map = new EnumMap<>(ElementType.class);
        for (int i = 0; i < size; i++) {
            ElementType type = buf.readEnum(ElementType.class);
            int points = buf.readInt();
            map.put(type, points);
        }
        return map;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.points.size());
        for (Map.Entry<ElementType, Integer> entry : this.points.entrySet()) {
            buf.writeEnum(entry.getKey());
            buf.writeInt(entry.getValue());
        }
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if (player != null) {
                Level level = player.level();
                var entity = level.getEntity(this.entityId);
                if (entity != null) {
                    if (this.points.isEmpty()) {
                        ClientResonanceAccumulationStorage.removeEntityAccumulation(this.entityId);
                    } else {
                        ClientResonanceAccumulationStorage.updateEntityAccumulation(this.entityId, this.points);
                    }
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @SubscribeEvent
    public static void onPlayerStartTracking(PlayerEvent.StartTracking event) {
        if (!event.getEntity().level().isClientSide() && event.getTarget() instanceof LivingEntity && event.getEntity() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            LivingEntity targetEntity = (LivingEntity) event.getTarget();
            Map<ElementType, Integer> accumulator = AbloomModAttachments.getAccumulator(targetEntity);

            Map<ElementType, Integer> nonZeroPoints = new EnumMap<>(ElementType.class);
            for (Map.Entry<ElementType, Integer> entry : accumulator.entrySet()) {
                if (entry.getValue() > 0) {
                    nonZeroPoints.put(entry.getKey(), entry.getValue());
                }
            }

            if (!nonZeroPoints.isEmpty()) {
                PacketDistributor.sendToPlayer(player,
                        new SyncResonanceAccumulationMessage(targetEntity.getId(), nonZeroPoints));
            }
        }
    }
}
