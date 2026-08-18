package com.auranite.abloom;

import com.auranite.abloom.datapack.CustomElementLoader.ResonanceEffectData;
import java.util.Arrays;
import java.util.Optional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.resources.ResourceLocation;

/**
 * Represents an element type in the Abloom API.
 * Supports both builtin elements (defined as constants) and custom elements (loaded from datapacks).
 * 
 * <p>Builtin elements are defined as static final instances and provide the core functionality.</p>
 * <p>Custom elements can be added via datapacks using {@link CustomElementRegistry}.</p>
 */
public class ElementType {
    // Builtin elements
    public static final ElementType FIRE = new ElementType("FIRE", "fire_dmg", 0xFF5500);
    public static final ElementType PHYSICAL = new ElementType("PHYSICAL", "physical_dmg", 0xC0C0C0);
    public static final ElementType WIND = new ElementType("WIND", "wind_dmg", 0x00FFFF);
    public static final ElementType EARTH = new ElementType("EARTH", "earth_dmg", 0x8B4513);
    public static final ElementType WATER = new ElementType("WATER", "water_dmg", 0x0080FF);
    public static final ElementType ICE = new ElementType("ICE", "ice_dmg", 0x00BFFF);
    public static final ElementType ELECTRIC = new ElementType("ELECTRIC", "electric_dmg", 0xFF19FF);
    public static final ElementType ENERGY = new ElementType("ENERGY", "energy_dmg", 0xFFFF00);
    public static final ElementType NATURAL = new ElementType("NATURAL", "natural_dmg", 0x32CD32);
    public static final ElementType QUANTUM = new ElementType("QUANTUM", "quantum_dmg", 0x9400D3);
    public static final ElementType ETHER = new ElementType("ETHER", "ether_dmg", 0x24B3A7);
    public static final ElementType LIGHT = new ElementType("LIGHT", "light_dmg", 0xFFF1A5);
    public static final ElementType SHADOW = new ElementType("SHADOW", "shadow_dmg", 0x4B0082);
    public static final ElementType PRISMATIC = new ElementType("PRISMATIC", "prismatic_dmg", 0xFFFFFF);
    
    // Registry of all element types (builtin + custom)
    private static final Map<String, ElementType> ELEMENT_REGISTRY = new ConcurrentHashMap<>();
    private static final Map<String, ElementType> DAMAGE_TYPE_REGISTRY = new ConcurrentHashMap<>();
    
    static {
        // Register builtin elements
        registerBuiltin(FIRE);
        registerBuiltin(PHYSICAL);
        registerBuiltin(WIND);
        registerBuiltin(EARTH);
        registerBuiltin(WATER);
        registerBuiltin(ICE);
        registerBuiltin(ELECTRIC);
        registerBuiltin(ENERGY);
        registerBuiltin(NATURAL);
        registerBuiltin(QUANTUM);
        registerBuiltin(ETHER);
        registerBuiltin(LIGHT);
        registerBuiltin(SHADOW);
        registerBuiltin(PRISMATIC);
    }
    
    private final String name;
    private final String damageTypeId;
    private final int damageColor;
    private final boolean isBuiltin;
    private final String translationKey;
    private final String resonanceTranslationKey;
    private final boolean canAccumulate;
    private final ResonanceEffectData effectData;
    
    /**
     * Create a builtin element type.
     */
    private ElementType(String name, String damageTypeId, int damageColor) {
        this.name = name;
        this.damageTypeId = damageTypeId;
        this.damageColor = damageColor;
        this.isBuiltin = true;
        this.translationKey = "element.abloom." + name.toLowerCase();
        this.resonanceTranslationKey = "resonance_text.abloom." + name.toLowerCase();
        this.canAccumulate = true;
        this.effectData = null;
    }
    
    /**
     * Create a custom element type (package-private, use CustomElementRegistry).
     */
    ElementType(String namespace, String elementId, String damageTypeId, int damageColor, 
                String translationKey, String resonanceTranslationKey, 
                boolean canAccumulate, ResonanceEffectData effectData) {
        this.name = elementId;
        this.damageTypeId = damageTypeId;
        this.damageColor = damageColor;
        this.isBuiltin = false;
        this.translationKey = translationKey;
        this.resonanceTranslationKey = resonanceTranslationKey;
        this.canAccumulate = canAccumulate;
        this.effectData = effectData;
    }
    
    /**
     * Register a builtin element.
     */
    private static void registerBuiltin(ElementType type) {
        ELEMENT_REGISTRY.put(type.name, type);
        DAMAGE_TYPE_REGISTRY.put(type.damageTypeId.toLowerCase(), type);
    }
    
    /**
     * Register a custom element (called by CustomElementRegistry).
     */
    static void registerCustom(ElementType type) {
        if (ELEMENT_REGISTRY.containsKey(type.name)) {
            AbloomMod.LOGGER.warn("Attempting to register duplicate element '{}', ignoring", type.name);
            return;
        }
        ELEMENT_REGISTRY.put(type.name, type);
        DAMAGE_TYPE_REGISTRY.put(type.damageTypeId.toLowerCase(), type);
    }
    
    /**
     * Register a custom element from datapack.
     * @param namespace Namespace of the element
     * @param elementId Element ID (without namespace)
     * @param damageTypeId Full damage type ID (e.g., "yourmod:custom_element")
     * @param color Display color
     * @param baseElement Base element for fallback behavior
     * @param translationKey Translation key for element name
     * @param resonanceTranslationKey Translation key for resonance text
     * @param canAccumulate Can accumulate resonance points
     * @param effectData Resonance effect configuration
     */
    public static void registerCustom(String namespace, String elementId, String damageTypeId, 
                                      int color, ElementType baseElement,
                                      String translationKey, String resonanceTranslationKey,
                                      boolean canAccumulate, ResonanceEffectData effectData) {
        ElementType customType = new ElementType(namespace, elementId, damageTypeId, color,
                                                  translationKey, resonanceTranslationKey,
                                                  canAccumulate, effectData);
        registerCustom(customType);
    }
    
    /**
     * Get the element name.
     */
    public String name() {
        return name;
    }
    
    /**
     * Get the damage type ID (without namespace).
     */
    public String getDamageTypeId() {
        return damageTypeId;
    }
    
    /**
     * Get the full damage type ID with namespace.
     */
    public String getFullDamageTypeId() {
        return "power:" + damageTypeId;
    }
    
    /**
     * Get the damage display color for this element.
     */
    public int getDamageColor() {
        return damageColor;
    }
    
    /**
     * Check if this is a builtin element.
     */
    public boolean isBuiltin() {
        return isBuiltin;
    }
    
    /**
     * Check if this is a custom element.
     */
    public boolean isCustom() {
        return !isBuiltin;
    }
    
    /**
     * Get the translation key for the element name.
     */
    public String getTranslationKey() {
        return translationKey;
    }
    
    /**
     * Get the translation key for the resonance effect text.
     */
    public String getResonanceTranslationKey() {
        return resonanceTranslationKey;
    }
    
    /**
     * Check if this element can accumulate resonance points.
     */
    public boolean canAccumulate() {
        return canAccumulate;
    }
    
    /**
     * Get the resonance effect data for this element.
     */
    public ResonanceEffectData getEffectData() {
        return effectData;
    }
    
    /**
     * Get an element type by name.
     */
    public static Optional<ElementType> fromName(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(ELEMENT_REGISTRY.get(name.toUpperCase()));
    }
    
    /**
     * Get an element type from damage type ID.
     */
    public static Optional<ElementType> fromDamageTypeId(String id) {
        if (id == null) return Optional.empty();

        String cleanId = id.contains(":") ? id.substring(id.indexOf(":") + 1) : id;

        ElementType type = DAMAGE_TYPE_REGISTRY.get(cleanId.toLowerCase());
        return type != null ? Optional.of(type) : Optional.empty();
    }
    
    /**
     * Get all registered element types (builtin + custom).
     */
    public static ElementType[] values() {
        return ELEMENT_REGISTRY.values().toArray(new ElementType[0]);
    }
    
    /**
     * Safe value lookup that returns null instead of throwing.
     */
    public static ElementType safeValueOf(String name) {
        if (name == null) return null;
        return ELEMENT_REGISTRY.get(name.toUpperCase());
    }
    
    /**
     * Map vanilla damage types to Abloom elements.
     */
    public static ElementType fromVanillaDamageType(String damageTypeId) {
        if (damageTypeId == null || damageTypeId.isEmpty()) {
            AbloomMod.LOGGER.warn("DamageType ID is null or empty, defaulting to PHYSICAL");
            return PHYSICAL;
        }

        String id = normalizeDamageTypeId(damageTypeId);

        AbloomMod.LOGGER.debug("Mapped DamageType '{}' -> normalized '{}'", damageTypeId, id);

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
        return name;
    }

    public String getDisplayName() {
        return name.charAt(0) + name.substring(1).toLowerCase();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ElementType)) return false;
        ElementType other = (ElementType) obj;
        return this.name.equals(other.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}