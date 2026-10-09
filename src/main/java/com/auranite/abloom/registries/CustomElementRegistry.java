package com.auranite.abloom.registries;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.datapack.CustomElementData;
import com.auranite.abloom.datapack.CustomElementProvider;
import com.auranite.abloom.util.ElementType;
import com.auranite.abloom.util.IElementalType;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for custom elemental types loaded from datapacks.
 * Provides lookup and management of custom element types.
 * 
 * <p>This registry works in conjunction with {@link ElementType} custom type support
 * to provide a unified API for accessing both built-in and custom element types.
 */
public class CustomElementRegistry {

    /** Thread-safe registry mapping element ID -> element data */
    private static final Map<ResourceLocation, CustomElementData> ELEMENT_DATA_BY_ID = new ConcurrentHashMap<>();
    
    /** Thread-safe registry mapping element name -> element data */
    private static final Map<String, CustomElementData> ELEMENT_DATA_BY_NAME = new ConcurrentHashMap<>();

    private CustomElementRegistry() {
        // Utility class
    }

    /**
     * Registers custom element data loaded from a datapack.
     * 
     * @param elementData the element data to register
     * @return true if the element was newly registered, false if a duplicate was detected
     */
    public static boolean register(CustomElementData elementData) {
        ResourceLocation elementId = elementData.elementId();
        String elementName = elementData.getElementName();

        if (ELEMENT_DATA_BY_ID.containsKey(elementId)) {
            AbloomMod.LOGGER.warn("Duplicate custom element registration for ID '{}' (from mod '{}'), skipping",
                    elementId, elementData.elementId().getNamespace());
            return false;
        }

        if (ELEMENT_DATA_BY_NAME.containsKey(elementName)) {
            AbloomMod.LOGGER.warn("Duplicate custom element registration for name '{}' (from mod '{}'), skipping",
                    elementName, elementData.elementId().getNamespace());
            return false;
        }

        ELEMENT_DATA_BY_ID.put(elementId, elementData);
        ELEMENT_DATA_BY_NAME.put(elementName, elementData);

        AbloomMod.LOGGER.debug("Registered custom element data: {} (name: {})", elementId, elementName);
        return true;
    }

    /**
     * Looks up custom element data by element ID.
     * 
     * @param elementId the element ID to look up
     * @return Optional containing the element data, or empty if not found
     */
    public static Optional<CustomElementData> byElementId(ResourceLocation elementId) {
        if (elementId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(ELEMENT_DATA_BY_ID.get(elementId));
    }

    /**
     * Looks up custom element data by element name.
     * 
     * @param elementName the element name to look up
     * @return Optional containing the element data, or empty if not found
     */
    public static Optional<CustomElementData> byElementName(String elementName) {
        if (elementName == null || elementName.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(ELEMENT_DATA_BY_NAME.get(elementName.toUpperCase(java.util.Locale.ROOT)));
    }

    /**
     * Gets the color for a custom element type.
     * 
     * @param type the elemental type
     * @return the color value, or 0xFFFFFF (white) if not found
     */
    public static int getColor(IElementalType type) {
        if (type == null || type.isCustom()) {
            String elementName = type.name();
            CustomElementData data = ELEMENT_DATA_BY_NAME.get(elementName);
            if (data != null) {
                return data.color();
            }
        }
        return 0xFFFFFF;
    }

    /**
     * Checks if a custom element with the given ID exists.
     * 
     * @param elementId the element ID to check
     * @return true if the element exists
     */
    public static boolean hasElement(ResourceLocation elementId) {
        return ELEMENT_DATA_BY_ID.containsKey(elementId);
    }

    /**
     * Checks if a custom element with the given name exists.
     * 
     * @param elementName the element name to check
     * @return true if the element exists
     */
    public static boolean hasElement(String elementName) {
        return ELEMENT_DATA_BY_NAME.containsKey(elementName.toUpperCase(java.util.Locale.ROOT));
    }

    /**
     * Returns the number of custom elements currently registered.
     */
    public static int getRegisteredCount() {
        return ELEMENT_DATA_BY_ID.size();
    }

    /**
     * Returns an unmodifiable map of all registered element data by ID.
     */
    public static Map<ResourceLocation, CustomElementData> getAllElementData() {
        return Map.copyOf(ELEMENT_DATA_BY_ID);
    }

    /**
     * Clears all registered custom element data.
     * Intended for testing or config reload scenarios.
     */
    public static void clearAll() {
        ELEMENT_DATA_BY_ID.clear();
        ELEMENT_DATA_BY_NAME.clear();
        AbloomMod.LOGGER.info("Cleared all custom element data");
    }

    /**
     * Loads custom elements from datapacks and registers them.
     * This is the main entry point for datapack loading.
     */
    public static void loadFromDatapacks() {
        AbloomMod.LOGGER.info("Loading custom elemental types from datapacks...");
        
        // CustomElementProvider.loadFromResources() handles the actual loading
        // and registration with ElementType.registerCustomElementType()
        CustomElementProvider.loadFromResources();
        
        AbloomMod.LOGGER.info("Custom elemental types loading complete. Total registered: {}", 
                getRegisteredCount());
    }
}