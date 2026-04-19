package com.auranite.abloom;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class ElementalWeaponUtils {

    private ElementalWeaponUtils() {}

    public static void registerItem(Item item, ElementType type) {
        registerItem(item, type, 1.0f);
    }

    public static void registerItem(Item vanillaItem, ElementType type, float accumulationMultiplier) {
        if (vanillaItem == null || type == null) return;
        ElementalWeaponRegistry.registerWeapon(vanillaItem, type, accumulationMultiplier);
        AbloomMod.LOGGER.info("⚔️ Registered item {} as {} elemental (accum x{})",
                BuiltInRegistries.ITEM.getKey(vanillaItem), type, accumulationMultiplier);
    }

    public static boolean registerItemById(String modId, String itemName, ElementType type) {
        return registerItemById(modId, itemName, type, 1.0f);
    }

    public static boolean registerItemById(String modId, String itemName, ElementType type, float accumulationMultiplier) {
        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(modId, itemName);
        Optional<Item> itemOpt = BuiltInRegistries.ITEM.getOptional(rl);

        if (itemOpt.isPresent()) {
            ElementalWeaponRegistry.registerWeapon(itemOpt.get(), type, accumulationMultiplier);
            AbloomMod.LOGGER.info("⚔️ Registered {}:{} as {} elemental (accum x{})", modId, itemName, type, accumulationMultiplier);
            return true;
        } else {
            AbloomMod.LOGGER.warn("❌ Item not found: {}:{}", modId, itemName);
            return false;
        }
    }

    @SafeVarargs
    public static void registerMultiple(ElementType type, Item... items) {
        registerMultiple(type, 1.0f, items);
    }

    @SafeVarargs
    public static void registerMultiple(ElementType type, float accumulationMultiplier, Item... items) {
        if (items == null) return;
        for (Item item : items) {
            if (item != null) {
                ElementalWeaponRegistry.registerWeapon(item, type, accumulationMultiplier);
            }
        }
        AbloomMod.LOGGER.info("⚔️ Registered {} items as {} elemental (accum x{})", items.length, type, accumulationMultiplier);
    }

    public static boolean isElemental(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return getElementType(stack) != null;
    }

    public static ElementType getElementType(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;

        Optional<ElementType> component = ElementalWeaponComponent.getElement(stack);
        if (component.isPresent()) {
            return component.get();
        }

        return ElementalWeaponRegistry.getElementType(stack);
    }

    public static float getAccumulationMultiplier(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 1.0f;

        float componentAccum = ElementalWeaponComponent.getAccumMultiplier(stack);
        if (componentAccum != 1.0f) {
            return componentAccum;
        }

        return ElementalWeaponRegistry.getAccumulationMultiplier(stack);
    }

    public static ItemStack addElementToStack(ItemStack stack, ElementType type) {
        return addElementToStackWithAccum(stack, type, 1.0f);
    }

    public static ItemStack addElementToStackWithAccum(ItemStack stack, ElementType type, float accumMultiplier) {
        if (stack == null || stack.isEmpty() || type == null) return stack;
        return ElementalWeaponComponent.withElementAndAccum(stack, type, accumMultiplier);
    }

    public static ItemStack removeElementFromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return stack;
        return ElementalWeaponComponent.removeElement(stack);
    }
}
