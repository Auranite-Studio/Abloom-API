package com.auranite.abloom;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

public class ElementalWeaponComponent {

    public static final String ELEMENT_TYPE_KEY = "element_type";
    public static final String ACCUM_MULTIPLIER_KEY = "accum_multiplier";

    public static ItemStack withElement(ItemStack stack, ElementType type) {
        return withElementAndAccum(stack, type, 1f);
    }

    public static ItemStack withElementAndAccum(ItemStack stack, ElementType type, float accumMultiplier) {
        if (stack == null || stack.isEmpty() || type == null) return stack;

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        customData.update(tag -> {
            tag.putString(ELEMENT_TYPE_KEY, type.name());
            tag.putFloat(ACCUM_MULTIPLIER_KEY, accumMultiplier);
        });
        stack.set(DataComponents.CUSTOM_DATA, customData);

        return stack;
    }

    public static Optional<ElementType> getElement(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Optional.empty();

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return Optional.empty();

        String typeName = customData.copyTag().getString(ELEMENT_TYPE_KEY);
        if (typeName.isEmpty()) return Optional.empty();

        try {
            return Optional.of(ElementType.valueOf(typeName));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static float getAccumMultiplier(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 1.0f;

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return 1.0f;

        return customData.copyTag().getFloat(ACCUM_MULTIPLIER_KEY);
    }

    public static boolean hasElement(ItemStack stack) {
        return getElement(stack).isPresent();
    }

    public static ItemStack removeElement(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return stack;

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            customData.update(tag -> {
                tag.remove(ELEMENT_TYPE_KEY);
                tag.remove(ACCUM_MULTIPLIER_KEY);
            });
            stack.set(DataComponents.CUSTOM_DATA, customData);
        }

        return stack;
    }
}
