package com.auranite.abloom.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Optional;

/**
 * Data class representing an elemental damage type loaded from JSON datapack.
 * 
 * JSON format:
 * {
 *   "id": "abloom:sharp_dmg",
 *   "damage_source_id": "minecraft:generic",
 *   "color": 16748011,
 *   "resonance_effect": "minecraft:poison",
 *   "resonance_effect_duration": 8,
 *   "element_Item_tooltip": "elemental.tooltip.sharp",
 *   "element_armor_resistance_tooltip": "elemental.resistance.sharp",
 *   "resonance_effect.status_text_display": "elemental.tooltip.sharp"
 * }
 */
public record ElementalDamageTypeData(
        ResourceLocation id,
        ResourceLocation damageSourceId,
        int color,
        Optional<ResourceLocation> resonanceEffect,
        int resonanceEffectDuration,
        String elementItemTooltip,
        String elementArmorResistanceTooltip,
        Optional<String> resonanceEffectStatusTextDisplay
) {
    public static final Codec<ElementalDamageTypeData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(ElementalDamageTypeData::id),
                    ResourceLocation.CODEC.fieldOf("damage_source_id").forGetter(ElementalDamageTypeData::damageSourceId),
                    Codec.INT.fieldOf("color").forGetter(ElementalDamageTypeData::color),
                    ResourceLocation.CODEC.optionalFieldOf("resonance_effect").forGetter(ElementalDamageTypeData::resonanceEffect),
                    Codec.INT.optionalFieldOf("resonance_effect_duration", 8).forGetter(ElementalDamageTypeData::resonanceEffectDuration),
                    Codec.STRING.fieldOf("element_Item_tooltip").forGetter(ElementalDamageTypeData::elementItemTooltip),
                    Codec.STRING.fieldOf("element_armor_resistance_tooltip").forGetter(ElementalDamageTypeData::elementArmorResistanceTooltip),
                    Codec.STRING.optionalFieldOf("resonance_effect.status_text_display").forGetter(ElementalDamageTypeData::resonanceEffectStatusTextDisplay)
            ).apply(instance, ElementalDamageTypeData::new)
    );

    public MobEffect getResonanceEffect() {
        return resonanceEffect.flatMap(loc -> BuiltInRegistries.MOB_EFFECT.getOptional(loc)).orElse(null);
    }
    
    public String getStatusTextDisplayKey() {
        return resonanceEffectStatusTextDisplay.orElse(elementItemTooltip);
    }
}
