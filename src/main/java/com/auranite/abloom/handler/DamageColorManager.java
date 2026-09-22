package com.auranite.abloom.handler;

import com.auranite.abloom.util.ElementType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Manages damage display colors for each element type.
 */
public class DamageColorManager {

    private static final Map<ElementType, Integer> DAMAGE_COLORS = new EnumMap<>(ElementType.class);

    private DamageColorManager() {
        // Utility class
    }

    /**
     * Initializes damage colors for all element types.
     * Must be called during mod initialization before any displays are spawned.
     */
    public static void initDamageColors() {
        DAMAGE_COLORS.put(ElementType.FIRE, 0xFF5500);
        DAMAGE_COLORS.put(ElementType.PHYSICAL, 0xC0C0C0);
        DAMAGE_COLORS.put(ElementType.WIND, 0x00FFFF);
        DAMAGE_COLORS.put(ElementType.WATER, 0x0080FF);
        DAMAGE_COLORS.put(ElementType.EARTH, 0x8B4513);
        DAMAGE_COLORS.put(ElementType.ICE, 0x00BFFF);
        DAMAGE_COLORS.put(ElementType.ELECTRIC, 0xFF19FF);
        DAMAGE_COLORS.put(ElementType.ENERGY, 0xFFFF00);
        DAMAGE_COLORS.put(ElementType.NATURAL, 0x32CD32);
        DAMAGE_COLORS.put(ElementType.QUANTUM, 0x9400D3);
        DAMAGE_COLORS.put(ElementType.ETHER, 0x24B3A7);
        DAMAGE_COLORS.put(ElementType.LIGHT, 0xFFF1A5);
        DAMAGE_COLORS.put(ElementType.SHADOW, 0x4B0082);
        DAMAGE_COLORS.put(ElementType.PRISMATIC, 0xFFFFFF);
    }

    /**
     * Gets the display color for a specific element type.
     *
     * @param type the element type
     * @return the color as an integer RGB value, or white (0xFFFFFF) if type is null
     */
    public static int getDamageColor(ElementType type) {
        if (type == null) return 0xFFFFFF;
        return DAMAGE_COLORS.getOrDefault(type, 0xFFFFFF);
    }
}
