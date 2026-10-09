package com.auranite.abloom.component;

import com.auranite.abloom.util.IElementalType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.HashMap;
import java.util.Map;

public class ElementalResistanceComponent {

    public static final String ELEMENT_RESISTANCE_KEY = "elemental_resistance_bonus";
    public static final String RESISTANCE_ENTRY_KEY = "resistance_entry";
    public static final String ELEMENT_TYPE_KEY = "element_type";
    public static final String RESISTANCE_VALUE_KEY = "resistance_value";

    private static final float MIN_RESISTANCE = -0.99f;
    private static final float MAX_RESISTANCE = 0.99f;

    public static CustomData createDefaultResistanceData(Map<IElementalType, Float> resistanceMap) {
        if (resistanceMap == null || resistanceMap.isEmpty()) {
            return CustomData.EMPTY;
        }

        return CustomData.EMPTY.update(tag -> {
            var resistanceTag = tag.getCompound(ELEMENT_RESISTANCE_KEY);
            for (Map.Entry<IElementalType, Float> entry : resistanceMap.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    float clampedValue = Math.max(-0.99f, Math.min(0.99f, entry.getValue()));
                    resistanceTag.putFloat(entry.getKey().name(), clampedValue);
                }
            }
            tag.put(ELEMENT_RESISTANCE_KEY, resistanceTag);
        });
    }

    public static ItemStack withResistance(ItemStack stack, IElementalType type, float resistance) {
        if (stack == null || stack.isEmpty() || type == null) return stack;

        final float clampedResistance = Math.max(-0.99f, Math.min(0.99f, resistance));
        final IElementalType finalType = type;

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        customData = customData.update(tag -> {
            var resistanceTag = tag.getCompound(ELEMENT_RESISTANCE_KEY);
            resistanceTag.putFloat(finalType.name(), clampedResistance);
            tag.put(ELEMENT_RESISTANCE_KEY, resistanceTag);
        });
        stack.set(DataComponents.CUSTOM_DATA, customData);

        return stack;
    }

    public static ItemStack withResistances(ItemStack stack, Map<IElementalType, Float> resistanceMap) {
        if (stack == null || stack.isEmpty() || resistanceMap == null) return stack;

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        customData = customData.update(tag -> {
            var resistanceTag = tag.getCompound(ELEMENT_RESISTANCE_KEY);
            for (Map.Entry<IElementalType, Float> entry : resistanceMap.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    float clampedValue = Math.max(-0.99f, Math.min(0.99f, entry.getValue()));
                    resistanceTag.putFloat(entry.getKey().name(), clampedValue);
                }
            }
            tag.put(ELEMENT_RESISTANCE_KEY, resistanceTag);
        });
        stack.set(DataComponents.CUSTOM_DATA, customData);

        return stack;
    }

    public static float getResistance(ItemStack stack, IElementalType type) {
        if (stack == null || stack.isEmpty() || type == null) return 0.0f;

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return 0.0f;

        var tag = customData.copyTag();
        if (!tag.contains(ELEMENT_RESISTANCE_KEY)) return 0.0f;

        var resistanceTag = tag.getCompound(ELEMENT_RESISTANCE_KEY);
        return resistanceTag.contains(type.name()) ? resistanceTag.getFloat(type.name()) : 0.0f;
    }

    @SuppressWarnings("deprecation")
    public static Map<IElementalType, Float> getAllResistances(ItemStack stack) {
        Map<IElementalType, Float> result = new HashMap<>();

        if (stack == null || stack.isEmpty()) return result;

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return result;

        var tag = customData.copyTag();
        if (!tag.contains(ELEMENT_RESISTANCE_KEY)) return result;

        var resistanceTag = tag.getCompound(ELEMENT_RESISTANCE_KEY);

        // Check built-in types
        for (com.auranite.abloom.util.ElementType type : com.auranite.abloom.util.ElementType.values()) {
            if (resistanceTag.contains(type.name())) {
                result.put(type, resistanceTag.getFloat(type.name()));
            }
        }

        // Check custom types
        for (String customName : com.auranite.abloom.util.ElementType.getCustomTypeNames()) {
            if (resistanceTag.contains(customName)) {
                com.auranite.abloom.util.ElementType.CustomElementType customType = 
                    com.auranite.abloom.util.ElementType.getCustomTypeByName(customName).orElse(null);
                if (customType != null) {
                    result.put(customType, resistanceTag.getFloat(customName));
                }
            }
        }

        return result;
    }

    public static boolean hasResistance(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return false;

        var tag = customData.copyTag();
        if (!tag.contains(ELEMENT_RESISTANCE_KEY)) return false;

        var resistanceTag = tag.getCompound(ELEMENT_RESISTANCE_KEY);

        // Check built-in types
        for (com.auranite.abloom.util.ElementType type : com.auranite.abloom.util.ElementType.values()) {
            if (resistanceTag.contains(type.name()) && resistanceTag.getFloat(type.name()) != 0.0f) {
                return true;
            }
        }

        // Check custom types
        for (String customName : com.auranite.abloom.util.ElementType.getCustomTypeNames()) {
            if (resistanceTag.contains(customName) && resistanceTag.getFloat(customName) != 0.0f) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasResistance(ItemStack stack, IElementalType type) {
        return getResistance(stack, type) != 0.0f;
    }

    public static ItemStack removeResistance(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return stack;

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            customData = customData.update(tag -> tag.remove(ELEMENT_RESISTANCE_KEY));
            stack.set(DataComponents.CUSTOM_DATA, customData);
        }

        return stack;
    }

    public static ItemStack removeResistance(ItemStack stack, IElementalType type) {
        if (stack == null || stack.isEmpty() || type == null) return stack;

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            customData = customData.update(tag -> {
                if (tag.contains(ELEMENT_RESISTANCE_KEY)) {
                    var resistanceTag = tag.getCompound(ELEMENT_RESISTANCE_KEY);
                    resistanceTag.remove(type.name());

                    if (resistanceTag.isEmpty()) {
                        tag.remove(ELEMENT_RESISTANCE_KEY);
                    } else {
                        tag.put(ELEMENT_RESISTANCE_KEY, resistanceTag);
                    }
                }
            });
            stack.set(DataComponents.CUSTOM_DATA, customData);
        }

        return stack;
    }
}
