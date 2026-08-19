package com.auranite.abloom.datapack;

import com.auranite.abloom.util.ElementType;
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
 * 
 * <p><b>Element resolution rules:</b></p>
 * <ul>
 *   <li><b>base_element</b>: primary element for tooltip display and general use (applies to all weapons)</li>
 *   <li><b>stages[].element</b>: element used for damage calculation for each specific stage</li>
 * </ul>
 */
public class ElementalWeaponData {

    /** Maximum number of attack stages */
    public static final int MAX_STAGES = 4;

    private final String item;
    private final String baseElement; // primary element for tooltip display and general use
    private final float accumulationMultiplier; // total accumulation for tooltip display
    private final float critChance;
    private final float critDamage;

    private final List<WeaponStage> stages;

    /**
     * Represents a single attack stage with its element and accumulation multiplier.
     */
    public static class WeaponStage {
        private final int stageNumber;
        private final String stageElement; // element for damage calculation of this stage
        private final float accumulationMultiplier;

        public WeaponStage(int stageNumber, String stageElement, float accumulationMultiplier) {
            this.stageNumber = stageNumber;
            this.stageElement = stageElement;
            this.accumulationMultiplier = accumulationMultiplier;
        }

        public int getStageNumber() {
            return stageNumber;
        }

        public String getStageElementString() {
            return stageElement;
        }

        public ElementType getStageElementType() {
            return ElementType.safeValueOf(stageElement.toUpperCase());
        }

        public float getAccumulationMultiplier() {
            return accumulationMultiplier;
        }
    }

    // Per-UUID stage tracking for multi-stage attacks
    private final Map<UUID, Integer> stageProgress = new ConcurrentHashMap<>();

    public ElementalWeaponData(String item, String baseElement, float accumulationMultiplier, float critChance, float critDamage) {
        this(item, baseElement, accumulationMultiplier, critChance, critDamage, Collections.emptyList());
    }

    public ElementalWeaponData(String item, String baseElement, float accumulationMultiplier, float critChance, float critDamage, List<WeaponStage> stages) {
        this.item = item;
        this.baseElement = baseElement;
        this.accumulationMultiplier = accumulationMultiplier;
        this.critChance = critChance;
        this.critDamage = critDamage;
        this.stages = stages;
    }

    public String getItem() {
        return item;
    }

    public String getBaseElement() {
        return baseElement;
    }

    public Optional<ElementType> getBaseElementType() {
        if (baseElement == null || baseElement.isEmpty()) {
            return Optional.empty();
        }
        ElementType result = ElementType.safeValueOf(baseElement.toUpperCase());
        return result != null ? Optional.of(result) : Optional.empty();
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

    /**
     * Gets the base element type for this weapon.
     * Used for tooltip display and general element determination.
     */
    public Optional<ElementType> getElementType() {
        return getBaseElementType();
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
     * Supports both legacy format (base_element) and multi-stage format (stages array or object).
     * 
     * <p>Element resolution:</p>
     * <ul>
     *   <li><b>base_element</b>: primary element for tooltip display (required)</li>
     *   <li><b>stages[].element</b>: element used per-stage for damage calculation</li>
     * </ul>
     */
    public static ElementalWeaponData fromJson(JsonObject json) {
        String item = GsonHelper.getAsString(json, "item");
        
        // base_element can be on top level OR inside stages object
        String baseElement = null;
        if (json.has("base_element")) {
            baseElement = GsonHelper.getAsString(json, "base_element", null);
        } else if (json.has("stages") && json.get("stages").isJsonObject()) {
            JsonObject stagesObj = json.getAsJsonObject("stages");
            if (stagesObj.has("base_element")) {
                baseElement = GsonHelper.getAsString(stagesObj, "base_element", null);
            }
        }
        if (baseElement == null || baseElement.isEmpty()) {
            baseElement = "PHYSICAL";
        }

        // Read crit_chance and crit_damage - can be on top level OR inside stages object
        float critChance = GsonHelper.getAsFloat(json, "crit_chance", 0.0f);
        float critDamage = GsonHelper.getAsFloat(json, "crit_damage", 0.0f);
        if (critChance == 0.0f && critDamage == 0.0f && json.has("stages") && json.get("stages").isJsonObject()) {
            JsonObject stagesObj = json.getAsJsonObject("stages");
            if (stagesObj.has("crit_chance")) critChance = GsonHelper.getAsFloat(stagesObj, "crit_chance", 0.0f);
            if (stagesObj.has("crit_damage")) critDamage = GsonHelper.getAsFloat(stagesObj, "crit_damage", 0.0f);
        }

        // Create stages list
        List<WeaponStage> stages = Collections.emptyList();
        if (json.has("stages")) {
            JsonElement stagesElem = json.get("stages");
            if (stagesElem.isJsonArray()) {
                // Array format: [ {...}, {...}, ... ]
                stages = parseStagesArray(stagesElem.getAsJsonArray());
            } else if (stagesElem.isJsonObject()) {
                // Object format: { "1": {...}, "2": {...}, ... }
                stages = parseStagesObject(stagesElem.getAsJsonObject());
            }
        }

        return new ElementalWeaponData(item, baseElement,
                GsonHelper.getAsFloat(json, "accumulation_multiplier", 1.0f),
                critChance,
                critDamage,
                stages);
    }

    private static List<WeaponStage> parseStagesArray(JsonArray stagesArray) {
        List<WeaponStage> stageList = new ArrayList<>();
        for (int i = 0; i < stagesArray.size() && i < MAX_STAGES; i++) {
            JsonObject stageData = stagesArray.get(i).getAsJsonObject();
            String stageElement = GsonHelper.getAsString(stageData, "element", "PHYSICAL");
            float accumMultiplier = GsonHelper.getAsFloat(stageData, "accumulation_multiplier", 1.0f);
            stageList.add(new WeaponStage(i + 1, stageElement, accumMultiplier));
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
                String stageElement = GsonHelper.getAsString(stageData, "element", "PHYSICAL");
                float accumMultiplier = GsonHelper.getAsFloat(stageData, "accumulation_multiplier", 1.0f);

                if (ElementType.safeValueOf(stageElement.toUpperCase()) == null) {
                    AbloomMod.LOGGER.warn("Unknown element type '{}' for stage {} of weapon, defaulting to PHYSICAL", stageElement, stageNumber);
                }

                stageList.add(new WeaponStage(stageNumber, stageElement, accumMultiplier));
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
