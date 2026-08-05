package com.auranite.abloom.datapack;

import com.auranite.abloom.ElementType;
import com.auranite.abloom.AbloomMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data class representing elemental weapon configuration from datapack JSON.
 * Supports both legacy single-element format and new multi-stage attack format.
 */
public class ElementalWeaponData {

    /** Maximum number of attack stages */
    public static final int MAX_STAGES = 4;

    private final String item;
    private final String element; // fallback display element (used only if stages are defined)
    private final float accumulationMultiplier; // total accumulation for tooltip display
    private final float critChance;
    private final float critDamage;

    private final List<WeaponStage> stages;

    /**
     * Represents a single attack stage with its element and accumulation multiplier.
     */
    public static class WeaponStage {
        private final int stageNumber;
        private final String elementType;
        private final float accumulationMultiplier;

        public WeaponStage(int stageNumber, String elementType, float accumulationMultiplier) {
            this.stageNumber = stageNumber;
            this.elementType = elementType;
            this.accumulationMultiplier = accumulationMultiplier;
        }

        public int getStageNumber() {
            return stageNumber;
        }

        public String getElementTypeString() {
            return elementType;
        }

        public ElementType getElementType() {
            return ElementType.safeValueOf(elementType.toUpperCase());
        }

        public float getAccumulationMultiplier() {
            return accumulationMultiplier;
        }
    }

    // Per-UUID stage tracking for multi-stage attacks
    private final Map<UUID, Integer> stageProgress = new ConcurrentHashMap<>();

    public ElementalWeaponData(String item, String element, float accumulationMultiplier, float critChance, float critDamage) {
        this(item, element, accumulationMultiplier, critChance, critDamage, Collections.emptyList());
    }

    public ElementalWeaponData(String item, String element, float accumulationMultiplier, float critChance, float critDamage, List<WeaponStage> stages) {
        this.item = item;
        this.element = element;
        this.accumulationMultiplier = accumulationMultiplier;
        this.critChance = critChance;
        this.critDamage = critDamage;
        this.stages = stages;
    }

    public String getItem() {
        return item;
    }

    public String getElement() {
        return element;
    }

    public float getAccumulationMultiplier() {
        return accumulationMultiplier;
    }

    public float getCritChance() {
        return critChance;
    }

    public float getCritDamage() {
        return critDamage;
    }

    public Optional<ResourceLocation> getItemResourceLocation() {
        try {
            return Optional.of(ResourceLocation.parse(item));
        } catch (Exception e) {
            AbloomMod.LOGGER.warn("Invalid item registry name: {}", item, e);
            return Optional.empty();
        }
    }

    public Optional<ElementType> getElementType() {
        if (hasStages()) {
            return Optional.empty(); // stages override the fallback element
        }
        ElementType result = ElementType.safeValueOf(element);
        return result != null ? Optional.of(result) : Optional.empty();
    }

    public boolean hasStages() {
        return !stages.isEmpty();
    }

    public List<WeaponStage> getStages() {
        return Collections.unmodifiableList(stages);
    }

    public WeaponStage getCurrentStage() {
        if (!hasStages()) return null;
        return stages.get(0); // Stage 1 is first
    }

    public WeaponStage getStageByNumber(int stageNumber) {
        return stages.stream()
                .filter(s -> s.getStageNumber() == stageNumber)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the current stage for a specific entity that is being hit by this weapon.
     * Used for tracking progress through multi-stage attacks.
     */
    public WeaponStage getCurrentStageForEntity(UUID entityId) {
        if (!hasStages()) return null;
        Integer stageIndex = stageProgress.get(entityId);
        int stageNum = (stageIndex != null) ? stageIndex + 1 : 0;
        if (stageNum >= stages.size()) {
            stageNum = 0; // reset to first stage after completing all
        }
        return stages.get(stageNum);
    }

    /**
     * Advance the stage for a specific entity after a hit.
     */
    public void advanceStageForEntity(UUID entityId) {
        if (!hasStages()) return;
        Integer current = stageProgress.getOrDefault(entityId, 0);
        int next = Math.min(current + 1, stages.size() - 1);
        stageProgress.put(entityId, next);
    }

    /**
     * Reset the stage progress for a specific entity.
     */
    public void resetStageForEntity(UUID entityId) {
        stageProgress.remove(entityId);
    }

    /**
     * Parse ElementalWeaponData from JSON object.
     * Supports both legacy format (element, accumulation_multiplier, etc.)
     * and new multi-stage format (stages object).
     */
    public static ElementalWeaponData fromJson(JsonObject json) {
        String item = GsonHelper.getAsString(json, "item");
        String element = GsonHelper.getAsString(json, "element", "PHYSICAL");
        float accumulationMultiplier = GsonHelper.getAsFloat(json, "accumulation_multiplier", 1.0f);
        float critChance = GsonHelper.getAsFloat(json, "crit_chance", 0.0f);
        float critDamage = GsonHelper.getAsFloat(json, "crit_damage", 0.0f);

        // Check for stages object
        List<WeaponStage> stages = Collections.emptyList();
        if (json.has("stages")) {
            JsonElement stagesElem = json.get("stages");
            if (stagesElem.isJsonArray()) {
                // Legacy array format: [ {...}, {...}, ... ]
                stages = parseStagesArray(stagesElem.getAsJsonArray());
            } else if (stagesElem.isJsonObject()) {
                // New object format: { "1": {...}, "2": {...}, ... }
                stages = parseStagesObject(stagesElem.getAsJsonObject());
            }
        }

        return new ElementalWeaponData(item, element, accumulationMultiplier, critChance, critDamage, stages);
    }

    private static List<WeaponStage> parseStagesArray(JsonArray stagesArray) {
        List<WeaponStage> stageList = new ArrayList<>();
        for (int i = 0; i < stagesArray.size() && i < MAX_STAGES; i++) {
            JsonObject stageData = stagesArray.get(i).getAsJsonObject();
            String element = GsonHelper.getAsString(stageData, "element", "PHYSICAL");
            float accumMultiplier = GsonHelper.getAsFloat(stageData, "accumulation_multiplier", 1.0f);
            stageList.add(new WeaponStage(i + 1, element, accumMultiplier));
        }
        return stageList;
    }

    private static List<WeaponStage> parseStagesObject(JsonObject stagesObj) {
        List<WeaponStage> stageList = new ArrayList<>();

        for (String stageKey : stagesObj.keySet()) {
            try {
                int stageNumber = Integer.parseInt(stageKey);
                if (stageNumber < 1 || stageNumber > MAX_STAGES) {
                    AbloomMod.LOGGER.warn("Invalid stage number {} for weapon, must be 1-{}, skipping", stageNumber, MAX_STAGES);
                    continue;
                }

                JsonObject stageData = stagesObj.getAsJsonObject(stageKey);
                String element = GsonHelper.getAsString(stageData, "element", "PHYSICAL");
                float accumMultiplier = GsonHelper.getAsFloat(stageData, "accumulation_multiplier", 1.0f);

                if (ElementType.safeValueOf(element.toUpperCase()) == null) {
                    AbloomMod.LOGGER.warn("Unknown element type '{}' for stage {} of weapon, defaulting to PHYSICAL", element, stageNumber);
                }

                stageList.add(new WeaponStage(stageNumber, element, accumMultiplier));
            } catch (NumberFormatException e) {
                AbloomMod.LOGGER.warn("Invalid stage key '{}' in stages object, skipping", stageKey);
            }
        }

        // Sort stages by number
        stageList.sort((a, b) -> Integer.compare(a.getStageNumber(), b.getStageNumber()));
        if (stageList.size() > MAX_STAGES) {
            AbloomMod.LOGGER.warn("Weapon has more than {} stages, truncating to {}", MAX_STAGES, MAX_STAGES);
            stageList = stageList.subList(0, MAX_STAGES);
        }

        return stageList;
    }
}
