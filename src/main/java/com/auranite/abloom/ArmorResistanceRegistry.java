package com.auranite.abloom;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.HashSet;
import java.util.Set;

public class ArmorResistanceRegistry {

    private static final Map<Item, Map<ElementType, Float>> ARMOR_RESISTANCES = new WeakHashMap<>();
    private static final Map<Identifier, Map<ElementType, Float>> ARMOR_RESISTANCES_BY_ID = new WeakHashMap<>();
    private static final Set<Identifier> BUILTIN_REGISTRATIONS = new HashSet<>();

    private ArmorResistanceRegistry() {}

    public static void registerArmor(Item item, Map<ElementType, Float> resistanceMap) {
        if (item == null || resistanceMap == null || resistanceMap.isEmpty()) return;
        
        Map<ElementType, Float> clampedMap = new EnumMap<>(ElementType.class);
        for (Map.Entry<ElementType, Float> entry : resistanceMap.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                float clampedValue = Math.max(-0.99f, Math.min(0.99f, entry.getValue()));
                clampedMap.put(entry.getKey(), clampedValue);
            }
        }
        
        ARMOR_RESISTANCES.put(item, clampedMap);
        AbloomMod.LOGGER.debug("Registered armor resistance: {} → {}", 
            item.getDescriptionId(), clampedMap);
    }

    public static void registerArmor(Item item, ElementType type, float resistance) {
        if (item == null || type == null) return;
        registerArmor(item, Map.of(type, resistance));
    }

    /**
     * Register armor from datapack (builtin)
     */
    public static void registerBuiltinArmor(Identifier itemLocation, Map<ElementType, Float> resistanceMap) {
        if (itemLocation == null || resistanceMap == null || resistanceMap.isEmpty()) return;
        
        // Check for conflicts
        if (BUILTIN_REGISTRATIONS.contains(itemLocation)) {
            AbloomMod.LOGGER.warn("Duplicate builtin registration for {}: skipping", itemLocation);
            return;
        }
        
        // Clamp values
        Map<ElementType, Float> clampedMap = new EnumMap<>(ElementType.class);
        for (Map.Entry<ElementType, Float> entry : resistanceMap.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                float clampedValue = Math.max(-0.99f, Math.min(0.99f, entry.getValue()));
                clampedMap.put(entry.getKey(), clampedValue);
            }
        }
        
        // Try to get the item from registry
        var optionalItem = BuiltInRegistries.ITEM.getOptional(itemLocation);
        if (optionalItem.isPresent()) {
            Item item = optionalItem.get();
            registerArmor(item, clampedMap);
            BUILTIN_REGISTRATIONS.add(itemLocation);
            ARMOR_RESISTANCES_BY_ID.put(itemLocation, clampedMap);
            AbloomMod.LOGGER.info("Registered builtin armor resistance: {} → {}", 
                itemLocation, clampedMap);
        } else {
            AbloomMod.LOGGER.warn("Item not found for builtin registration: {}", itemLocation);
        }
    }

    public static Map<ElementType, Float> getResistances(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Map.of();
        return ARMOR_RESISTANCES.getOrDefault(stack.getItem(), Map.of());
    }

    public static Map<ElementType, Float> getResistancesById(Identifier itemLocation) {
        if (itemLocation == null) return Map.of();
        return ARMOR_RESISTANCES_BY_ID.getOrDefault(itemLocation, Map.of());
    }

    public static float getResistance(ItemStack stack, ElementType type) {
        if (stack == null || stack.isEmpty() || type == null) return 0.0f;
        Map<ElementType, Float> resistances = getResistances(stack);
        return resistances.getOrDefault(type, 0.0f);
    }

    public static boolean isBuiltinRegistered(Identifier itemLocation) {
        return BUILTIN_REGISTRATIONS.contains(itemLocation);
    }

    public static boolean hasResistances(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return !getResistances(stack).isEmpty();
    }

    public static void clear() {
        ARMOR_RESISTANCES.clear();
        ARMOR_RESISTANCES_BY_ID.clear();
        BUILTIN_REGISTRATIONS.clear();
    }

    public static int getRegisteredCount() {
        return ARMOR_RESISTANCES.size();
    }
    
    /**
     * Get all registered armor resistances by item location
     */
    public static Map<Identifier, Map<ElementType, Float>> getAllRegisteredResistances() {
        return new java.util.HashMap<>(ARMOR_RESISTANCES_BY_ID);
    }
}
