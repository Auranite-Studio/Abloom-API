package com.auranite.abloom;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for custom elemental damage types registered via datapacks.
 * This allows modpack creators and datapack authors to define their own elemental damage types.
 */
public class CustomElementRegistry {
    
    private static final Map<ResourceLocation, CustomElementType> CUSTOM_ELEMENTS = new ConcurrentHashMap<>();
    private static final Map<String, CustomElementType> BY_DAMAGE_TYPE_ID = new ConcurrentHashMap<>();
    
    private CustomElementRegistry() {}
    
    /**
     * Registers a custom elemental damage type.
     * 
     * @param elementType The custom element type to register
     * @return true if registration was successful, false if an element with the same ID already exists
     */
    public static boolean register(CustomElementType elementType) {
        if (elementType == null || elementType.getId() == null) {
            AbloomMod.LOGGER.warn("Cannot register null CustomElementType or one with null ID");
            return false;
        }
        
        ResourceLocation id = elementType.getId();
        
        if (CUSTOM_ELEMENTS.containsKey(id)) {
            AbloomMod.LOGGER.warn("Custom element type with ID {} is already registered! Skipping.", id);
            return false;
        }
        
        CUSTOM_ELEMENTS.put(id, elementType);
        BY_DAMAGE_TYPE_ID.put(elementType.getFullDamageTypeId(), elementType);
        
        // Also register the damage color for display purposes
        ElementDamageDisplayManager.registerDamageColor(elementType);
        
        AbloomMod.LOGGER.info("Registered custom elemental damage type: {} -> {}", id, elementType);
        return true;
    }
    
    /**
     * Gets a custom elemental damage type by its ID.
     * 
     * @param id The ResourceLocation ID of the custom element
     * @return Optional containing the custom element type if found
     */
    public static Optional<CustomElementType> get(ResourceLocation id) {
        return Optional.ofNullable(CUSTOM_ELEMENTS.get(id));
    }
    
    /**
     * Gets a custom elemental damage type by its full damage type ID (namespace:path).
     * 
     * @param fullDamageTypeId The full damage type ID (e.g., "mymod:plasma_dmg")
     * @return Optional containing the custom element type if found
     */
    public static Optional<CustomElementType> getByDamageTypeId(String fullDamageTypeId) {
        if (fullDamageTypeId == null) return Optional.empty();
        return Optional.ofNullable(BY_DAMAGE_TYPE_ID.get(fullDamageTypeId));
    }
    
    /**
     * Checks if a custom elemental damage type is registered.
     * 
     * @param id The ResourceLocation ID to check
     * @return true if the element is registered
     */
    public static boolean isRegistered(ResourceLocation id) {
        return CUSTOM_ELEMENTS.containsKey(id);
    }
    
    /**
     * Checks if a damage type ID corresponds to a custom element.
     * 
     * @param fullDamageTypeId The full damage type ID to check
     * @return true if it's a custom element
     */
    public static boolean isCustomElement(String fullDamageTypeId) {
        return BY_DAMAGE_TYPE_ID.containsKey(fullDamageTypeId);
    }
    
    /**
     * Gets all registered custom elemental damage types.
     * 
     * @return An unmodifiable map of all registered custom elements
     */
    public static Map<ResourceLocation, CustomElementType> getAll() {
        return Collections.unmodifiableMap(CUSTOM_ELEMENTS);
    }
    
    /**
     * Gets the count of registered custom elemental damage types.
     * 
     * @return The number of registered custom elements
     */
    public static int getCount() {
        return CUSTOM_ELEMENTS.size();
    }
    
    /**
     * Clears all registered custom elemental damage types.
     * Typically used for testing or when reloading datapacks.
     */
    public static void clear() {
        CUSTOM_ELEMENTS.clear();
        BY_DAMAGE_TYPE_ID.clear();
        AbloomMod.LOGGER.info("Cleared all custom elemental damage types");
    }
    
    /**
     * Attempts to resolve a damage type ID to either a built-in ElementType or a CustomElementType.
     * 
     * @param damageTypeId The damage type ID to resolve
     * @return Optional containing either ElementType or CustomElementType if found
     */
    public static Optional<Object> resolveDamageType(String damageTypeId) {
        if (damageTypeId == null) return Optional.empty();
        
        // First check if it's a custom element
        Optional<CustomElementType> custom = getByDamageTypeId(damageTypeId);
        if (custom.isPresent()) {
            return Optional.of(custom.get());
        }
        
        // Then check built-in types
        Optional<ElementType> builtin = ElementType.fromDamageTypeId(damageTypeId);
        if (builtin.isPresent()) {
            return Optional.of(builtin.get());
        }
        
        return Optional.empty();
    }
}
