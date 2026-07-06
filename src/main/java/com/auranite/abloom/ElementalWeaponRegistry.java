package com.auranite.abloom;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.HashSet;
import java.util.Set;

public class ElementalWeaponRegistry {

	private static final Map<Item, WeaponData> WEAPON_DATA = new WeakHashMap<>();
	private static final Map<Identifier, WeaponData> WEAPON_DATA_BY_ID = new WeakHashMap<>();
	private static final Set<Identifier> BUILTIN_REGISTRATIONS = new HashSet<>();

	private ElementalWeaponRegistry() {}

	public static void registerWeapon(Item item, ElementType type, float accumulationMultiplier) {
		if (item == null || type == null) return;
		
		// Check for duplicates
		if (WEAPON_DATA.containsKey(item)) {
			AbloomMod.LOGGER.warn("Weapon {} already registered, skipping duplicate registration", item.getDescriptionId());
			return;
		}
		
		Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
		if (WEAPON_DATA_BY_ID.containsKey(itemId)) {
			AbloomMod.LOGGER.warn("Weapon {} already registered by ID, skipping duplicate registration", itemId);
			return;
		}
		
		WEAPON_DATA.put(item, new WeaponData(type, Math.max(0f, accumulationMultiplier)));
		WEAPON_DATA_BY_ID.put(itemId, new WeaponData(type, Math.max(0f, accumulationMultiplier)));
		AbloomMod.LOGGER.debug("Registered elemental weapon: {} → {} (accum: x{})",
				item.getDescriptionId(), type, accumulationMultiplier);
	}

	public static void registerWeapon(Item item, ElementType type) {
		registerWeapon(item, type, 1.0f);
	}

	/**
	 * Register weapon from datapack (builtin)
	 */
	public static void registerBuiltinWeapon(Identifier itemLocation, ElementType type, float accumulationMultiplier) {
		if (itemLocation == null || type == null) return;
		
		// Check for conflicts
		if (BUILTIN_REGISTRATIONS.contains(itemLocation)) {
			AbloomMod.LOGGER.warn("Duplicate builtin registration for {}: skipping", itemLocation);
			return;
		}
		
		// Try to get the item from registry
		var optionalItem = BuiltInRegistries.ITEM.getOptional(itemLocation);
		if (optionalItem.isPresent()) {
			Item item = optionalItem.get();
			registerWeapon(item, type, accumulationMultiplier);
			BUILTIN_REGISTRATIONS.add(itemLocation);
			WEAPON_DATA_BY_ID.put(itemLocation, new WeaponData(type, Math.max(0f, accumulationMultiplier)));
			AbloomMod.LOGGER.info("Registered builtin elemental weapon: {} → {} (accum: x{})",
					itemLocation, type, accumulationMultiplier);
		} else {
			AbloomMod.LOGGER.warn("Item not found for builtin registration: {}", itemLocation);
		}
	}

	public static WeaponData getWeaponData(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return null;
		return WEAPON_DATA.get(stack.getItem());
	}

	public static WeaponData getWeaponDataById(Identifier itemLocation) {
		if (itemLocation == null) return null;
		return WEAPON_DATA_BY_ID.get(itemLocation);
	}

	public static ElementType getElementType(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return ElementType.PHYSICAL;
		}
		WeaponData data = getWeaponData(stack);
		return data != null ? data.type() : ElementType.PHYSICAL;
	}

	public static boolean isBuiltinRegistered(Identifier itemLocation) {
		return BUILTIN_REGISTRATIONS.contains(itemLocation);
	}

	public static float getAccumulationMultiplier(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return 1.0f;
		WeaponData data = getWeaponData(stack);
		return data != null ? data.accumulationMultiplier() : 1.0f;
	}

	public static boolean canDealDamage(ItemStack stack) {
		return true;
	}

	@Deprecated
	public static boolean isElementalWeapon(ItemStack stack) {
		return getElementType(stack) != null;
	}

	public static void clear() {
		WEAPON_DATA.clear();
		WEAPON_DATA_BY_ID.clear();
		BUILTIN_REGISTRATIONS.clear();
	}

	public static int getRegisteredCount() {
		return WEAPON_DATA.size();
	}

	public record WeaponData(ElementType type, float accumulationMultiplier) {
		@Override
		public String toString() {
			return String.format("WeaponData{type=%s, accum=x%.2f}", type, accumulationMultiplier);
		}
	}
}
