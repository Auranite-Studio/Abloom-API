package com.auranite.abloom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * Represents a custom elemental damage type that can be registered via datapack.
 */
public class CustomElementType {
    public static final Codec<CustomElementType> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(CustomElementType::getId),
                    Codec.STRING.fieldOf("damage_type_id").forGetter(CustomElementType::getDamageTypeId),
                    Codec.INT.fieldOf("color").forGetter(CustomElementType::getColor),
                    Codec.BOOL.optionalFieldOf("is_custom", true).forGetter(CustomElementType::isCustom)
            ).apply(instance, CustomElementType::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CustomElementType> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, CustomElementType::getId,
            ByteBufCodecs.STRING_UTF8, CustomElementType::getDamageTypeId,
            ByteBufCodecs.VAR_INT, CustomElementType::getColor,
            ByteBufCodecs.BOOL, CustomElementType::isCustom,
            CustomElementType::new
    );

    private final ResourceLocation id;
    private final String damageTypeId;
    private final int color;
    private final boolean isCustom;

    public CustomElementType(ResourceLocation id, String damageTypeId, int color, boolean isCustom) {
        this.id = id;
        this.damageTypeId = damageTypeId;
        this.color = color;
        this.isCustom = isCustom;
    }

    public CustomElementType(ResourceLocation id, String damageTypeId, int color) {
        this(id, damageTypeId, color, true);
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getDamageTypeId() {
        return damageTypeId;
    }

    public String getFullDamageTypeId() {
        if (id.getNamespace().equals(AbloomMod.MODID)) {
            return AbloomMod.MODID + ":" + damageTypeId;
        }
        return id.getNamespace() + ":" + damageTypeId;
    }

    public int getColor() {
        return color;
    }

    public boolean isCustom() {
        return isCustom;
    }

    /**
     * Converts this CustomElementType to an ElementType for compatibility.
     * Returns Optional.empty() if no matching ElementType exists and it's not a custom type.
     */
    public Optional<ElementType> toElementType() {
        // Try to match with built-in ElementType by damage type ID
        for (ElementType type : ElementType.values()) {
            if (type.getDamageTypeId().equals(this.damageTypeId)) {
                return Optional.of(type);
            }
        }
        
        // For custom types, we'll handle them separately in the damage system
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "CustomElementType{id=" + id + ", damageTypeId='" + damageTypeId + "', color=0x" + Integer.toHexString(color) + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CustomElementType other)) return false;
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
