package com.auranite.abloom.datapack;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.util.ElementType;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

/**
 * Data class representing a custom element type configuration from datapack JSON.
 * Allows mods to add custom elemental damage types without modifying the base ElementType enum.
 */
public class CustomElementData {
    private final String elementId;
    private final ResourceLocation damageType;
    private final int color;
    private final String basedElementKey;
    private final String elementTranslationKey;
    private final String resonanceTranslationKey;
    private final boolean canResonanceAccumulation;
    private final ResonanceEffectData effectData;

    /**
     * Represents the resonance effect configuration for a custom element.
     */
    public static class ResonanceEffectData {
        private final ResourceLocation effectType;
        private final int duration;
        private final int amplifier;

        public ResonanceEffectData(ResourceLocation effectType, int duration, int amplifier) {
            this.effectType = effectType;
            this.duration = duration;
            this.amplifier = amplifier;
        }

        public ResourceLocation getEffectType() {
            return effectType;
        }

        public int getDuration() {
            return duration;
        }

        public int getAmplifier() {
            return amplifier;
        }
    }

    public CustomElementData(String elementId, ResourceLocation damageType, int color,
                             String basedElementKey, String elementTranslationKey,
                             String resonanceTranslationKey, boolean canResonanceAccumulation,
                             ResonanceEffectData effectData) {
        this.elementId = elementId;
        this.damageType = damageType;
        this.color = color;
        this.basedElementKey = basedElementKey;
        this.elementTranslationKey = elementTranslationKey;
        this.resonanceTranslationKey = resonanceTranslationKey;
        this.canResonanceAccumulation = canResonanceAccumulation;
        this.effectData = effectData;
    }

    public String getElementId() {
        return elementId;
    }

    public ResourceLocation getDamageType() {
        return damageType;
    }

    public int getColor() {
        return color;
    }

    public String getBasedElementKey() {
        return basedElementKey;
    }

    public String getElementTranslationKey() {
        return elementTranslationKey;
    }

    public String getResonanceTranslationKey() {
        return resonanceTranslationKey;
    }

    public boolean canResonanceAccumulation() {
        return canResonanceAccumulation;
    }

    public ResonanceEffectData getEffectData() {
        return effectData;
    }

    /**
     * Parse CustomElementData from JSON object.
     * Expected JSON format:
     * <pre>{@code
     * {
     *   "element_id": "CUSTOM_ELEMENT",
     *   "damage_type": "yourmod:custom_element",
     *   "color": 0x2B004C,
     *   "based_elemental_dmg": "FIRE",
     *   "element_translation_key": "element.yourmod.custom",
     *   "resonance_translation_key": "resonance_text.yourmod.custom",
     *   "can_resonance_accumulation": true,
     *   "effect": {
     *     "type": "yourmod:custom_resonance_effect",
     *     "config": {
     *       "duration": 100,
     *       "amplifier": 0
     *     }
     *   }
     * }
     * }</pre>
     */
    public static CustomElementData fromJson(JsonObject json) {
        String elementId = GsonHelper.getAsString(json, "element_id");
        String damageTypeStr = GsonHelper.getAsString(json, "damage_type");
        int color = GsonHelper.getAsInt(json, "color", 0xFFFFFF);
        String basedElementKey = GsonHelper.getAsString(json, "based_elemental_dmg", "PHYSICAL");
        String elementTranslationKey = GsonHelper.getAsString(json, "element_translation_key", "");
        String resonanceTranslationKey = GsonHelper.getAsString(json, "resonance_translation_key", "");
        boolean canResonanceAccumulation = GsonHelper.getAsBoolean(json, "can_resonance_accumulation", true);

        ResourceLocation damageType = null;
        try {
            damageType = ResourceLocation.parse(damageTypeStr);
        } catch (Exception e) {
            AbloomMod.LOGGER.error("Invalid damage_type '{}' for custom element '{}'", damageTypeStr, elementId, e);
        }

        ResonanceEffectData effectData = null;
        if (json.has("effect")) {
            JsonObject effectObj = json.getAsJsonObject("effect");
            String effectTypeStr = GsonHelper.getAsString(effectObj, "type", "");
            ResourceLocation effectType = null;
            try {
                effectType = ResourceLocation.parse(effectTypeStr);
            } catch (Exception e) {
                AbloomMod.LOGGER.warn("Invalid effect type '{}' for custom element '{}'", effectTypeStr, elementId);
            }

            JsonObject configObj = GsonHelper.getAsObject(effectObj, "config", new JsonObject());
            int duration = GsonHelper.getAsInt(configObj, "duration", 100);
            int amplifier = GsonHelper.getAsInt(configObj, "amplifier", 0);

            effectData = new ResonanceEffectData(effectType, duration, amplifier);
        }

        return new CustomElementData(elementId, damageType, color, basedElementKey,
                elementTranslationKey, resonanceTranslationKey, canResonanceAccumulation, effectData);
    }

    @Override
    public String toString() {
        return String.format("CustomElementData{id='%s', damageType=%s, color=0x%X}", 
                elementId, damageType, color);
    }
}
