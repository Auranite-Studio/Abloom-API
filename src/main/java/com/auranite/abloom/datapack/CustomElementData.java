package com.auranite.abloom.datapack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * Immutable data representation of a custom elemental type loaded from a datapack JSON file.
 * 
 * <p>JSON format example:
 * <pre>{@code
 * {
 *   "element_id": "yourmod:custom_element",
 *   "override_damage_types": ["yourmod:custom_element_damage_source", "minecraft:generic"],
 *   "color": 2891628,
 *   "element_translation_key": "element.yourmod.custom",
 *   "resonance_translation_key": "resonance_text.yourmod.custom",
 *   "can_resonance_accumulation": true,
 *   "resonance_dmg_multiplier": 2.0,
 *   "resonance_effect": {
 *     "effect": "yourmod:custom_resonance_effect",
 *     "config": {
 *       "duration": 100,
 *       "amplifier": 0
 *     }
 *   }
 * }
 * }</pre>
 */
public record CustomElementData(
        ResourceLocation elementId,
        ResourceLocation[] overrideDamageTypes,
        int color,
        @Nullable String elementTranslationKey,
        @Nullable String resonanceTranslationKey,
        boolean canResonanceAccumulation,
        double resonanceDmgMultiplier,
        @Nullable ResonanceEffectConfig resonanceEffect
) {
    /**
     * Configuration for the resonance effect triggered at 100 accumulation points.
     */
    public record ResonanceEffectConfig(
            ResourceLocation effect,
            int duration,
            int amplifier
    ) {
        public ResonanceEffectConfig {
            if (effect == null) throw new IllegalArgumentException("Resonance effect must not be null");
            if (duration <= 0) throw new IllegalArgumentException("Duration must be positive");
            if (amplifier < 0) throw new IllegalArgumentException("Amplifier must be non-negative");
        }
    }

    /**
     * Builder for constructing {@link CustomElementData} instances.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the name part of elementId (without namespace).
     * Used as the enum-style name for CustomElementType (e.g., "CUSTOM_ELEMENT").
     */
    public String getElementName() {
        return elementId.getPath().toUpperCase(java.util.Locale.ROOT);
    }

    /**
     * Returns the damage type ID path (without namespace).
     * Used as the damageTypeId for CustomElementType (e.g., "custom_element_dmg").
     */
    public String getDamageTypeId() {
        return elementId.getPath() + "_dmg";
    }

    /**
     * Returns the display name for this element.
     * Falls back to element name if no translation key is provided.
     */
    public String getDisplayName() {
        if (elementTranslationKey != null) {
            // Extract display name from translation key (e.g., "element.yourmod.custom" -> "Custom")
            String[] parts = elementTranslationKey.split("\\.");
            if (parts.length >= 3) {
                String rawName = parts[2];
                return Character.toUpperCase(rawName.charAt(0)) + rawName.substring(1);
            }
        }
        return getElementName().toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Returns the raw element translation key for use with Component.translatable().
     * @return the elementTranslationKey, or null if not set
     */
    @Nullable
    public String getRawElementTranslationKey() {
        return elementTranslationKey;
    }

    /**
     * Returns the raw resonance translation key for use with Component.translatable().
     * @return the resonanceTranslationKey, or null if not set
     */
    @Nullable
    public String getRawResonanceTranslationKey() {
        return resonanceTranslationKey;
    }

    /**
     * Parses a {@link CustomElementData} from a JSON object.
     * 
     * @param json the JSON object to parse
     * @return the parsed CustomElementData instance
     * @throws JsonParseException if required fields are missing or invalid
     */
    public static CustomElementData fromJson(JsonObject json) {
        if (json == null) {
            throw new IllegalArgumentException("JSON object must not be null");
        }

        // Parse element_id (required)
        ResourceLocation elementId;
        if (json.has("element_id") && !json.get("element_id").isJsonNull()) {
            String elementIdStr = json.get("element_id").getAsString();
            elementId = ResourceLocation.tryParse(elementIdStr);
            if (elementId == null) {
                throw new IllegalArgumentException("Invalid element_id: " + elementIdStr);
            }
        } else {
            throw new IllegalArgumentException("Missing required field: element_id");
        }

        // Parse override_damage_types (optional)
        ResourceLocation[] overrideDamageTypes = new ResourceLocation[0];
        if (json.has("override_damage_types") && !json.get("override_damage_types").isJsonNull()) {
            JsonArray damageTypesArray = json.get("override_damage_types").getAsJsonArray();
            if (damageTypesArray != null && !damageTypesArray.isEmpty()) {
                overrideDamageTypes = new ResourceLocation[damageTypesArray.size()];
                for (int i = 0; i < damageTypesArray.size(); i++) {
                    String damageTypeStr = damageTypesArray.get(i).getAsString();
                    overrideDamageTypes[i] = ResourceLocation.tryParse(damageTypeStr);
                    if (overrideDamageTypes[i] == null) {
                        throw new IllegalArgumentException("Invalid damage type in override_damage_types: " + damageTypeStr);
                    }
                }
            }
        }

        // Parse color (optional, defaults to 0xFFFFFF)
        int color = 0xFFFFFF;
        if (json.has("color") && !json.get("color").isJsonNull()) {
            JsonElement colorElem = json.get("color");
            if (colorElem.isJsonPrimitive()) {
                String colorStr = colorElem.getAsString();
                // Support both decimal (16777215) and hex (0xFFFFFF) format
                if (colorStr.startsWith("0x") || colorStr.startsWith("0X")) {
                    color = Integer.decode(colorStr);
                } else {
                    color = Integer.parseInt(colorStr);
                }
            } else {
                color = colorElem.getAsInt();
            }
        }

        // Parse element_translation_key (optional)
        String elementTranslationKey = null;
        if (json.has("element_translation_key") && !json.get("element_translation_key").isJsonNull()) {
            elementTranslationKey = json.get("element_translation_key").getAsString();
        }

        // Parse resonance_translation_key (optional)
        String resonanceTranslationKey = null;
        if (json.has("resonance_translation_key") && !json.get("resonance_translation_key").isJsonNull()) {
            resonanceTranslationKey = json.get("resonance_translation_key").getAsString();
        }

        // Parse can_resonance_accumulation (optional, defaults to true)
        boolean canResonanceAccumulation = true;
        if (json.has("can_resonance_accumulation") && !json.get("can_resonance_accumulation").isJsonNull()) {
            canResonanceAccumulation = json.get("can_resonance_accumulation").getAsBoolean();
        }

        // Parse resonance_dmg_multiplier (optional, defaults to 1.0)
        double resonanceDmgMultiplier = 1.0;
        if (json.has("resonance_dmg_multiplier") && !json.get("resonance_dmg_multiplier").isJsonNull()) {
            resonanceDmgMultiplier = json.get("resonance_dmg_multiplier").getAsDouble();
        }

        // Parse resonance_effect (optional)
        ResonanceEffectConfig resonanceEffect = null;
        if (json.has("resonance_effect") && !json.get("resonance_effect").isJsonNull()) {
            resonanceEffect = parseResonanceEffect(json.get("resonance_effect").getAsJsonObject());
        }

        return new CustomElementData(
                elementId,
                overrideDamageTypes,
                color,
                elementTranslationKey,
                resonanceTranslationKey,
                canResonanceAccumulation,
                resonanceDmgMultiplier,
                resonanceEffect
        );
    }

    private static ResonanceEffectConfig parseResonanceEffect(JsonObject resonanceJson) {
        if (resonanceJson == null) {
            return null;
        }

        // Parse effect (required)
        ResourceLocation effect;
        if (resonanceJson.has("effect") && !resonanceJson.get("effect").isJsonNull()) {
            String effectStr = resonanceJson.get("effect").getAsString();
            effect = ResourceLocation.tryParse(effectStr);
            if (effect == null) {
                throw new IllegalArgumentException("Invalid resonance effect: " + effectStr);
            }
        } else {
            throw new IllegalArgumentException("Missing required field in resonance_effect: effect");
        }

        // Parse config (required)
        JsonObject config;
        if (resonanceJson.has("config") && !resonanceJson.get("config").isJsonNull()) {
            config = resonanceJson.get("config").getAsJsonObject();
        } else {
            throw new IllegalArgumentException("Missing required field in resonance_effect: config");
        }

        // Parse duration (required)
        int duration;
        if (config.has("duration") && !config.get("duration").isJsonNull()) {
            duration = config.get("duration").getAsInt();
            if (duration <= 0) {
                throw new IllegalArgumentException("Duration must be positive: " + duration);
            }
        } else {
            throw new IllegalArgumentException("Missing required field in resonance_effect.config: duration");
        }

        // Parse amplifier (required)
        int amplifier;
        if (config.has("amplifier") && !config.get("amplifier").isJsonNull()) {
            amplifier = config.get("amplifier").getAsInt();
            if (amplifier < 0) {
                throw new IllegalArgumentException("Amplifier must be non-negative: " + amplifier);
            }
        } else {
            throw new IllegalArgumentException("Missing required field in resonance_effect.config: amplifier");
        }

        return new ResonanceEffectConfig(effect, duration, amplifier);
    }

    /**
     * Builder class for constructing {@link CustomElementData} instances.
     */
    public static class Builder {
        private ResourceLocation elementId;
        private ResourceLocation[] overrideDamageTypes = new ResourceLocation[0];
        private int color = 0xFFFFFF;
        private String elementTranslationKey = null;
        private String resonanceTranslationKey = null;
        private boolean canResonanceAccumulation = true;
        private double resonanceDmgMultiplier = 1.0;
        private ResonanceEffectConfig resonanceEffect = null;

        public Builder elementId(ResourceLocation elementId) {
            this.elementId = elementId;
            return this;
        }

        public Builder overrideDamageTypes(ResourceLocation... overrideDamageTypes) {
            this.overrideDamageTypes = overrideDamageTypes != null ? overrideDamageTypes : new ResourceLocation[0];
            return this;
        }

        public Builder color(int color) {
            this.color = color;
            return this;
        }

        public Builder elementTranslationKey(String elementTranslationKey) {
            this.elementTranslationKey = elementTranslationKey;
            return this;
        }

        public Builder resonanceTranslationKey(String resonanceTranslationKey) {
            this.resonanceTranslationKey = resonanceTranslationKey;
            return this;
        }

        public Builder canResonanceAccumulation(boolean canResonanceAccumulation) {
            this.canResonanceAccumulation = canResonanceAccumulation;
            return this;
        }

        public Builder resonanceDmgMultiplier(double resonanceDmgMultiplier) {
            this.resonanceDmgMultiplier = resonanceDmgMultiplier;
            return this;
        }

        public Builder resonanceEffect(ResonanceEffectConfig resonanceEffect) {
            this.resonanceEffect = resonanceEffect;
            return this;
        }

        public CustomElementData build() {
            if (elementId == null) {
                throw new IllegalArgumentException("elementId must not be null");
            }
            return new CustomElementData(
                    elementId,
                    overrideDamageTypes,
                    color,
                    elementTranslationKey,
                    resonanceTranslationKey,
                    canResonanceAccumulation,
                    resonanceDmgMultiplier,
                    resonanceEffect
            );
        }
    }
}