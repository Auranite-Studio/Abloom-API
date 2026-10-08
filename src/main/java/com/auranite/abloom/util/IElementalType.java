package com.auranite.abloom.util;

import java.util.Optional;

/**
 * Common interface for all elemental types (built-in enum + custom datapack types).
 * Enables unified handling of element types throughout the API.
 */
public interface IElementalType {
    /** Enum-style name (e.g. "FIRE", "PLASMA") */
    String name();
    /** Damage type ID used in datapack JSON (e.g. "fire_dmg", "plasma_dmg") */
    String damageTypeId();
    /** Human-readable display name (e.g. "Fire", "Plasma") */
    String getDisplayName();
    /** True if this is a custom (non-builtin) element type */
    default boolean isCustom() { return false; }

    /**
     * Looks up a built-in ElementType by its enum name.
     * @return Optional containing the ElementType, or empty if not found
     */
    static Optional<ElementType> fromName(String name) {
        if (name == null || name.isEmpty()) return Optional.empty();
        try {
            return Optional.of(ElementType.valueOf(name));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Looks up any elemental type (built-in or custom) by name.
     * @return Optional containing the type, or empty if not found
     */
    @SuppressWarnings("unchecked")
    static Optional<? extends IElementalType> byName(String name) {
        if (name == null || name.isEmpty()) return Optional.empty();
        Optional<ElementType> builtin = fromName(name);
        if (builtin.isPresent()) return builtin;
        return ElementType.getCustomTypeByName(name);
    }

    /**
     * Looks up any elemental type (built-in or custom) by damage type ID.
     * @return Optional containing the type, or empty if not found
     */
    @SuppressWarnings("unchecked")
    static Optional<? extends IElementalType> byDamageTypeId(String id) {
        if (id == null || id.isEmpty()) return Optional.empty();
        String cleanId = id.contains(":") ? id.substring(id.indexOf(":") + 1) : id;
        Optional<ElementType> builtin = ElementType.fromDamageTypeId(id);
        if (builtin.isPresent()) return builtin;
        return ElementType.getCustomTypeByDamageId(cleanId);
    }
}
