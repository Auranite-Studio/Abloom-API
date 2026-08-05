package com.auranite.abloom;

import com.auranite.abloom.datapack.ElementalWeaponData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ElementalWeaponRegistry {

	private static final Map<Item, WeaponData> WEAPON_DATA = new WeakHashMap<>();
	private static final Map<ResourceLocation, WeaponData> WEAPON_DATA_BY_ID = new WeakHashMap<>();
	private static final Set<ResourceLocation> BUILTIN_REGISTRATIONS = new HashSet<>();

	// Stage tracking: maps weapon item location to list of stage data
	private static final Map<ResourceLocation, List<StageData>> WEAPON_STAGES = new WeakHashMap<>();

	// Base element tracking for multi-stage weapons (used for tooltip display)
	private static final Map<ResourceLocation, ElementType> WEAPON_BASE_ELEMENTS = new WeakHashMap<>();

	// Cooldown tracking: tracks last attack time for each (attacker, target) pair
	private static final Map<String, Long> STAGE_COOLDOWN_TRACKER = new ConcurrentHashMap<>();

	// Default attack speed if entity doesn't have the attribute
	private static final float DEFAULT_ATTACK_SPEED = 1.6f;

	private ElementalWeaponRegistry() {}

	public static void registerWeapon(Item item, ElementType type, float accumulationMultiplier) {
		registerWeapon(item, type, accumulationMultiplier, 0.0f, 0.0f);
	}

	public static void registerWeapon(Item item, ElementType type, float accumulationMultiplier, float critChance, float critDamage) {
		if (item == null || type == null) return;
		
		// Check for duplicates
		if (WEAPON_DATA.containsKey(item)) {
			AbloomMod.LOGGER.warn("Weapon {} already registered, skipping duplicate registration", item.getDescriptionId());
			return;
		}
		
		ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
		if (WEAPON_DATA_BY_ID.containsKey(itemId)) {
			AbloomMod.LOGGER.warn("Weapon {} already registered by ID, skipping duplicate registration", itemId);
			return;
		}
		
		WEAPON_DATA.put(item, new WeaponData(type, Math.max(0f, accumulationMultiplier), critChance, critDamage));
		WEAPON_DATA_BY_ID.put(itemId, new WeaponData(type, Math.max(0f, accumulationMultiplier), critChance, critDamage));
		AbloomMod.LOGGER.debug("Registered elemental weapon: {} -> {} (accum: x{}, crit: {:.0f}%/{:.0f}%)",
				item.getDescriptionId(), type, accumulationMultiplier, critChance * 100, critDamage * 100);
	}

	public static void registerWeapon(Item item, ElementType type) {
		registerWeapon(item, type, 1.0f);
	}

	/**
	 * Register weapon from datapack (builtin)
	 */
	public static void registerBuiltinWeapon(ResourceLocation itemLocation, ElementType type, float accumulationMultiplier) {
		registerBuiltinWeapon(itemLocation, type, accumulationMultiplier, 0.0f, 0.0f);
	}

	public static void registerBuiltinWeapon(ResourceLocation itemLocation, ElementType type, float accumulationMultiplier, float critChance, float critDamage) {
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
			registerWeapon(item, type, accumulationMultiplier, critChance, critDamage);
			BUILTIN_REGISTRATIONS.add(itemLocation);
			WEAPON_DATA_BY_ID.put(itemLocation, new WeaponData(type, Math.max(0f, accumulationMultiplier), critChance, critDamage));
			AbloomMod.LOGGER.info("Registered builtin elemental weapon: {} -> {} (accum: x{}, crit: {:.0f}%/{:.0f}%)",
					itemLocation, type, accumulationMultiplier, critChance * 100, critDamage * 100);
		} else {
			AbloomMod.LOGGER.warn("Item not found for builtin registration: {}", itemLocation);
		}
	}

	/**
	 * Register a multi-stage elemental weapon.
	 * Each stage has its own element type and accumulation multiplier.
	 * 
	 * @param itemLocation The item resource location
	 * @param stageNumber The stage number (1-based, max 4)
	 * @param stageElement The element type for this stage
	 * @param stageAccumulation The accumulation multiplier for this stage
	 * @param critChance Critical hit chance (shared across all stages)
	 * @param critDamage Critical hit damage multiplier (shared across all stages)
	 */
	public static void registerBuiltinWeaponWithStage(ResourceLocation itemLocation, int stageNumber, 
			                                                 ElementType stageElement, float stageAccumulation,
			                                                 float critChance, float critDamage) {
		if (itemLocation == null || stageElement == null) return;
		if (stageNumber < 1 || stageNumber > ElementalWeaponData.MAX_STAGES) {
			AbloomMod.LOGGER.warn("Invalid stage number {} for weapon {}, must be 1-{}", 
					stageNumber, itemLocation, ElementalWeaponData.MAX_STAGES);
			return;
		}
		
		// Skip duplicate check - stages can be registered multiple times
		// We only want to add to BUILTIN_REGISTRATIONS once after all stages are registered

		StageData stageData = new StageData(stageNumber, stageElement, stageAccumulation);
		
		// Get or create stages list
		List<StageData> stages = WEAPON_STAGES.computeIfAbsent(itemLocation, k -> new ArrayList<>());
		
		// Check if this stage already exists
		boolean stageFound = false;
		for (int i = 0; i < stages.size(); i++) {
			if (stages.get(i).stageNumber() == stageNumber) {
				stages.set(i, stageData);
				stageFound = true;
				break;
			}
		}
		if (!stageFound) {
			stages.add(stageData);
		}
		
		// Sort stages by number
		stages.sort((a, b) -> Integer.compare(a.stageNumber(), b.stageNumber()));
		
		// Clamp to max stages
		while (stages.size() > ElementalWeaponData.MAX_STAGES) {
			stages.remove(stages.size() - 1);
		}
		
		// Mark as builtin registered and register weapon with first stage only once
		if (!BUILTIN_REGISTRATIONS.contains(itemLocation)) {
			BUILTIN_REGISTRATIONS.add(itemLocation);

			var optionalItem = BuiltInRegistries.ITEM.getOptional(itemLocation);
			if (optionalItem.isPresent()) {
				Item item = optionalItem.get();
				// For multi-stage weapons, register with first stage as fallback
				StageData firstStage = stages.get(0);
				registerWeapon(item, firstStage.element(), firstStage.accumulation(), critChance, critDamage);
				WEAPON_DATA_BY_ID.put(itemLocation, new WeaponData(firstStage.element(), Math.max(0f, firstStage.accumulation()), critChance, critDamage));
				AbloomMod.LOGGER.info("Registered multi-stage weapon: {} with {} stages", itemLocation, stages.size());
			}
		}

		// Set base element from first stage as fallback (only if not already set by provider)
		if (!stages.isEmpty() && !WEAPON_BASE_ELEMENTS.containsKey(itemLocation)) {
			StageData firstStage = stages.get(0);
			setBaseElement(itemLocation, firstStage.element());
		}
	}

	/**
	 * Check if a weapon has stage-based attacks.
	 */
	public static boolean hasStages(ResourceLocation itemLocation) {
		return WEAPON_STAGES.containsKey(itemLocation) && !WEAPON_STAGES.get(itemLocation).isEmpty();
	}

	/**
	 * Set the base element for a multi-stage weapon.
	 */
	public static void setBaseElement(ResourceLocation itemLocation, ElementType baseElement) {
		if (itemLocation != null && baseElement != null) {
			WEAPON_BASE_ELEMENTS.put(itemLocation, baseElement);
		}
	}

	/**
	 * Get the base element for a multi-stage weapon.
	 */
	public static ElementType getBaseElement(ResourceLocation itemLocation) {
		return WEAPON_BASE_ELEMENTS.get(itemLocation);
	}

	/**
	 * Get the list of stages for a weapon.
	 */
	public static List<StageData> getStages(ResourceLocation itemLocation) {
		List<StageData> stages = WEAPON_STAGES.get(itemLocation);
		return stages != null ? Collections.unmodifiableList(stages) : Collections.emptyList();
	}

	/**
	 * Get the current stage data for a weapon.
	 */
	public static StageData getCurrentStage(ResourceLocation itemLocation) {
		List<StageData> stages = getStages(itemLocation);
		return stages.isEmpty() ? null : stages.get(0); // Return first stage by default
	}

	/**
	 * Get stage data by stage number.
	 */
	public static StageData getStageByNumber(ResourceLocation itemLocation, int stageNumber) {
		List<StageData> stages = getStages(itemLocation);
		return stages.stream()
				.filter(s -> s.stageNumber() == stageNumber)
				.findFirst()
				.orElse(null);
	}

	/**
	 * Get total accumulation multiplier for all stages (for tooltip display).
	 */
	public static float getTotalAccumulationMultiplier(ResourceLocation itemLocation) {
		List<StageData> stages = getStages(itemLocation);
		float total = 0f;
		for (StageData stage : stages) {
			total += stage.accumulation();
		}
		return total > 0 ? total : 1f;
	}

	public static WeaponData getWeaponData(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return null;
		return WEAPON_DATA.get(stack.getItem());
	}

	public static WeaponData getWeaponDataById(ResourceLocation itemLocation) {
		if (itemLocation == null) return null;
		return WEAPON_DATA_BY_ID.get(itemLocation);
	}

	public static ElementType getElementType(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return ElementType.PHYSICAL;
		}

		ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

		// If weapon has stages, return base element if set, otherwise null
		if (hasStages(itemId)) {
			ElementType baseElement = getBaseElement(itemId);
			return baseElement;
		}

		WeaponData data = getWeaponData(stack);
		return data != null ? data.type() : ElementType.PHYSICAL;
	}

	public static float getCritChance(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return 0.0f;
		WeaponData data = getWeaponData(stack);
		return data != null ? data.critChance() : 0.0f;
	}

	public static float getCritDamage(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return 0.0f;
		WeaponData data = getWeaponData(stack);
		return data != null ? data.critDamage() : 0.0f;
	}

	public static boolean isBuiltinRegistered(ResourceLocation itemLocation) {
		return BUILTIN_REGISTRATIONS.contains(itemLocation);
	}

	public static float getAccumulationMultiplier(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return 1.0f;
		
		ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
		
		// If weapon has stages, return total accumulation
		if (hasStages(itemId)) {
			return getTotalAccumulationMultiplier(itemId);
		}
		
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
		WEAPON_STAGES.clear();
		WEAPON_BASE_ELEMENTS.clear();
	}

	public static int getRegisteredCount() {
		return WEAPON_DATA.size();
	}

	public record WeaponData(ElementType type, float accumulationMultiplier, float critChance, float critDamage) {
		@Override
		public String toString() {
			return String.format("WeaponData{type=%s, accum=x%.2f, crit=%.0f%%/%.0f%%}", type, accumulationMultiplier, critChance * 100, critDamage * 100);
		}
	}

	/**
	 * Represents a single stage of a multi-stage elemental weapon.
	 */
	public record StageData(int stageNumber, ElementType element, float accumulation) {
		@Override
		public String toString() {
			return String.format("Stage%d{%s, accum=x%.2f}", stageNumber, element, accumulation);
		}
	}

	/**
	 * Get the attack cooldown based on entity's attack speed + 1 second.
	 * @param attacker the attacking entity
	 * @return cooldown in milliseconds
	 */
	public static long getAttackCooldown(LivingEntity attacker) {
		if (attacker == null) {
			return (long) ((1.0f / DEFAULT_ATTACK_SPEED + 1.0f) * 1000);
		}
		
		float attackSpeed = (float) attacker.getAttributeValue(Attributes.ATTACK_SPEED);
		if (attackSpeed <= 0) attackSpeed = DEFAULT_ATTACK_SPEED;
		
		// Cooldown = (1 / attackSpeed) + 1 second
		return (long) ((1.0f / attackSpeed + 1.0f) * 1000);
	}

	/**
	 * Generate a unique key for cooldown tracking.
	 */
	public static String getCooldownKey(LivingEntity attacker, LivingEntity target) {
		return attacker.getId() + "_" + target.getId();
	}

	/**
	 * Check if cooldown has expired for a (attacker, target) pair.
	 * @return true if enough time has passed to reset stages
	 */
	public static boolean isCooldownExpired(LivingEntity attacker, LivingEntity target) {
		String key = getCooldownKey(attacker, target);
		Long lastAttackTime = STAGE_COOLDOWN_TRACKER.get(key);
		
		if (lastAttackTime == null) {
			return true; // No previous attack, cooldown expired
		}
		
		long elapsed = System.currentTimeMillis() - lastAttackTime;
		long cooldown = getAttackCooldown(attacker);
		
		return elapsed >= cooldown;
	}

	/**
	 * Reset stages for a (attacker, target) pair and update cooldown.
	 */
	public static void resetStagesAndCooldown(LivingEntity attacker, LivingEntity target) {
		String key = getCooldownKey(attacker, target);
		STAGE_COOLDOWN_TRACKER.put(key, System.currentTimeMillis());
		// Note: actual stage reset is handled by ElementDamageHandler
	}

	/**
	 * Clear all cooldown tracking entries.
	 */
	public static void clearCooldownTracking() {
		STAGE_COOLDOWN_TRACKER.clear();
	}

	/**
	 * Clean up old cooldown entries for specified entity.
	 */
	public static void cleanupEntityCooldowns(LivingEntity entity) {
		STAGE_COOLDOWN_TRACKER.keySet().removeIf(key -> key.contains("_" + entity.getId() + "_"));
	}
}
