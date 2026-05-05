package com.auranite.abloom.datapack;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.ElementResistanceManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for custom elemental resistances (for custom element types defined via datapacks).
 */
public class CustomResistanceRegistry {
    
    // Maps entity type -> (element type ID -> resistance)
    private static final Map<String, Map<String, ElementResistanceManager.Resistance>> CUSTOM_RESISTANCES = new ConcurrentHashMap<>();
    
    private CustomResistanceRegistry() {}
    
    /**
     * Registers a resistance for an entity type against a custom element type.
     * 
     * @param entityType The entity type
     * @param elementTypeId The custom element type ID (e.g., "mymod:plasma_dmg")
     * @param resistance The resistance value
     */
    public static void registerResistance(net.minecraft.world.entity.EntityType<?> entityType, 
                                          String elementTypeId, 
                                          ElementResistanceManager.Resistance resistance) {
        if (entityType == null || elementTypeId == null || resistance == null) {
            return;
        }
        
        String entityKey = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
            entityType.builtInRegistryHolder().key().location().getNamespace(),
            entityType.builtInRegistryHolder().key().location().getPath()
        ).toString();
        
        CUSTOM_RESISTANCES.computeIfAbsent(entityKey, k -> new ConcurrentHashMap<>())
                         .put(elementTypeId, resistance);
        
        AbloomMod.LOGGER.debug("Registered custom resistance for {} against {}: {}", 
                              entityKey, elementTypeId, resistance);
    }
    
    /**
     * Gets the resistance for an entity type against a custom element type.
     * 
     * @param entityType The entity type
     * @param elementTypeId The custom element type ID
     * @return The resistance value, or ZERO if not found
     */
    public static ElementResistanceManager.Resistance getResistance(net.minecraft.world.entity.EntityType<?> entityType, 
                                                                     String elementTypeId) {
        if (entityType == null || elementTypeId == null) {
            return ElementResistanceManager.Resistance.ZERO;
        }
        
        String entityKey = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
            entityType.builtInRegistryHolder().key().location().getNamespace(),
            entityType.builtInRegistryHolder().key().location().getPath()
        ).toString();
        
        Map<String, ElementResistanceManager.Resistance> entityResistances = CUSTOM_RESISTANCES.get(entityKey);
        if (entityResistances == null) {
            return ElementResistanceManager.Resistance.ZERO;
        }
        
        ElementResistanceManager.Resistance resistance = entityResistances.get(elementTypeId);
        return resistance != null ? resistance : ElementResistanceManager.Resistance.ZERO;
    }
    
    /**
     * Clears all custom resistances.
     */
    public static void clear() {
        CUSTOM_RESISTANCES.clear();
        AbloomMod.LOGGER.info("Cleared all custom element resistances");
    }
}
