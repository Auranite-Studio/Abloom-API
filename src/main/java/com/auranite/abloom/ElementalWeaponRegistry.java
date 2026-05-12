package com.auranite.abloom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.WeakHashMap;

public class ElementalWeaponRegistry {

	private static final Map<Item, WeaponData> WEAPON_DATA = new WeakHashMap<>();

	private ElementalWeaponRegistry() {}

	public static void registerWeapon(Item item, ElementType type, float accumulationMultiplier) {
		if (item == null || type == null) return;
		WEAPON_DATA.put(item, new WeaponData(type.getDamageTypeId(), Math.max(0f, accumulationMultiplier)));
		AbloomMod.LOGGER.debug("⚔️ Registered elemental weapon: {} → {} (accum: x{})",
				item.getDescriptionId(), type, accumulationMultiplier);
	}

	public static void registerWeapon(Item item, ElementType type) {
		registerWeapon(item, type, 1.0f);
	}

	/**
	 * Register a weapon with a custom damage type ID.
	 * @param item the item
	 * @param damageTypeId the custom damage type ID
	 * @param accumulationMultiplier the accumulation multiplier
	 */
	public static void registerWeapon(Item item, String damageTypeId, float accumulationMultiplier) {
		if (item == null || damageTypeId == null) return;
		WEAPON_DATA.put(item, new WeaponData(damageTypeId, Math.max(0f, accumulationMultiplier)));
		AbloomMod.LOGGER.debug("⚔️ Registered custom elemental weapon: {} → {} (accum: x{})",
				item.getDescriptionId(), damageTypeId, accumulationMultiplier);
	}

	public static WeaponData getWeaponData(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return null;
		return WEAPON_DATA.get(stack.getItem());
	}

	public static ElementType getElementType(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return ElementType.PHYSICAL;
		}
		WeaponData data = getWeaponData(stack);
		if (data != null && data.damageTypeId() != null) {
			// Try to map back to built-in ElementType for compatibility
			return ElementType.fromDamageTypeId(data.damageTypeId()).orElse(ElementType.PHYSICAL);
		}
		return ElementType.PHYSICAL;
	}

	/**
	 * Get the damage type ID for a weapon (built-in or custom).
	 * @param stack the item stack
	 * @return the damage type ID, or null if not found
	 */
	public static String getDamageTypeId(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return null;
		WeaponData data = getWeaponData(stack);
		return data != null ? data.damageTypeId() : null;
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
	}

	public static int getRegisteredCount() {
		return WEAPON_DATA.size();
	}

	public record WeaponData(String damageTypeId, float accumulationMultiplier) {
		@Override
		public String toString() {
			return String.format("WeaponData{damageTypeId=%s, accum=x%.2f}", damageTypeId, accumulationMultiplier);
		}
	}
}
