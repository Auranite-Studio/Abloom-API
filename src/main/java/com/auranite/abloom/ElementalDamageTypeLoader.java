package com.auranite.abloom;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * Resource listener that loads custom elemental damage types from data packs.
 * Path: data/{modid}/elemental_dmg_types/<damage_type_id>.json
 */
public class ElementalDamageTypeLoader extends SimplePreparableReloadListener<Map<ResourceLocation, CustomElementalDamageType>> {

    private static final Map<ResourceLocation, CustomElementalDamageType> LOADED_TYPES = new ConcurrentHashMap<>();
    private static final String FOLDER = "elemental_dmg_types";

    public ElementalDamageTypeLoader() {
        super();
    }

    @Override
    protected CompletableFuture<Map<ResourceLocation, CustomElementalDamageType>> prepare(ResourceManager resourceManager, Executor executor, ProfilerFiller profiler) {
        return CompletableFuture.supplyAsync(() -> {
            Map<ResourceLocation, CustomElementalDamageType> map = new HashMap<>();
            var resources = resourceManager.listResources(FOLDER, path -> path.toString().endsWith(".json"));
            
            for (var entry : resources.entrySet()) {
                try {
                    var resource = entry.getValue();
                    var json = com.google.gson.JsonParser.parseString(resource.openAsReader().readString()).getAsJsonObject();
                    
                    var id = ResourceLocation.tryParse(json.get("id").getAsString());
                    var damageTypeId = json.get("damage_type_id").getAsString();
                    int color = json.has("color") ? json.get("color").getAsInt() : 0xFFFFFF;
                    
                    var resonanceEffect = json.has("resonance_effect") 
                        ? java.util.Optional.of(ResourceLocation.tryParse(json.get("resonance_effect").getAsString()))
                        : java.util.Optional.empty();
                    
                    int duration = json.has("resonance_effect_duration") ? json.get("resonance_effect_duration").getAsInt() : 15;
                    
                    var itemTooltip = json.has("element_Item_tooltip")
                        ? java.util.Optional.of(json.get("element_Item_tooltip").getAsString())
                        : java.util.Optional.empty();
                    
                    var armorTooltip = json.has("element_armor_resistance_tooltip")
                        ? java.util.Optional.of(json.get("element_armor_resistance_tooltip").getAsString())
                        : java.util.Optional.empty();
                    
                    var statusText = json.has("resonance_effect.status_text_display")
                        ? java.util.Optional.of(json.get("resonance_effect.status_text_display").getAsString())
                        : java.util.Optional.empty();
                    
                    if (id != null) {
                        var type = new CustomElementalDamageType(id, damageTypeId, color, resonanceEffect, duration, itemTooltip, armorTooltip, statusText);
                        map.put(id, type);
                    }
                } catch (Exception e) {
                    AbloomMod.LOGGER.error("Failed to parse custom damage type from: {}", entry.getKey(), e);
                }
            }
            return map;
        }, executor);
    }

    @Override
    protected void apply(Map<ResourceLocation, CustomElementalDamageType> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        LOADED_TYPES.clear();
        LOADED_TYPES.putAll(objects);
        
        AbloomMod.LOGGER.info("Loaded {} custom elemental damage types from data packs", objects.size());
        
        for (Map.Entry<ResourceLocation, CustomElementalDamageType> entry : objects.entrySet()) {
            CustomElementalDamageType type = entry.getValue();
            AbloomMod.LOGGER.debug("  - {}: {}", entry.getKey(), type);
            
            // Register the color for damage display
            ElementDamageDisplayManager.registerDamageColorFromCustom(type);
        }
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
