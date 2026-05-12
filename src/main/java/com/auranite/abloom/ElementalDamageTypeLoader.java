package com.auranite.abloom;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * Resource listener that loads custom elemental damage types from data packs.
 * Path: data/{modid}/elemental_dmg_types/<damage_type_id>.json
 */
public class ElementalDamageTypeLoader implements PreparableReloadListener {

    private static final Map<ResourceLocation, CustomElementalDamageType> LOADED_TYPES = new ConcurrentHashMap<>();
    private static final String FOLDER = "elemental_dmg_types";
    private static final Gson GSON = new GsonBuilder().create();

    public ElementalDamageTypeLoader() {
        super();
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<ResourceLocation, CustomElementalDamageType> map = new HashMap<>();
            var resources = resourceManager.listResources(FOLDER, path -> path.toString().endsWith(".json"));
            
            for (var entry : resources.entrySet()) {
                try {
                    var resource = entry.getValue();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {
                        JsonObject json = GSON.fromJson(reader, JsonObject.class);
                        
                        if (json == null) continue;
                        
                        String idString = json.has("id") ? json.get("id").getAsString() : null;
                        ResourceLocation id = idString != null ? ResourceLocation.parse(idString) : null;
                        
                        String damageTypeId = json.has("damage_type_id") ? json.get("damage_type_id").getAsString() : "";
                        
                        // Parse color, supporting both integer and hex string formats (e.g., "0xFFFF0000" or "FF0000")
                        int color = 0xFFFFFF;
                        if (json.has("color")) {
                            if (json.get("color").isJsonPrimitive()) {
                                var colorPrimitive = json.get("color").getAsJsonPrimitive();
                                if (colorPrimitive.isNumber()) {
                                    color = colorPrimitive.getAsInt();
                                } else if (colorPrimitive.isString()) {
                                    String colorStr = colorPrimitive.getAsString();
                                    if (colorStr.startsWith("0x") || colorStr.startsWith("0X")) {
                                        color = (int) Long.parseLong(colorStr.substring(2), 16);
                                    } else {
                                        color = (int) Long.parseLong(colorStr, 16);
                                    }
                                }
                            }
                        }
                        
                        Optional<ResourceLocation> resonanceEffect = json.has("resonance_effect") 
                            ? Optional.of(ResourceLocation.parse(json.get("resonance_effect").getAsString()))
                            : Optional.empty();
                        
                        int duration = json.has("resonance_effect_duration") ? json.get("resonance_effect_duration").getAsInt() : 15;
                        
                        Optional<String> itemTooltip = json.has("element_Item_tooltip")
                            ? Optional.of(json.get("element_Item_tooltip").getAsString())
                            : Optional.empty();
                        
                        Optional<String> armorTooltip = json.has("element_armor_resistance_tooltip")
                            ? Optional.of(json.get("element_armor_resistance_tooltip").getAsString())
                            : Optional.empty();
                        
                        Optional<String> statusText = json.has("resonance_effect.status_text_display")
                            ? Optional.of(json.get("resonance_effect.status_text_display").getAsString())
                            : Optional.empty();
                        
                        if (id != null && !damageTypeId.isEmpty()) {
                            var type = new CustomElementalDamageType(id, damageTypeId, color, resonanceEffect, duration, itemTooltip, armorTooltip, statusText);
                            map.put(id, type);
                        }
                    }
                } catch (Exception e) {
                    AbloomMod.LOGGER.error("Failed to parse custom damage type from: {}", entry.getKey(), e);
                }
            }
            return map;
        }, backgroundExecutor).thenCompose(preparationBarrier::wait).thenAcceptAsync(map -> {
            LOADED_TYPES.clear();
            LOADED_TYPES.putAll(map);
            
            AbloomMod.LOGGER.info("Loaded {} custom elemental damage types from data packs", map.size());
            
            for (Map.Entry<ResourceLocation, CustomElementalDamageType> entry : map.entrySet()) {
                CustomElementalDamageType type = entry.getValue();
                AbloomMod.LOGGER.debug("  - {}: {}", entry.getKey(), type);
                
                // Register the color for damage display
                ElementDamageDisplayManager.registerDamageColorFromCustom(type);
            }
        }, gameExecutor);
    }

    /**
     * Get a loaded custom elemental damage type by its ID.
     */
    public static CustomElementalDamageType get(ResourceLocation id) {
        return LOADED_TYPES.get(id);
    }

    /**
     * Get a loaded custom elemental damage type by damage_type_id string.
     */
    public static CustomElementalDamageType getByDamageTypeId(String damageTypeId) {
        if (damageTypeId == null) return null;
        
        for (CustomElementalDamageType type : LOADED_TYPES.values()) {
            if (type.getDamageTypeId().equals(damageTypeId)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Check if a damage type ID belongs to a custom elemental damage type.
     */
    public static boolean isCustomDamageType(String damageTypeId) {
        return getByDamageTypeId(damageTypeId) != null;
    }

    /**
     * Get all loaded custom elemental damage types.
     */
    public static Map<ResourceLocation, CustomElementalDamageType> getAllLoadedTypes() {
        return Map.copyOf(LOADED_TYPES);
    }

    /**
     * Clear all loaded types (for testing/reload).
     */
    public static void clear() {
        LOADED_TYPES.clear();
    }
}
