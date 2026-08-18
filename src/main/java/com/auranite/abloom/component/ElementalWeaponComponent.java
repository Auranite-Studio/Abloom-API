package com.auranite.abloom.component;

import com.auranite.abloom.util.ElementType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

public class ElementalWeaponComponent {

    public static final String ELEMENT_TYPE_KEY = "element_type";
    public static final String ACCUM_POINTS_KEY = "accum_points";
    public static final String CRIT_CHANCE_KEY = "crit_chance";
    public static final String CRIT_DAMAGE_KEY = "crit_damage";

    private static final CustomData EMPTY_DATA = CustomData.EMPTY;

    public static ItemStack withElement(ItemStack stack, ElementType type) {
        return withElementAndAccum(stack, type, 1f, 0.0f, 0.0f);
    }

    public static ItemStack withElementAndAccum(ItemStack stack, ElementType type, float accumPoints) {
        return withElementAndAccum(stack, type, accumPoints, 0.0f, 0.0f);
    }

    public static ItemStack withElementAndAccum(ItemStack stack, ElementType type, float accumPoints, float critChance, float critDamage) {
        if (stack == null || stack.isEmpty() || type == null) return stack;

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, EMPTY_DATA);
        customData.update(tag -> {
            tag.putString(ELEMENT_TYPE_KEY, type.name());
            tag.putFloat(ACCUM_POINTS_KEY, accumPoints);
            tag.putFloat(CRIT_CHANCE_KEY, critChance);
            tag.putFloat(CRIT_DAMAGE_KEY, critDamage);
        });
        stack.set(DataComponents.CUSTOM_DATA, customData);

        return stack;
    }

    public static Optional<ElementType> getElement(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Optional.empty();

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return Optional.empty();

        String typeName = customData.copyTag().getStringOr(ELEMENT_TYPE_KEY,"");
        return typeName.isEmpty() ? Optional.empty() : Optional.ofNullable(ElementType.safeValueOf(typeName));
    }

    public static float getAccumMultiplier(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 1.0f;

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return 1.0f;

        return customData.copyTag().getFloatOr(ACCUM_POINTS_KEY, 0);
    }

    public static float getCritChance(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0.0f;

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return 0.0f;

        return customData.copyTag().getFloatOr(CRIT_CHANCE_KEY, 0);
    }

    public static float getCritDamage(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0.0f;

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return 0.0f;

        return customData.copyTag().getFloatOr(CRIT_DAMAGE_KEY, 0);
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
                tag.remove(ACCUM_POINTS_KEY);
                tag.remove(CRIT_CHANCE_KEY);
                tag.remove(CRIT_DAMAGE_KEY);
            });
            stack.set(DataComponents.CUSTOM_DATA, customData);
        }

        return stack;
    }
}
