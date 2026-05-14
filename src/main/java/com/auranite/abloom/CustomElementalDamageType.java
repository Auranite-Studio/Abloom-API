package com.auranite.abloom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

import java.util.Optional;

/**
 * Represents a custom elemental damage type loaded from datapack.
 * 
 * Datapack JSON format (data/{modid}/elemental_dmg_types/{id}.json):
 * {
 *   "id": "abloom:sharp_dmg",
 *   "damage_source_id": "minecraft:generic",
 *   "color": 16483051,
 *   "resonance_effect": "minecraft:poison",
 *   "resonance_effect_duration": 8,
 *   "element_Item_tooltip": "elemental.tooltip.sharp",
 *   "element_armor_resistance_tooltip": "elemental.resistance.sharp",
 *   "resonance_effect.status_text_display": "elemental.tooltip.sharp"
 * }
 */
public class CustomElementalDamageType {
    
    public static final Codec<CustomElementalDamageType> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(CustomElementalDamageType::getId),
            ResourceLocation.CODEC.fieldOf("damage_source_id").forGetter(CustomElementalDamageType::getDamageSourceId),
            Codec.INT.fieldOf("color").forGetter(CustomElementalDamageType::getColor),
            ResourceLocation.CODEC.optionalFieldOf("resonance_effect").forGetter(CustomElementalDamageType::getResonanceEffect),
            Codec.INT.optionalFieldOf("resonance_effect_duration", 8).forGetter(CustomElementalDamageType::getResonanceEffectDuration),
            Codec.STRING.fieldOf("element_Item_tooltip").forGetter(CustomElementalDamageType::getElementItemTooltip),
            Codec.STRING.fieldOf("element_armor_resistance_tooltip").forGetter(CustomElementalDamageType::getElementArmorResistanceTooltip),
            Codec.STRING.optionalFieldOf("resonance_effect_status_text_display").forGetter(CustomElementalDamageType::getResonanceEffectStatusTextDisplay)
        ).apply(instance, CustomElementalDamageType::new)
    );

    private final ResourceLocation id;
    private final ResourceLocation damageSourceId;
    private final int color;
    private final Optional<ResourceLocation> resonanceEffect;
    private final int resonanceEffectDuration;
    private final String elementItemTooltip;
    private final String elementArmorResistanceTooltip;
    private final Optional<String> resonanceEffectStatusTextDisplay;

    public CustomElementalDamageType(ResourceLocation id,
                                     ResourceLocation damageSourceId,
                                     int color,
                                     Optional<ResourceLocation> resonanceEffect,
                                     int resonanceEffectDuration,
                                     String elementItemTooltip,
                                     String elementArmorResistanceTooltip,
                                     Optional<String> resonanceEffectStatusTextDisplay) {
        this.id = id;
        this.damageSourceId = damageSourceId;
        this.color = color;
        this.resonanceEffect = resonanceEffect;
        this.resonanceEffectDuration = resonanceEffectDuration;
        this.elementItemTooltip = elementItemTooltip;
        this.elementArmorResistanceTooltip = elementArmorResistanceTooltip;
        this.resonanceEffectStatusTextDisplay = resonanceEffectStatusTextDisplay;
    }

    public ResourceLocation getId() {
        return id;
    }

    public ResourceLocation getDamageSourceId() {
        return damageSourceId;
    }

    public int getColor() {
        return color;
    }

    public Optional<ResourceLocation> getResonanceEffect() {
        return resonanceEffect;
    }

    public int getResonanceEffectDuration() {
        return resonanceEffectDuration;
    }

    public String getElementItemTooltip() {
        return elementItemTooltip;
    }

    public String getElementArmorResistanceTooltip() {
        return elementArmorResistanceTooltip;
    }

    public Optional<String> getResonanceEffectStatusTextDisplay() {
        return resonanceEffectStatusTextDisplay;
    }

    public MobEffect getResonanceMobEffect() {
        return resonanceEffect.flatMap(loc -> 
            net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getOptional(loc)
        ).orElse(MobEffects.POISON);
    }

    @Override
    public String toString() {
        return "CustomElementalDamageType{" +
                "id=" + id +
                ", damageSourceId=" + damageSourceId +
                ", color=0x" + Integer.toHexString(color) +
                ", resonanceEffect=" + resonanceEffect.orElse(null) +
                '}';
    }
}
