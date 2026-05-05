package com.auranite.abloom.datapack;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.CustomElementRegistry;
import com.auranite.abloom.ElementResistanceManager;
import com.auranite.abloom.ElementType;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;

import java.util.Map;
import java.util.Optional;

/**
 * Datapack loader for custom elemental resistances.
 * Reads JSON files from data/<namespace>/abloom/element_resistances/*.json
 * 
 * Format:
 * {
 *   "element_type": "mymod:plasma_dmg",
 *   "entities": {
 *     "immune": ["minecraft:blaze", "minecraft:magma_cube"],
 *     "resistance": ["minecraft:strider"],
 *     "weakness": ["minecraft:snow_golem"]
 *   }
 * }
 */
public class CustomElementResistancesLoader extends SimplePreparableReloadListener<Map<ResourceLocation, com.google.gson.JsonObject>> {
    
    private static final String FOLDER = "abloom/element_resistances";
    
    public CustomElementResistancesLoader() {
        super();
    }
    
    @Override
    protected Map<ResourceLocation, com.google.gson.JsonObject> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.push("custom_element_resistances_prepare");
        
        Map<ResourceLocation, com.google.gson.JsonObject> jsonObjects = new java.util.HashMap<>();
        
        for (var entry : resourceManager.listResources(FOLDER, path -> path.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation id = entry.getKey();
            try (var reader = entry.getValue().openAsReader()) {
                com.google.gson.JsonObject json = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
                jsonObjects.put(id, json);
            } catch (Exception e) {
                AbloomMod.LOGGER.error("Failed to read resistance file {}: {}", id, e.getMessage());
            }
        }
        
        profiler.pop();
        return jsonObjects;
    }
    
    @Override
    protected void apply(Map<ResourceLocation, com.google.gson.JsonObject> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.push("custom_element_resistances");
        
        int loadedCount = 0;
        int failedCount = 0;
        
        objects.forEach((id, json) -> {
            try {
                // Get the element type ID
                String elementTypeId = json.has("element_type") ? json.get("element_type").getAsString() : null;
                if (elementTypeId == null || elementTypeId.isEmpty()) {
                    AbloomMod.LOGGER.warn("Skipping resistance file {} - missing element_type", id);
                    failedCount++;
                    return;
                }
                
                // Check if it's a custom element or built-in
                Optional<Object> elementTypeOpt = CustomElementRegistry.resolveDamageType(elementTypeId);
                if (elementTypeOpt.isEmpty()) {
                    // Try to parse as built-in ElementType
                    Optional<ElementType> builtinType = ElementType.fromDamageTypeId(elementTypeId);
                    if (builtinType.isEmpty()) {
                        AbloomMod.LOGGER.warn("Unknown element type '{}' in resistance file {}", elementTypeId, id);
                        failedCount++;
                        return;
                    }
                }
                
                // Process entity lists
                if (json.has("entities")) {
                    com.google.gson.JsonObject entities = json.getAsJsonObject("entities");
                    
                    // Process immune entities
                    if (entities.has("immune")) {
                        processEntityList(elementTypeId, entities.getAsJsonArray("immune"), 
                                ElementResistanceManager.Resistance.IMMUNE, id);
                        loadedCount++;
                    }
                    
                    // Process resistance entities
                    if (entities.has("resistance")) {
                        processEntityList(elementTypeId, entities.getAsJsonArray("resistance"), 
                                ElementResistanceManager.Resistance.HALF_RESIST, id);
                        loadedCount++;
                    }
                    
                    // Process weakness entities
                    if (entities.has("weakness")) {
                        processEntityList(elementTypeId, entities.getAsJsonArray("weakness"), 
                                ElementResistanceManager.Resistance.WEAKNESS, id);
                        loadedCount++;
                    }
                }
                
                AbloomMod.LOGGER.debug("Loaded resistance data from: {}", id);
                
            } catch (Exception e) {
                AbloomMod.LOGGER.error("Error loading resistance file {}: {}", id, e.getMessage());
                failedCount++;
            }
        });
        
        profiler.pop();
        AbloomMod.LOGGER.info("Loaded {} custom element resistance entries ({} failed)", loadedCount, failedCount);
    }
    
    private void processEntityList(String elementTypeId, com.google.gson.JsonArray entityArray, 
                                   ElementResistanceManager.Resistance resistance, ResourceLocation sourceFile) {
        Optional<Object> elementTypeOpt = CustomElementRegistry.resolveDamageType(elementTypeId);
        ElementType builtinType = null;
        
        if (elementTypeOpt.isPresent()) {
            Object obj = elementTypeOpt.get();
            if (obj instanceof ElementType) {
                builtinType = (ElementType) obj;
            }
            // For CustomElementType, we handle it differently - store by damage type ID string
        }
        
        for (var element : entityArray) {
            String entityId = element.getAsString();
            try {
                ResourceLocation entityRl = ResourceLocation.parse(entityId);
                Optional<EntityType<?>> entityTypeOpt = EntityType.byString(entityId);
                
                if (entityTypeOpt.isPresent()) {
                    EntityType<?> entityType = entityTypeOpt.get();
                    
                    // Register resistance using the manager
                    // We need to track custom element resistances separately
                    if (builtinType != null) {
                        ElementResistanceManager.registerResistance(entityType, 
                            java.util.Map.of(builtinType, resistance));
                    } else {
                        // For custom elements, store in a separate registry
                        CustomResistanceRegistry.registerResistance(entityType, elementTypeId, resistance);
                    }
                    
                    AbloomMod.LOGGER.debug("  Registered {} for {} against {}", resistance, entityId, elementTypeId);
                } else {
                    AbloomMod.LOGGER.warn("Unknown entity type '{}' in resistance file {}", entityId, sourceFile);
                }
            } catch (Exception e) {
                AbloomMod.LOGGER.error("Error processing entity '{}' in resistance file {}: {}", entityId, sourceFile, e.getMessage());
            }
        }
    }
}
