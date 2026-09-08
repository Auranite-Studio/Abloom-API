package com.auranite.abloom.datapack;

import com.auranite.abloom.AbloomMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

/**
 * Datapack loader for custom element types.
 * Reads JSON files from data/<namespace>/custom_elements/ directory.
 * 
 * <p>Example JSON format:</p>
 * <pre>{@code
 * {
 *   "element_id": "CUSTOM_ELEMENT",
 *   "damage_type": "yourmod:custom_element",
 *   "color": 0x2B004C,
 *   "based_elemental_dmg": "FIRE",
 *   "element_translation_key": "element.yourmod.custom",
 *   "resonance_translation_key": "resonance_text.yourmod.custom",
 *   "can_resonance_accumulation": true,
 *   "effect": {
 *     "type": "yourmod:custom_resonance_effect",
 *     "config": {
 *       "duration": 100,
 *       "amplifier": 0
 *     }
 *   }
 * }
 * }</pre>
 */
public class CustomElementLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FOLDER = "custom_elements";
    
    private Map<String, CustomElementData> customElements = new HashMap<>();

    public CustomElementLoader() {
        super(GSON, FOLDER);
    }

    @Override
    protected void apply(Map<String, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<String, CustomElementData> elements = new HashMap<>();
        
        profiler.push("custom_elements");
        
        jsonMap.forEach((resourceId, jsonElement) -> {
            try {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                CustomElementData data = CustomElementData.fromJson(jsonObject);
                
                if (data.getElementId() != null && !data.getElementId().isEmpty()) {
                    elements.put(data.getElementId(), data);
                    AbloomMod.LOGGER.debug("Loaded custom element: {} -> {}", 
                            data.getElementId(), data.getDamageType());
                } else {
                    AbloomMod.LOGGER.warn("Skipping custom element with null/empty element_id in {}", resourceId);
                }
            } catch (Exception e) {
                AbloomMod.LOGGER.error("Failed to parse custom element JSON: {}", resourceId, e);
            }
        });
        
        this.customElements = elements;
        
        profiler.pop();
        AbloomMod.LOGGER.info("Loaded {} custom element types", customElements.size());
    }

    /**
     * Get a custom element by its ID.
     * @param elementId the element ID (without namespace)
     * @return Optional containing the custom element data if found
     */
    public static Optional<CustomElementData> getCustomElement(String elementId) {
        return Optional.ofNullable(CustomElementProvider.CUSTOM_ELEMENTS.get(elementId));
    }

    /**
     * Get all loaded custom elements.
     * @return map of element ID to custom element data
     */
    public static Map<String, CustomElementData> getAllCustomElements() {
        return new HashMap<>(CustomElementProvider.CUSTOM_ELEMENTS);
    }

    /**
     * Check if a custom element exists.
     * @param elementId the element ID
     * @return true if the element exists
     */
    public static boolean hasCustomElement(String elementId) {
        return CustomElementProvider.CUSTOM_ELEMENTS.containsKey(elementId);
    }

    /**
     * Internal provider class to hold loaded custom elements.
     */
    public static class CustomElementProvider {
        private static final Map<String, CustomElementData> CUSTOM_ELEMENTS = new HashMap<>();
        
        static void setElements(Map<String, CustomElementData> elements) {
            CUSTOM_ELEMENTS.clear();
            CUSTOM_ELEMENTS.putAll(elements);
        }
    }

    @Override
    protected void apply(Map<String, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler, Runnable onLoadComplete) {
        apply(object, resourceManager, profiler);
        CustomElementProvider.setElements(this.customElements);
        // Sync with ElementType
        com.auranite.abloom.util.ElementType.syncCustomElements(this.customElements);
        onLoadComplete.run();
    }
}
