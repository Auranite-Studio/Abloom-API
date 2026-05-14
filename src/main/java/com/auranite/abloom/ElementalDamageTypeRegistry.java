package com.auranite.abloom;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.profiler.Instrumentation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Registry for custom elemental damage types loaded from datapacks.
 * 
 * Datapacks can register custom elemental damage types by placing JSON files in:
 * data/{modid}/elemental_dmg_types/{id}.json
 */
@EventBusSubscriber(modid = AbloomMod.MODID)
public class ElementalDamageTypeRegistry {
    
    public static final Logger LOGGER = LogManager.getLogger(ElementalDamageTypeRegistry.class);
    
    private static final Map<ResourceLocation, CustomElementalDamageType> CUSTOM_DAMAGE_TYPES = new HashMap<>();
    private static final Map<String, ResourceLocation> DAMAGE_SOURCE_TO_CUSTOM_TYPE = new HashMap<>();
    
    // Default 11 elemental types from the original mod
    private static final String[] DEFAULT_ELEMENTAL_IDS = {
        "fire_dmg", "physical_dmg", "wind_dmg", "earth_dmg", "water_dmg",
        "ice_dmg", "electric_dmg", "energy_dmg", "natural_dmg", "quantum_dmg", "ether_dmg"
    };
    
    private ElementalDamageTypeRegistry() {}
    
    /**
     * Register a custom elemental damage type.
     */
    public static void register(CustomElementalDamageType type) {
        if (type == null) return;
        
        CUSTOM_DAMAGE_TYPES.put(type.getId(), type);
        
        // Map damage source ID to custom type for lookup
        DAMAGE_SOURCE_TO_CUSTOM_TYPE.put(
            type.getDamageSourceId().toString(), 
            type.getId()
        );
        
        // Register damage color
        ElementDamageDisplayManager.registerDamageColorFromCustom(type);
        
        LOGGER.info("Registered custom elemental damage type: {}", type);
    }
    
    /**
     * Get a custom elemental damage type by its ID.
     */
    public static Optional<CustomElementalDamageType> get(ResourceLocation id) {
        return Optional.ofNullable(CUSTOM_DAMAGE_TYPES.get(id));
    }
    
    /**
     * Get a custom elemental damage type by damage source ID.
     */
    public static Optional<CustomElementalDamageType> getByDamageSource(ResourceLocation damageSourceId) {
        ResourceLocation customId = DAMAGE_SOURCE_TO_CUSTOM_TYPE.get(damageSourceId.toString());
        if (customId != null) {
            return get(customId);
        }
        return Optional.empty();
    }
    
    /**
     * Check if a damage type ID is a custom elemental type.
     */
    public static boolean isCustomElementType(String damageTypeId) {
        if (damageTypeId == null) return false;
        String cleanId = damageTypeId.contains(":") ? damageTypeId.substring(damageTypeId.indexOf(":") + 1) : damageTypeId;
        ResourceLocation rl = ResourceLocation.tryParse(cleanId);
        if (rl == null) rl = ResourceLocation.tryParse(AbloomMod.MODID + ":" + cleanId);
        return rl != null && CUSTOM_DAMAGE_TYPES.containsKey(rl);
    }
    
    /**
     * Get custom type by damage type ID string.
     */
    public static Optional<CustomElementalDamageType> getByDamageTypeId(String damageTypeId) {
        if (damageTypeId == null) return Optional.empty();
        
        // Try with full ID first
        ResourceLocation rl = ResourceLocation.tryParse(damageTypeId);
        if (rl != null) {
            Optional<CustomElementalDamageType> result = get(rl);
            if (result.isPresent()) return result;
        }
        
        // Try with just path
        String cleanId = damageTypeId.contains(":") ? damageTypeId.substring(damageTypeId.indexOf(":") + 1) : damageTypeId;
        rl = ResourceLocation.tryParse(AbloomMod.MODID + ":" + cleanId);
        if (rl != null) {
            return get(rl);
        }
        
        return Optional.empty();
    }
    
    /**
     * Check if this is one of the original 11 elemental types.
     */
    public static boolean isDefaultElementType(String damageTypeId) {
        if (damageTypeId == null) return false;
        String cleanId = damageTypeId.contains(":") ? damageTypeId.substring(damageTypeId.indexOf(":") + 1) : damageTypeId;
        for (String defaultId : DEFAULT_ELEMENTAL_IDS) {
            if (defaultId.equals(cleanId)) return true;
        }
        return false;
    }
    
    /**
     * Clear all registered custom damage types.
     */
    public static void clear() {
        CUSTOM_DAMAGE_TYPES.clear();
        DAMAGE_SOURCE_TO_CUSTOM_TYPE.clear();
        LOGGER.info("Cleared custom elemental damage types registry");
    }
    
    /**
     * Get count of registered custom damage types.
     */
    public static int getCount() {
        return CUSTOM_DAMAGE_TYPES.size();
    }
    
    /**
     * Reload listener for datapack synchronization.
     */
    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new ElementalDamageTypeReloadListener());
    }
    
    /**
     * Called when server is starting and resources are ready.
     */
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel().isClientSide()) {
            // Client-side initialization if needed
        }
    }
    
    /**
     * Called when datapacks are synced to clients.
     */
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        LOGGER.debug("Datapack sync event - {} custom elemental damage types registered", CUSTOM_DAMAGE_TYPES.size());
    }
    
    /**
     * Reload listener implementation.
     */
    private static class ElementalDamageTypeReloadListener implements net.minecraft.server.packs.resources.SimplePreparableReloadListener<Map<ResourceLocation, CustomElementalDamageType>> {
        
        @Override
        protected Map<ResourceLocation, CustomElementalDamageType> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
            LOGGER.info("Preparing reload of custom elemental damage types...");
            Map<ResourceLocation, CustomElementalDamageType> loadedTypes = new HashMap<>();
            
            // Find all JSON files in elemental_dmg_types folders
            String folderPath = "elemental_dmg_types";
            
            for (String namespace : resourceManager.getNamespaces()) {
                try {
                    var resources = resourceManager.listResourceStacks(folderPath, path -> path.getPath().endsWith(".json"));
                    
                    for (var entry : resources.entrySet()) {
                        ResourceLocation loc = entry.getKey();
                        var stack = entry.getValue();
                        
                        if (stack.isEmpty()) continue;
                        
                        var resource = stack.getFirst();
                        try (var reader = resource.openAsReader()) {
                            var json = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
                            
                            // Parse using codec
                            var codecResult = CustomElementalDamageType.CODEC.parse(
                                com.mojang.serialization.JsonOps.INSTANCE, json
                            );
                            
                            codecResult.result().ifPresent(type -> {
                                loadedTypes.put(type.getId(), type);
                                LOGGER.debug("Loaded custom damage type: {}", type);
                            });
                            
                            codecResult.error().ifPresent(err -> 
                                LOGGER.warn("Failed to parse custom damage type {}: {}", loc, err.message())
                            );
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn("Error loading custom damage types from namespace {}: {}", namespace, e.getMessage());
                }
            }
            
            return loadedTypes;
        }
        
        @Override
        protected void apply(Map<ResourceLocation, CustomElementalDamageType> loadedTypes, ResourceManager resourceManager, ProfilerFiller profiler) {
            LOGGER.info("Applying {} custom elemental damage types...", loadedTypes.size());
            
            // Clear old entries
            clear();
            
            // Register new entries
            for (CustomElementalDamageType type : loadedTypes.values()) {
                register(type);
            }
            
            LOGGER.info("Successfully loaded {} custom elemental damage types", CUSTOM_DAMAGE_TYPES.size());
        }
    }
}
