package com.auranite.abloom.network;

import com.auranite.abloom.AbloomMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public record SpawnStatusTextPacket(
        int entityId,
        Component text,
        int color
) implements CustomPacketPayload {

    public static final Type<SpawnStatusTextPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "spawn_status_text")
    );

    public static final StreamCodec<FriendlyByteBuf, SpawnStatusTextPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public SpawnStatusTextPacket decode(FriendlyByteBuf buffer) {
                    int entityId = buffer.readInt();
                    String text = buffer.readUtf(32767);
                    int color = buffer.readInt();
                    Component component = Component.literal(text).withStyle(Style.EMPTY.withColor(color));
                    return new SpawnStatusTextPacket(entityId, component, color);
                }

                @Override
                public void encode(FriendlyByteBuf buffer, SpawnStatusTextPacket packet) {
                    buffer.writeInt(packet.entityId);
                    buffer.writeUtf(packet.text().getString());
                    buffer.writeInt(packet.color());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
