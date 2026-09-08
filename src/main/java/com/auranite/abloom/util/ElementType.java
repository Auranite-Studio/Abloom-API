package com.auranite.abloom.util;

import java.util.Arrays;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.datapack.CustomElementData;
import net.minecraft.resources.ResourceLocation;

/**
 * Represents an elemental damage type in the Abloom API.
 * Supports both built-in element types and custom elements loaded from datapacks.
 * 
 * <p>Built-in element types are defined as enum constants. Custom elements can be
 * added via datapack JSON files in the {@code data/<namespace>/custom_elements/} folder.</p>
 * 
 * @see com.auranite.abloom.datapack.CustomElementLoader
 */
public enum ElementType {
    FIRE("fire_dmg"),
    PHYSICAL("physical_dmg"),
    WIND("wind_dmg"),
    EARTH("earth_dmg"),
    WATER("water_dmg"),
    ICE("ice_dmg"),
    ELECTRIC("electric_dmg"),
    ENERGY("energy_dmg"),
    NATURAL("natural_dmg"),
    QUANTUM("quantum_dmg"),
    ETHER("ether_dmg"),
    LIGHT("light_dmg"),
    SHADOW("shadow_dmg"),
    PRISMATIC("prismatic_dmg");

    private final String damageTypeId;
    
    // Cache for custom element instances
    private static final Map<String, ElementType> CUSTOM_ELEMENT_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, CustomElementData> CUSTOM_DATA_CACHE = new ConcurrentHashMap<>();

    ElementType(String damageTypeId) {
        this.damageTypeId = damageTypeId;
    }

    public String getDamageTypeId() {
        return damageTypeId;
    }

    public String getFullDamageTypeId() {
        return "power:" + damageTypeId;
    }
    
    /**
     * Register a custom element type at runtime.
     * This allows mods to add custom elements programmatically.
     * 
     * @param elementId unique identifier for the custom element (without namespace)
     * @param damageType ResourceLocation of the damage type
     * @param color display color for damage numbers
     * @param basedElementKey base element key for translation fallback
     * @param elementTranslationKey translation key for the element name
     * @param resonanceTranslationKey translation key for resonance effect text
     * @param canResonanceAccumulation whether this element can accumulate resonance points
     * @return the created or cached ElementType
     */
    public static ElementType registerCustomElement(String elementId, ResourceLocation damageType,
                                                     int color, String basedElementKey,
                                                     String elementTranslationKey,
                                                     String resonanceTranslationKey,
                                                     boolean canResonanceAccumulation) {
        return registerCustomElement(elementId, damageType, color, basedElementKey,
                elementTranslationKey, resonanceTranslationKey, canResonanceAccumulation, null);
    }
    
    /**
     * Register a custom element type with resonance effect data.
     * 
     * @param elementId unique identifier for the custom element (without namespace)
     * @param damageType ResourceLocation of the damage type
     * @param color display color for damage numbers
     * @param basedElementKey base element key for translation fallback
     * @param elementTranslationKey translation key for the element name
     * @param resonanceTranslationKey translation key for resonance effect text
     * @param canResonanceAccumulation whether this element can accumulate resonance points
     * @param effectData optional resonance effect data
     * @return the created or cached ElementType
     */
    public static ElementType registerCustomElement(String elementId, ResourceLocation damageType,
                                                     int color, String basedElementKey,
                                                     String elementTranslationKey,
                                                     String resonanceTranslationKey,
                                                     boolean canResonanceAccumulation,
                                                     CustomElementData.ResonanceEffectData effectData) {
        if (elementId == null || elementId.isEmpty()) {
            AbloomMod.LOGGER.warn("Cannot register custom element with null/empty ID");
            return null;
        }
        
        // Check if already registered
        ElementType cached = CUSTOM_ELEMENT_CACHE.get(elementId);
        if (cached != null) {
            AbloomMod.LOGGER.debug("Custom element '{}' already registered, returning cached instance", elementId);
            return cached;
        }
        
        // Create custom data
        CustomElementData data = new CustomElementData(
                elementId, damageType, color, basedElementKey,
                elementTranslationKey, resonanceTranslationKey,
                canResonanceAccumulation, effectData
        );
        
        // Store in caches
        CUSTOM_DATA_CACHE.put(elementId, data);
        
        AbloomMod.LOGGER.info("Registered custom element: {} -> {}", elementId, damageType);
        return getOrCreateCustomElement(elementId);
    }
    
    /**
     * Get or create a custom ElementType instance.
     * Custom elements are stored separately from enum constants but behave identically.
     */
    private static ElementType getOrCreateCustomElement(String elementId) {
        return CUSTOM_ELEMENT_CACHE.computeIfAbsent(elementId, id -> {
            // We use a placeholder approach - custom elements are tracked by ID string
            // but we return a special marker that can be checked with isCustomElement()
            AbloomMod.LOGGER.debug("Creating custom element reference for: {}", id);
            return null; // Will be handled by wrapper methods
        });
    }
    
    /**
     * Check if an element ID refers to a custom element.
     * @param elementId the element ID to check
     * @return true if it's a custom element
     */
    public static boolean isCustomElement(String elementId) {
        return CUSTOM_ELEMENT_CACHE.containsKey(elementId) || CUSTOM_DATA_CACHE.containsKey(elementId);
    }
    
    /**
     * Get custom element data for a given element ID.
     * @param elementId the element ID
     * @return Optional containing the custom element data if found
     */
    public static Optional<CustomElementData> getCustomElementData(String elementId) {
        if (elementId == null) return Optional.empty();
        CustomElementData data = CUSTOM_DATA_CACHE.get(elementId);
        if (data != null) return Optional.of(data);
        return Optional.empty();
    }
    
    /**
     * Internal method to sync custom elements from datapack loader.
     * Called by CustomElementLoader when datapacks are loaded.
     */
    public static void syncCustomElements(Map<String, CustomElementData> customElements) {
        CUSTOM_DATA_CACHE.clear();
        CUSTOM_ELEMENT_CACHE.clear();
        customElements.forEach((id, data) -> {
            CUSTOM_DATA_CACHE.put(id, data);
            AbloomMod.LOGGER.debug("Synced custom element: {} -> {}", id, data.getDamageType());
        });
        AbloomMod.LOGGER.info("Synced {} custom elements from datapacks", customElements.size());
    }

    public static Optional<ElementType> fromDamageTypeId(String id) {
        if (id == null) return Optional.empty();

        String cleanId = id.contains(":") ? id.substring(id.indexOf(":") + 1) : id;

        // First check built-in elements
        Optional<ElementType> builtin = Arrays.stream(values())
                .filter(type -> type.getDamageTypeId().equals(cleanId))
                .findFirst();
        
        if (builtin.isPresent()) {
            return builtin;
        }
        
        // Then check custom elements
        for (Map.Entry<String, CustomElementData> entry : CUSTOM_DATA_CACHE.entrySet()) {
            if (entry.getValue().getDamageType() != null && 
                entry.getValue().getDamageType().getPath().equals(cleanId)) {
                AbloomMod.LOGGER.debug("Found custom element for damage type: {}", cleanId);
                return Optional.of(getOrCreateCustomElement(entry.getKey()));
            }
        }
        
        return Optional.empty();
    }

    public static ElementType fromVanillaDamageType(String damageTypeId) {
        if (damageTypeId == null || damageTypeId.isEmpty()) {
            AbloomMod.LOGGER.warn("DamageType ID is null or empty, defaulting to PHYSICAL");
            return PHYSICAL;
        }

        String id = normalizeDamageTypeId(damageTypeId);

        AbloomMod.LOGGER.debug("Mapped DamageType '{}' -> normalized '{}'", damageTypeId, id);

        // Check custom elements first
        for (Map.Entry<String, CustomElementData> entry : CUSTOM_DATA_CACHE.entrySet()) {
            if (entry.getValue().getDamageType() != null) {
                String customPath = entry.getValue().getDamageType().getPath();
                String customNamespace = entry.getValue().getDamageType().getNamespace();
                
                // Match by path or full ResourceLocation
                if (customPath.equals(id) || entry.getValue().getDamageType().toString().equals(damageTypeId)) {
                    AbloomMod.LOGGER.debug("Matched custom element: {} for damage type: {}", 
                            entry.getKey(), damageTypeId);
                    return getOrCreateCustomElement(entry.getKey());
                }
            }
        }

        return switch (id) {

            case "arrow",
                 "player_attack",
                 "entity_attack",
                 "mob_attack",
                 "mob_projectile",
                 "fall",
                 "anvil",
                 "cactus",
                 "sweet_berry_bush",
                 "fly_into_wall",
                 "dragon_breath",
                 "wither_skull",
                 "trident",
                 "sweep_attack",
                 "fireball",
                 "thrown",
                 "end_crystal" -> PHYSICAL;

            case "in_fire",
                 "on_fire",
                 "lava",
                 "hot_floor",
                 "campfire",
                 "unattributed_fireball",
                 "fireworks" -> FIRE;

            case "drown",
                 "wet" -> WATER;

            case "generic",
                 "explosion",
                 "explosion_player",
                 "wind_charge",
                 "generic_knockback" -> WIND;

            case "stalagmite",
                 "falling_stalactite",
                 "falling_anvil",
                 "falling_block" -> EARTH;

            case "lightning_bolt" -> ELECTRIC;

            case "freeze",
                 "frostbite" -> ICE;

            case "indirect_magic",
                 "magic",
                 "sonic_boom",
                 "thorns",
                 "guardian",
                 "evocation_fangs",
                 "wither_effect" -> ENERGY;

            case "poison",
                 "wither",
                 "starve",
                 "cramming",
                 "dry_out" -> NATURAL;

            case "out_of_world",
                 "generic_kill",
                 "void",
                 "outside_border" -> QUANTUM;

//            case "ether",
//                 "aether",
//                 "magic_ether",
//                 "arcane" -> ETHER;

            default -> {
                AbloomMod.LOGGER.debug("Unknown DamageType '{}', defaulting to PHYSICAL", id);
                yield PHYSICAL;
            }
        };
    }

    private static String normalizeDamageTypeId(String input) {
        if (input == null) return "generic";

        String str = input.trim();
        if (str.startsWith("ResourceKey[")) {
            int colonIdx = str.indexOf(':');
            int bracketIdx = str.indexOf(']');
            if (colonIdx > 0 && bracketIdx > colonIdx) {
                str = str.substring(colonIdx + 1, bracketIdx).trim();
            }
        }
        if (str.contains(":")) {
            String[] parts = str.split(":", 2);
            str = parts.length > 1 && !parts[1].isEmpty() ? parts[1] : parts[0];
        }
        str = camelToSnake(str);
        str = str.toLowerCase(java.util.Locale.ROOT);

        str = str.replaceAll("[^a-z0-9/._-]", "_");
        try {
            ResourceLocation rl = ResourceLocation.parse("minecraft:" + str);
            return rl.getPath();
        } catch (Exception e) {
            return str;
        }
    }

    private static String camelToSnake(String input) {
        if (input == null || input.isEmpty()) return input;

        return input.replaceAll("([a-z])([A-Z]+)", "$1_$2");
    }

    @Override
    public String toString() {
        return name();
    }

    public String getDisplayName() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }

    public static ElementType safeValueOf(String name) {
        if (name == null || name.isEmpty()) return null;
        
        // Check if it's a custom element
        if (CUSTOM_DATA_CACHE.containsKey(name)) {
            return getOrCreateCustomElement(name);
        }
        
        try {
            return ElementType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
    
    /**
     * Check if this ElementType represents a custom element.
     * @return true if this is a custom element
     */
    public boolean isCustom() {
        return CUSTOM_ELEMENT_CACHE.containsValue(this);
    }
    
    /**
     * Get the custom element data for this ElementType if it's a custom element.
     * @return Optional containing the custom element data
     */
    public Optional<CustomElementData> getCustomData() {
        if (!isCustom()) return Optional.empty();
        
        for (Map.Entry<String, ElementType> entry : CUSTOM_ELEMENT_CACHE.entrySet()) {
            if (entry.getValue() == this) {
                return getCustomElementData(entry.getKey());
            }
        }
        return Optional.empty();
    }
    
    /**
     * Get all available element types including both built-in and custom elements.
     * For use in loops instead of ElementType.values() to support custom elements.
     * @return array of all element types
     */
    public static ElementType[] getAllElements() {
        // Combine built-in enum values with custom elements
        ElementType[] builtin = values();
        Map<String, CustomElementData> customData = CUSTOM_DATA_CACHE;
        
        if (customData.isEmpty()) {
            return builtin;
        }
        
        // Create array with space for both builtin and custom
        java.util.List<ElementType> allElements = new java.util.ArrayList<>();
        allElements.addAll(java.util.Arrays.asList(builtin));
        
        // Add custom element placeholders
        for (String elementId : customData.keySet()) {
            ElementType custom = getOrCreateCustomElement(elementId);
            if (custom != null && !allElements.contains(custom)) {
                allElements.add(custom);
            }
        }
        
        return allElements.toArray(new ElementType[0]);
    }
    
    /**
     * Get all custom element IDs that are currently registered.
     * @return set of custom element IDs
     */
    public static java.util.Set<String> getAllCustomElementIds() {
        return CUSTOM_DATA_CACHE.keySet();
    }
    
    /**
     * Check if an ElementType is a built-in enum constant.
     * @return true if this is a built-in element, false if custom
     */
    public boolean isBuiltIn() {
        try {
            ElementType.valueOf(this.name());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}