package com.auranite.abloom;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;

/**
 * Registry for custom elements defined via datapacks.
 * Allows mods and datapacks to add new element types without modifying the base ElementType class.
 * 
 * <p>Custom elements are loaded from JSON files in data/&lt;modid&gt;/custom_elements/ directory.</p>
 * <p>See docs/custom_elements.md for documentation on creating custom elements.</p>
 */
public class CustomElementRegistry {
    private static final String MODID = "abloom";
    
    // Map of custom element ID (uppercase) to ElementType
    private static final Map<String, ElementType> CUSTOM_ELEMENTS = new ConcurrentHashMap<>();
    
    private CustomElementRegistry() {}
    
    /**
     * Initialize the custom element registry.
     * This method is called automatically when datapacks are loaded.
     * Custom elements should be registered via datapack JSON files, not programmatically.
     */
    public static void init() {
        // Custom elements are now loaded via CustomElementLoader from datapacks
        // This method is kept for backward compatibility
        AbloomMod.LOGGER.debug("CustomElementRegistry initialized");
    }
    
    /**
     * Register a custom element programmatically (for advanced use cases).
     * Most users should use datapack JSON files instead.
     * 
     * @param namespace Namespace of the element
     * @param elementId Element ID (without namespace, e.g., "CUSTOM_ELEMENT")
     * @param damageTypeId Full damage type ID (e.g., "yourmod:custom_element")
     * @param color Display color as RGB integer
     * @param baseElement Base element for fallback behavior
     * @param translationKey Translation key for element name
     * @param resonanceTranslationKey Translation key for resonance text
     * @param canAccumulate Can accumulate resonance points
     * @param effectData Resonance effect configuration
     */
    public static void registerCustom(String namespace, String elementId, String damageTypeId,
                                      int color, ElementType baseElement,
                                      String translationKey, String resonanceTranslationKey,
                                      boolean canAccumulate, Object effectData) {
        try {
            ElementType.registerCustom(namespace, elementId, damageTypeId, color,
                                       baseElement, translationKey, resonanceTranslationKey,
                                       canAccumulate, 
                                       effectData != null ? (com.auranite.abloom.datapack.CustomElementLoader.ResonanceEffectData) effectData : null);
            
            CUSTOM_ELEMENTS.put(elementId.toUpperCase(), ElementType.fromName(elementId).orElse(null));
            AbloomMod.LOGGER.info("Registered custom element: {}.{}", namespace, elementId);
        } catch (Exception e) {
            AbloomMod.LOGGER.error("Failed to register custom element: {}.{}", namespace, elementId, e);
        }
    }
    
    /**
     * Get a custom element by its ID.
     */
    public static Optional<ElementType> getCustomElement(String elementId) {
        if (elementId == null) return Optional.empty();
        ElementType type = CUSTOM_ELEMENTS.get(elementId.toUpperCase());
        return type != null ? Optional.of(type) : Optional.empty();
    }
    
    /**
     * Check if an element ID corresponds to a custom element.
     */
    public static boolean isCustomElement(String elementId) {
        if (elementId == null) return false;
        return CUSTOM_ELEMENTS.containsKey(elementId.toUpperCase());
    }
    
    /**
     * Get all registered custom elements.
     */
    public static Collection<ElementType> getAllCustomElements() {
        return Collections.unmodifiableCollection(CUSTOM_ELEMENTS.values());
    }
    
    /**
     * Get count of registered custom elements.
     */
    public static int getCustomElementCount() {
        return CUSTOM_ELEMENTS.size();
    }
    
    /**
     * Clear all registered custom elements (for server reload).
     */
    public static void clearAll() {
        CUSTOM_ELEMENTS.clear();
        AbloomMod.LOGGER.info("Cleared all custom element registrations");
    }
}
