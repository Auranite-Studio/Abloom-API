package com.auranite.abloom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * Represents a custom elemental damage type loaded from data packs.
 * Data format:
 * {
 *   "id": "abloom:wind_dmg",
 *   "damage_type_id": "wind_dmg",
 *   "color": 0xFF87CEEB,
 *   "element_type": "WIND",
 *   "resonance_effect": "abloom:erosion",
 *   "resonance_effect_duration": 15,
 *   "element_Item_tooltip": "elemental.tooltip.wind",
 *   "element_armor_resistance_tooltip": "elemental.resistance.wind",
 *   "resonance_effect.status_text_display": "elemental.tooltip.wind_whirlwind"
 * }
 */
public class CustomElementalDamageType {
    
    public static final Codec<CustomElementalDamageType> CODEC = RecordCodecBuilder.create(builder ->
        builder.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(CustomElementalDamageType::getId),
            Codec.STRING.fieldOf("damage_type_id").forGetter(CustomElementalDamageType::getDamageTypeId),
            Codec.INT.optionalFieldOf("color", 0xFFFFFF).forGetter(CustomElementalDamageType::getColor),
            ElementType.CODEC.optionalFieldOf("element_type").forGetter(CustomElementalDamageType::getElementType),
            ResourceLocation.CODEC.optionalFieldOf("resonance_effect").forGetter(CustomElementalDamageType::getResonanceEffect),
            Codec.INT.optionalFieldOf("resonance_effect_duration", 15).forGetter(CustomElementalDamageType::getResonanceEffectDuration),
            Codec.STRING.optionalFieldOf("element_Item_tooltip").forGetter(CustomElementalDamageType::getElementItemTooltip),
            Codec.STRING.optionalFieldOf("element_armor_resistance_tooltip").forGetter(CustomElementalDamageType::getElementArmorResistanceTooltip),
            Codec.STRING.optionalFieldOf("resonance_effect.status_text_display").forGetter(CustomElementalDamageType::getResonanceEffectStatusTextDisplay)
        ).apply(builder, CustomElementalDamageType::new)
    );

    private final ResourceLocation id;
    private final String damageTypeId;
    private final int color;
    private final Optional<ElementType> elementType;
    private final Optional<ResourceLocation> resonanceEffect;
    private final int resonanceEffectDuration;
    private final Optional<String> elementItemTooltip;
    private final Optional<String> elementArmorResistanceTooltip;
    private final Optional<String> resonanceEffectStatusTextDisplay;

    public CustomElementalDamageType(ResourceLocation id,
                                     String damageTypeId,
                                     int color,
                                     Optional<ElementType> elementType,
                                     Optional<ResourceLocation> resonanceEffect,
                                     int resonanceEffectDuration,
                                     Optional<String> elementItemTooltip,
                                     Optional<String> elementArmorResistanceTooltip,
                                     Optional<String> resonanceEffectStatusTextDisplay) {
        this.id = id;
        this.damageTypeId = damageTypeId;
        this.color = color;
        this.elementType = elementType;
        this.resonanceEffect = resonanceEffect;
        this.resonanceEffectDuration = resonanceEffectDuration;
        this.elementItemTooltip = elementItemTooltip;
        this.elementArmorResistanceTooltip = elementArmorResistanceTooltip;
        this.resonanceEffectStatusTextDisplay = resonanceEffectStatusTextDisplay;
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getDamageTypeId() {
        return damageTypeId;
    }

    public int getColor() {
        return color;
    }

    public Optional<ElementType> getElementType() {
        return elementType;
    }

    public Optional<ResourceLocation> getResonanceEffect() {
        return resonanceEffect;
    }

    public int getResonanceEffectDuration() {
        return resonanceEffectDuration;
    }

    public Optional<String> getElementItemTooltip() {
        return elementItemTooltip;
    }

    public Optional<String> getElementArmorResistanceTooltip() {
        return elementArmorResistanceTooltip;
    }

    public Optional<String> getResonanceEffectStatusTextDisplay() {
        return resonanceEffectStatusTextDisplay;
    }

    public String getModId() {
        return id.getNamespace();
    }

    public String getPath() {
        return id.getPath();
    }

    @Override
    public String toString() {
        return "CustomElementalDamageType{" +
                "id=" + id +
                ", damageTypeId='" + damageTypeId + '\'' +
                ", color=0x" + Integer.toHexString(color) +
                ", resonanceEffect=" + resonanceEffect +
                ", resonanceEffectDuration=" + resonanceEffectDuration +
                '}';
    }
}
