package com.auranite.abloom.util;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;

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
     * Namespace used to look up this element's icon texture.
     * <p>
     * Built-in elements resolve under the {@code abloom} namespace; custom elements can
     * override it (derived from their namespaced damage type id, e.g.
     * {@code "yourmod:plasma_dmg"} -> {@code "yourmod"}) so that mods can ship icons for
     * their own elements without touching Abloom resources.
     * @return the texture namespace, or null to fall back to "abloom"
     */
    @org.jetbrains.annotations.Nullable
    default String getTextureNamespace() { return null; }

    /**
     * Resolves the conventional icon texture location for this element:
     * {@code {namespace}:gui/element_icons/{name_lowercase}}.
     * <p>
     * Example: FIRE -> {@code abloom:gui/element_icons/fire},
     * custom PLASMA from yourmod -> {@code yourmod:gui/element_icons/plasma}.
     * @return the ResourceLocation of the icon texture (may not exist; check before use)
     */
    default ResourceLocation getElementIconTexture() {
        String ns = getTextureNamespace();
        if (ns == null || ns.isEmpty()) ns = "abloom";
        return ResourceLocation.fromNamespaceAndPath(ns, "gui/element_icons/" + name().toLowerCase(java.util.Locale.ROOT));
    }
    /**
     * Gets the dispersion bonus multiplier for this element type.
     * This value determines the additional damage bonus when the target has the DISPERSION effect.
     * Built-in elements have predefined values; custom elements can override this.
     * @return the dispersion bonus (e.g., 0.15f for 15%%, 0.30f for 30%%)
     */
    default float getDispersionBonus() { return 0.0f; }

    /**
     * Returns true if this element type is PRISMATIC (either built-in or via base_element).
     * Used to gate Prismatic conversion mechanics in ElementDamageHandler.
     */
    default boolean isPrismatic() { return false; }

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
