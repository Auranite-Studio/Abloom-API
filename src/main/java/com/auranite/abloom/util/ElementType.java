package com.auranite.abloom.util;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.auranite.abloom.AbloomMod;
import net.minecraft.resources.ResourceLocation;

public enum ElementType implements IElementalType {
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

    ElementType(String damageTypeId) {
        this.damageTypeId = damageTypeId;
    }

    // ==================== IElementalType implementation ====================

    @Override
    public String damageTypeId() {
        return damageTypeId;
    }

    @Override
    public String getDisplayName() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }

    // ==================== End IElementalType ====================

    public String getFullDamageTypeId() {
        return "power:" + damageTypeId;
    }

    // ==================== Custom Element Type Support ====================

    /**
     * Immutable record representing a custom element type loaded from a datapack.
     * Custom types are not enum constants and cannot participate in switch statements,
     * but they can be referenced by name or damage type ID throughout the system.
     */
    public record CustomElementType(String name, String damageTypeId, String displayName,
            @org.jetbrains.annotations.Nullable String rawElementTranslationKey,
            @org.jetbrains.annotations.Nullable String rawResonanceTranslationKey) implements IElementalType {
        public CustomElementType {
            if (name == null || name.isEmpty()) throw new IllegalArgumentException("Name must not be null or empty");
            if (damageTypeId == null || damageTypeId.isEmpty()) throw new IllegalArgumentException("Damage type ID must not be null or empty");
        }

        @Override
        public String getDisplayName() {
            return displayName != null && !displayName.isEmpty() ? displayName : name;
        }

        @Override
        public boolean isCustom() {
            return true;
        }

        /**
         * Returns the raw element translation key for use with Component.translatable().
         */
        @org.jetbrains.annotations.Nullable
        public String getRawElementTranslationKey() {
            return rawElementTranslationKey;
        }

        /**
         * Returns the raw resonance translation key for use with Component.translatable().
         */
        @org.jetbrains.annotations.Nullable
        public String getRawResonanceTranslationKey() {
            return rawResonanceTranslationKey;
        }
    }

    /** Thread-safe registry mapping element type name -> custom type. */
    private static final ConcurrentHashMap<String, CustomElementType> CUSTOM_TYPES_BY_NAME = new ConcurrentHashMap<>();
    /** Thread-safe registry mapping damage type ID -> custom type. */
    private static final ConcurrentHashMap<String, CustomElementType> CUSTOM_TYPES_BY_DAMAGE_ID = new ConcurrentHashMap<>();

    /**
     * Registers a custom element type that can be referenced by name or damage type ID.
     * <p>
     * This method is thread-safe and idempotent — calling it twice with the same name
     * or damage type ID is a no-op (returns false on duplicate).
     *
     * @param name           the enum-style name (e.g. "PLASMA", "VOID_TEAR"); must be uppercase alphanumeric with underscores
     * @param damageTypeId   the damage type ID used in datapack JSON (e.g. "plasma_dmg")
     * @param displayName    human-readable display name (e.g. "Plasma"); if null or empty, falls back to name
     * @param rawElementTranslationKey  the raw translation key for the element (e.g. "element.yourmod.custom")
     * @param rawResonanceTranslationKey  the raw translation key for resonance text (e.g. "resonance.yourmod.custom")
     * @return true if the type was newly registered, false if a type with this name or damage type ID already exists
     * @throws IllegalArgumentException if name or damageTypeId is null, empty, or contains invalid characters
     */
    public static boolean registerCustomElementType(String name, String damageTypeId, String displayName,
            @org.jetbrains.annotations.Nullable String rawElementTranslationKey,
            @org.jetbrains.annotations.Nullable String rawResonanceTranslationKey) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Element type name must not be null or empty");
        }
        if (damageTypeId == null || damageTypeId.isEmpty()) {
            throw new IllegalArgumentException("Damage type ID must not be null or empty");
        }

        // Enforce uppercase enum-style name
        String normalizedName = name.toUpperCase(java.util.Locale.ROOT);
        if (!normalizedName.equals(name)) {
            throw new IllegalArgumentException("Element type name must be uppercase (use underscores for spaces): " + name);
        }
        if (!normalizedName.matches("[A-Z][A-Z0-9_]*")) {
            throw new IllegalArgumentException("Invalid element type name (must start with letter, contain only A-Z, 0-9, _): " + normalizedName);
        }
        if (!damageTypeId.matches("[a-z0-9/._-]+")) {
            throw new IllegalArgumentException("Invalid damage type ID (must contain only lowercase letters, numbers, /, ., _, -): " + damageTypeId);
        }

        if (CUSTOM_TYPES_BY_NAME.containsKey(normalizedName)) {
            AbloomMod.LOGGER.warn("Custom element type '{}' already registered, skipping", normalizedName);
            return false;
        }
        if (CUSTOM_TYPES_BY_DAMAGE_ID.containsKey(damageTypeId)) {
            AbloomMod.LOGGER.warn("Custom element type with damage ID '{}' already registered, skipping", damageTypeId);
            return false;
        }

        CustomElementType customType = new CustomElementType(normalizedName, damageTypeId, displayName,
                rawElementTranslationKey, rawResonanceTranslationKey);
        CUSTOM_TYPES_BY_NAME.put(normalizedName, customType);
        CUSTOM_TYPES_BY_DAMAGE_ID.put(damageTypeId, customType);

        AbloomMod.LOGGER.info("Registered custom element type '{}' (damage ID: '{}')", normalizedName, damageTypeId);
        return true;
    }

    /**
     * Convenience variant that uses the name as display name.
     */
    public static boolean registerCustomElementType(String name, String damageTypeId) {
        return registerCustomElementType(name, damageTypeId, null, null, null);
    }

    /** Looks up a custom element type by its name. */
    public static Optional<CustomElementType> getCustomTypeByName(String name) {
        if (name == null || name.isEmpty()) return Optional.empty();
        return Optional.ofNullable(CUSTOM_TYPES_BY_NAME.get(name.toUpperCase(java.util.Locale.ROOT)));
    }

    /** Looks up a custom element type by its damage type ID. */
    public static Optional<CustomElementType> getCustomTypeByDamageId(String damageTypeId) {
        if (damageTypeId == null || damageTypeId.isEmpty()) return Optional.empty();
        String cleanId = damageTypeId.contains(":") ? damageTypeId.substring(damageTypeId.indexOf(":") + 1) : damageTypeId;
        return Optional.ofNullable(CUSTOM_TYPES_BY_DAMAGE_ID.get(cleanId));
    }

    /** Checks if a custom element type with the given name exists. */
    public static boolean hasCustomType(String name) {
        return CUSTOM_TYPES_BY_NAME.containsKey(name.toUpperCase(java.util.Locale.ROOT));
    }

    /** Checks if a custom element type with the given damage type ID exists. */
    public static boolean hasCustomTypeByDamageId(String damageTypeId) {
        if (damageTypeId == null || damageTypeId.isEmpty()) return false;
        String cleanId = damageTypeId.contains(":") ? damageTypeId.substring(damageTypeId.indexOf(":") + 1) : damageTypeId;
        return CUSTOM_TYPES_BY_DAMAGE_ID.containsKey(cleanId);
    }

    /** Returns an unmodifiable set of all registered custom element type names. */
    public static Set<String> getCustomTypeNames() {
        return Set.copyOf(CUSTOM_TYPES_BY_NAME.keySet());
    }

    /** Returns true if the given type is a custom (non-builtin) element type. */
    public static boolean isCustomType(IElementalType type) {
        return type != null && type.isCustom();
    }

    /** Returns the number of custom element types currently registered. */
    public static int getCustomTypeCount() {
        return CUSTOM_TYPES_BY_NAME.size();
    }

    /** Clears all custom element types. Intended for testing or config reload scenarios. */
    public static void clearCustomTypes() {
        CUSTOM_TYPES_BY_NAME.clear();
        CUSTOM_TYPES_BY_DAMAGE_ID.clear();
        AbloomMod.LOGGER.info("Cleared all custom element types");
    }

    // ==================== End Custom Element Type Support ====================

    /**
     * Safe lookup for built-in ElementType by name.
     * Does NOT check custom types — use {@link IElementalType#byName(String)} for full lookup.
     */
    public static ElementType safeValueOf(String name) {
        if (name == null || name.isEmpty()) return null;
        try {
            return ElementType.valueOf(name);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    /**
     * Looks up a built-in ElementType by damage type ID.
     * Does NOT check custom types — use {@link IElementalType#byDamageTypeId(String)} for full lookup.
     */
    public static Optional<ElementType> fromDamageTypeId(String id) {
        if (id == null) return Optional.empty();

        String cleanId = id.contains(":") ? id.substring(id.indexOf(":") + 1) : id;

        return Arrays.stream(values())
                .filter(type -> type.damageTypeId().equals(cleanId))
                .findFirst();
    }

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
                 "generic",
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

            case "explosion",
                 "explosion_player",
                 "wind_charge",
                 "generic_knockback",
                 "sonic_boom" -> WIND;

            case "stalagmite",
                 "falling_stalactite",
                 "falling_anvil",
                 "falling_block" -> EARTH;

            case "lightning_bolt" -> ELECTRIC;

            case "freeze",
                 "frostbite" -> ICE;

            case "thorns",
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

            case "indirect_magic",
                 "magic" -> ETHER;

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
}
