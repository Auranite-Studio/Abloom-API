package com.auranite.abloom.network;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.util.IElementalType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record SpawnDamageNumberPacket(
        int entityId,
        float damage,
        @Nullable IElementalType elementType,
        int color,
        boolean isCrit,
        boolean hasBreak,
        boolean isMultiCrit
) implements CustomPacketPayload {

    public static final Type<SpawnDamageNumberPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID, "spawn_damage_number")
    );

    public static final StreamCodec<FriendlyByteBuf, SpawnDamageNumberPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public SpawnDamageNumberPacket decode(FriendlyByteBuf buffer) {
                    int entityId = buffer.readInt();
                    float damage = buffer.readFloat();
                    boolean hasType = buffer.readBoolean();
                    @Nullable IElementalType elementType = hasType ? IElementalType.byName(buffer.readUtf()).orElse(null) : null;
                    int color = buffer.readInt();
                    boolean isCrit = buffer.readBoolean();
                    boolean hasBreak = buffer.readBoolean();
                    boolean isMultiCrit = buffer.readBoolean();
                    return new SpawnDamageNumberPacket(entityId, damage, elementType, color, isCrit, hasBreak, isMultiCrit);
                }

                @Override
                public void encode(FriendlyByteBuf buffer, SpawnDamageNumberPacket packet) {
                    buffer.writeInt(packet.entityId);
                    buffer.writeFloat(packet.damage);
                    if (packet.elementType != null) {
                        buffer.writeBoolean(true);
                        buffer.writeUtf(packet.elementType.name());
                    } else {
                        buffer.writeBoolean(false);
                    }
                    buffer.writeInt(packet.color);
                    buffer.writeBoolean(packet.isCrit);
                    buffer.writeBoolean(packet.hasBreak);
                    buffer.writeBoolean(packet.isMultiCrit);
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
