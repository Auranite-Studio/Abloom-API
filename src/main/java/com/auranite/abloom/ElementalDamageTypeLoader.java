package com.auranite.abloom;

import com.mojang.serialization.JsonCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonCodecResourceListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resource listener that loads custom elemental damage types from data packs.
 * Path: data/{modid}/elemental_dmg_types/<damage_type_id>.json
 */
public class ElementalDamageTypeLoader extends SimpleJsonCodecResourceListener<CustomElementalDamageType> {

    private static final Map<ResourceLocation, CustomElementalDamageType> LOADED_TYPES = new ConcurrentHashMap<>();

    public ElementalDamageTypeLoader() {
        super(CustomElementalDamageType.CODEC, "elemental_dmg_types");
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
