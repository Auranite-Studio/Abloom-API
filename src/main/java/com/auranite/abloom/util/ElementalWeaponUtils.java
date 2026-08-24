package com.auranite.abloom.util;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.component.ElementalWeaponComponent;
import com.auranite.abloom.registries.ElementalWeaponRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

/**
 * Utility class for elemental weapon registration and manipulation.
 * Provides convenient methods for registering items as elemental weapons,
 * checking elemental properties, and modifying item stacks.
 * Supports both single-element and multi-stage elemental weapons.
 */
public class ElementalWeaponUtils {

    private static final String ACCUM_POINTS_KEY = "accum_points";

    private ElementalWeaponUtils() {}

    /**
     * Registers an item as a elemental weapon with default accumulation multiplier.
     * @param item the item to register
     * @param type the elemental type
     */
    public static void registerItem(Item item, ElementType type) {
        registerItem(item, type, 1.0f);
    }

    /**
     * Registers an item as a elemental weapon with custom accumulation multiplier.
     * Skips registration if the item was already registered via datapack.
     * @param vanillaItem the item to register
     * @param type the elemental type
     * @param accumulationMultiplier the accumulation multiplier (>= 0)
     */
    public static void registerItem(Item vanillaItem, ElementType type, float accumulationMultiplier) {
        if (vanillaItem == null || type == null) return;
        
        // Check if already registered via datapack
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(vanillaItem);
        if (ElementalWeaponRegistry.isBuiltinRegistered(itemId)) {
            AbloomMod.LOGGER.debug("Item {} already registered via datapack, skipping code registration", itemId);
            return;
        }
        
        ElementalWeaponRegistry.registerWeapon(vanillaItem, type, Math.max(0f, accumulationMultiplier));
        AbloomMod.LOGGER.info("Registered item {} as {} elemental (accum x{})",
                itemId, type, accumulationMultiplier);
    }

    /**
     * Registers an item by its resource location string with default accumulation multiplier.
     * @param modId the mod ID
     * @param itemName the item name
     * @param type the elemental type
     * @return true if registration succeeded, false if item not found
     */
    public static boolean registerItemById(String modId, String itemName, ElementType type) {
        return registerItemById(modId, itemName, type, 1.0f);
    }

    /**
     * Registers an item by its resource location string with custom accumulation multiplier.
     * @param modId the mod ID
     * @param itemName the item name
     * @param type the elemental type
     * @param accumulationMultiplier the accumulation multiplier
     * @return true if registration succeeded, false if item not found
     */
    public static boolean registerItemById(String modId, String itemName, ElementType type, float accumulationMultiplier) {
        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(modId, itemName);
        Optional<Item> itemOpt = BuiltInRegistries.ITEM.getOptional(rl);

        if (itemOpt.isPresent()) {
            ElementalWeaponRegistry.registerWeapon(itemOpt.get(), type, accumulationMultiplier);
            AbloomMod.LOGGER.info("Registered {}:{} as {} elemental (accum x{})", modId, itemName, type, accumulationMultiplier);
            return true;
        } else {
            AbloomMod.LOGGER.warn("Item not found: {}:{} ", modId, itemName);
            return false;
        }
    }

    /**
     * Registers multiple items with the same elemental type and default accumulation multiplier.
     * @param type the elemental type
     * @param items the items to register
     */
    @SafeVarargs
    public static void registerMultiple(ElementType type, Item... items) {
        registerMultiple(type, 1.0f, items);
    }

    /**
     * Registers multiple items with the same elemental type and custom accumulation multiplier.
     * @param type the elemental type
     * @param accumulationMultiplier the accumulation multiplier
     * @param items the items to register
     */
    @SafeVarargs
    public static void registerMultiple(ElementType type, float accumulationMultiplier, Item... items) {
        if (items == null || items.length == 0) return;
        int registered = 0;
        for (Item item : items) {
            if (item != null) {
                ElementalWeaponRegistry.registerWeapon(item, type, Math.max(0f, accumulationMultiplier));
                registered++;
            }
        }
        if (registered > 0) {
            AbloomMod.LOGGER.info("Registered {} items as {} elemental (accum x{})", registered, type, accumulationMultiplier);
        }
    }

    /**
     * Checks if an item stack is elemental.
     * @param stack the item stack
     * @return true if the item has an elemental type (including multi-stage)
     */
    public static boolean isElemental(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return getElementType(stack) != null || isMultiStageWeapon(stack);
    }

    /**
     * Checks if an item stack is a multi-stage elemental weapon.
     * @param stack the item stack
     * @return true if the item has stage-based attacks
     */
    public static boolean isMultiStageWeapon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return ElementalWeaponRegistry.hasStages(itemId);
    }

    /**
     * Gets the elemental type of an item stack.
     * @param stack the item stack
     * @return the elemental type, or null if not elemental (or multi-stage)
     */
    public static ElementType getElementType(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;

        Optional<ElementType> component = ElementalWeaponComponent.getElement(stack);
        if (component.isPresent()) {
            return component.get();
        }

        return ElementalWeaponRegistry.getElementType(stack);
    }

    /**
     * Gets the accumulation multiplier of an item stack.
     * For multi-stage weapons, returns the total accumulation across all stages.
     * @param stack the item stack
     * @return the accumulation multiplier (1.0f if none)
     */
    public static float getAccumulationMultiplier(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 1.0f;

        // First check component data
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            // Component exists, check if it has accum_points
            var tag = customData.copyTag();
            if (tag.contains(ACCUM_POINTS_KEY)) {
                return tag.getFloat(ACCUM_POINTS_KEY);
            }
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        
        // If multi-stage, return total accumulation
        if (ElementalWeaponRegistry.hasStages(itemId)) {
            return ElementalWeaponRegistry.getTotalAccumulationMultiplier(itemId);
        }
        
        float registryAccum = ElementalWeaponRegistry.getAccumulationMultiplier(stack);
        return registryAccum != 1.0f ? registryAccum : 1.0f;
    }

    /**
     * Gets the critical hit chance of an item stack (sum of component and registry values).
     * @param stack the item stack
     * @return the total crit chance (0.0f to 1.0f)
     */
    public static float getCritChance(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0.0f;
        return ElementalWeaponRegistry.getCritChance(stack) + ElementalWeaponComponent.getCritChance(stack);
    }

    /**
     * Gets the critical hit damage multiplier of an item stack (sum of component and registry values).
     * @param stack the item stack
     * @return the total crit damage multiplier (0.0f to 10.0f)
     */
    public static float getCritDamage(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0.0f;
        return ElementalWeaponRegistry.getCritDamage(stack) + ElementalWeaponComponent.getCritDamage(stack);
    }

    /**
     * Adds elemental type to an item stack with default accumulation multiplier.
     * @param stack the item stack
     * @param type the elemental type
     * @return a new stack with the elemental property
     */
    public static ItemStack addElementToStack(ItemStack stack, ElementType type) {
        return addElementToStackWithAccum(stack, type, 1.0f);
    }

    /**
     * Adds elemental type to an item stack with custom accumulation multiplier.
     * @param stack the item stack
     * @param type the elemental type
     * @param accumPoints the accumulation multiplier
     * @return a new stack with the elemental property
     */
    public static ItemStack addElementToStackWithAccum(ItemStack stack, ElementType type, float accumPoints) {
        if (stack == null || stack.isEmpty() || type == null) return stack;
        return ElementalWeaponComponent.withElementAndAccum(stack, type, accumPoints);
    }

    /**
     * Removes elemental property from an item stack.
     * @param stack the item stack
     * @return a new stack without elemental properties
     */
    public static ItemStack removeElementFromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return stack;
        return ElementalWeaponComponent.removeElement(stack);
    }
}
