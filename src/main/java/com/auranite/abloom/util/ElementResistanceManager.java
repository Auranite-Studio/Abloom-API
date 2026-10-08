package com.auranite.abloom.util;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.init.AbloomModEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages elemental resistance calculations for entities.
 * Handles both datapack-based resistance registration and tag-based lazy loading.
 * Supports the following resistance levels:
 * <ul>
 *   <li>ZERO (0.0) - No resistance, full damage taken</li>
 *   <li>HALF_RESIST (0.5) - 50% damage reduction</li>
 *   <li>WEAKNESS (-0.5) - 50% damage increase</li>
 * </ul>
 */
public class ElementResistanceManager {

	private static final Map<EntityType<?>, Map<IElementalType, Resistance>> ENTITY_RESISTANCES = new ConcurrentHashMap<>();
	private static final Map<EntityType<?>, Boolean> TAG_CHECKED_ENTITIES = new ConcurrentHashMap<>();

	private ElementResistanceManager() {}

	/**
	 * Registers elemental resistances for an entity type.
	 * Existing resistances are preserved and merged with new values.
	 * @param entityType the entity type
	 * @param resistanceMap map of element types to resistance values
	 */
	public static void registerResistance(EntityType<?> entityType, Map<IElementalType, Resistance> resistanceMap) {
		if (entityType == null || resistanceMap == null || resistanceMap.isEmpty()) return;

		Map<IElementalType, Resistance> existing = ENTITY_RESISTANCES.computeIfAbsent(
				entityType, k -> new HashMap<>()
		);
		existing.putAll(resistanceMap);
	}

	/**
	 * Loads resistances from a datapack tag.
	 * @param elementType the elemental type
	 * @param tag the entity type tag
	 * @param resistance the resistance value to apply
	 * @param lookupProvider the lookup provider for loading tags
	 */
	public static void loadFromTag(IElementalType elementType, TagKey<EntityType<?>> tag,
			Resistance resistance, net.minecraft.core.HolderLookup.Provider lookupProvider) {
		if (elementType == null || tag == null || resistance == null || lookupProvider == null) {
			AbloomMod.LOGGER.warn("loadFromTag called with null params: element={}, tag={}, resistance={}, lookup={}",
					elementType, tag, resistance, lookupProvider != null);
			return;
		}

		var entityLookup = lookupProvider.lookupOrThrow(Registries.ENTITY_TYPE);

		entityLookup.get(tag).ifPresentOrElse(tagged -> {
			int count = 0;
			for (var holder : tagged) {
				EntityType<?> entityType = holder.value();
				if (entityType == null) continue;

				Map<IElementalType, Resistance> resistanceMap = ENTITY_RESISTANCES
						.computeIfAbsent(entityType, k -> new HashMap<>());
				resistanceMap.put(elementType, resistance);
				count++;
			}
			AbloomMod.LOGGER.info("Loaded {} entities from tag {} → {}", count, tag.location(), resistance);
		}, () -> {
			AbloomMod.LOGGER.warn("Tag {} not found! Check your datapack.", tag.location());
		});
	}

	private static void tryLazyLoadFromTags(EntityType<?> entityType, IElementalType elementType) {
		if (entityType == null || elementType == null) return;

		if (TAG_CHECKED_ENTITIES.getOrDefault(entityType, false)) {
			return;
		}
		TAG_CHECKED_ENTITIES.put(entityType, true);

		String elementLower = elementType.name().toLowerCase();
		String modid = AbloomMod.MODID;

		TagKey<EntityType<?>> resistTag = createTag(modid, elementLower, "resistance");
		if (entityType.is(resistTag)) {
			registerResistance(entityType, Map.of(elementType, Resistance.HALF_RESIST));
			return;
		}

		TagKey<EntityType<?>> weaknessTag = createTag(modid, elementLower, "weakness");
		if (entityType.is(weaknessTag)) {
			registerResistance(entityType, Map.of(elementType, Resistance.WEAKNESS));
			return;
		}
	}

	private static TagKey<EntityType<?>> createTag(String modid, String element, String modifier) {
		return TagKey.create(Registries.ENTITY_TYPE,
				ResourceLocation.fromNamespaceAndPath(modid, "element_resistance/" + element + "/" + modifier));
	}

	/**
	 * Gets the resistance value for an entity and element type.
	 * @param entity the entity
	 * @param type the element type
	 * @return the resistance value
	 */
	public static Resistance getResistance(Entity entity, IElementalType type) {
		if (entity == null || type == null) return Resistance.ZERO;
		return getResistance(entity.getType(), type);
	}

	/**
	 * Gets the resistance value for an entity type and element type.
	 * @param entityType the entity type
	 * @param type the element type
	 * @return the resistance value
	 */
	public static Resistance getResistance(EntityType<?> entityType, IElementalType type) {
		if (entityType == null || type == null) return Resistance.ZERO;

		Map<IElementalType, Resistance> typeMap = ENTITY_RESISTANCES.get(entityType);

		if (typeMap == null || !typeMap.containsKey(type)) {
			tryLazyLoadFromTags(entityType, type);
			typeMap = ENTITY_RESISTANCES.get(entityType);
		}

		if (typeMap == null) return Resistance.ZERO;

		Resistance res = typeMap.get(type);
		return res != null ? res : Resistance.ZERO;
	}

	/**
	 * Calculates accumulation points with resistance applied.
	 * @param entity the target entity
	 * @param type the element type
	 * @param basePoints the base accumulation points
	 * @return the accumulated points after resistance
	 */
	public static int calculateAccumulationPoints(Entity entity, IElementalType type, int basePoints) {
		if (entity == null || type == null) return basePoints;
		return calculateAccumulationPoints(entity.getType(), type, basePoints);
	}

	/**
	 * Calculates accumulation points with resistance applied.
	 * @param entityType the entity type
	 * @param type the element type
	 * @param basePoints the base accumulation points
	 * @return the accumulated points after resistance
	 */
	public static int calculateAccumulationPoints(EntityType<?> entityType, IElementalType type, int basePoints) {
		if (entityType == null || type == null) return basePoints;
		Resistance resistance = getResistance(entityType, type);
		return resistance.applyToAccumulation(basePoints);
	}

	/**
	 * Calculates reduced damage based on entity resistance.
	 * @param entity the target entity
	 * @param type the element type
	 * @param baseDamage the base damage
	 * @return the damage after resistance reduction
	 */
	public static float calculateReducedDamage(Entity entity, IElementalType type, float baseDamage) {
		if (entity == null || type == null) return baseDamage;
		return calculateReducedDamage(entity.getType(), type, baseDamage);
	}

	/**
	 * Calculates reduced damage based on entity resistance.
	 * @param entityType the entity type
	 * @param type the element type
	 * @param baseDamage the base damage
	 * @return the damage after resistance reduction
	 */
	public static float calculateReducedDamage(EntityType<?> entityType, IElementalType type, float baseDamage) {
		if (entityType == null || type == null) return baseDamage;
		Resistance resistance = getResistance(entityType, type);
		return resistance.applyToDamage(baseDamage);
	}

	/**
	 * Checks if an entity is immune to a specific element type.
	 * @param entity the entity
	 * @param type the element type
	 * @return true if immune
	 */
	public static boolean isImmune(Entity entity, IElementalType type) {
		if (entity == null || type == null) return false;
		return isImmune(entity.getType(), type);
	}

	/**
	 * Checks if an entity type is immune to a specific element type.
	 * @param entityType the entity type
	 * @param type the element type
	 * @return true if immune
	 */
	public static boolean isImmune(EntityType<?> entityType, IElementalType type) {
		if (entityType == null || type == null) return false;
		Resistance resistance = getResistance(entityType, type);
		return resistance == Resistance.IMMUNE;
	}

	/**
	 * Checks if an entity has resistance for a specific element type.
	 * @param entity the entity
	 * @param type the element type
	 * @return true if resistance is set
	 */
	public static boolean hasResistanceFor(Entity entity, IElementalType type) {
		if (entity == null || type == null) return false;
		return hasResistanceFor(entity.getType(), type);
	}

	/**
	 * Checks if an entity type has resistance for a specific element type.
	 * @param entityType the entity type
	 * @param type the element type
	 * @return true if resistance is set
	 */
	public static boolean hasResistanceFor(EntityType<?> entityType, IElementalType type) {
		if (entityType == null || type == null) return false;
		Map<IElementalType, Resistance> typeMap = ENTITY_RESISTANCES.get(entityType);
		if (typeMap == null) return false;
		return typeMap.containsKey(type);
	}

	/**
	 * Checks if an entity type has any resistance registered.
	 * @param entityType the entity type
	 * @return true if any resistance is registered
	 */
	public static boolean hasResistanceFor(EntityType<?> entityType) {
		if (entityType == null) return false;
		Map<IElementalType, Resistance> typeMap = ENTITY_RESISTANCES.get(entityType);
		return typeMap != null && !typeMap.isEmpty();
	}

	/**
	 * Checks if an entity has any resistance registered.
	 * @param entity the entity
	 * @return true if any resistance is registered
	 */
	public static boolean hasResistanceFor(Entity entity) {
		if (entity == null) return false;
		return hasResistanceFor(entity.getType());
	}

	/**
	 * Checks if an entity is weak to a specific element type.
	 * @param entity the entity
	 * @param type the element type
	 * @return true if weak
	 */
	public static boolean isWeakness(Entity entity, IElementalType type) {
		if (entity == null || type == null) return false;
		return isWeakness(entity.getType(), type);
	}

	/**
	 * Checks if an entity type is weak to a specific element type.
	 * @param entityType the entity type
	 * @param type the element type
	 * @return true if weak
	 */
	public static boolean isWeakness(EntityType<?> entityType, IElementalType type) {
		if (entityType == null || type == null) return false;
		Resistance resistance = getResistance(entityType, type);
		return resistance == Resistance.WEAKNESS;
	}

	/**
	 * Clears all registered resistances.
	 */
	public static void clearAllResistances() {
		ENTITY_RESISTANCES.clear();
		TAG_CHECKED_ENTITIES.clear();
	}

	/**
	 * Returns the number of registered entity types with resistances.
	 */
	public static int getRegisteredEntityCount() {
		return ENTITY_RESISTANCES.size();
	}

	/**
	 * Debug print of all registered resistances.
	 */
	public static void debugPrintRegistry() {
		AbloomMod.LOGGER.info("=== Element Resistance Registry ===");
		AbloomMod.LOGGER.info("Registered entities: {}", ENTITY_RESISTANCES.size());
		for (Map.Entry<EntityType<?>, Map<IElementalType, Resistance>> entry : ENTITY_RESISTANCES.entrySet()) {
			AbloomMod.LOGGER.info("  {} -> {}", entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Represents the level of elemental resistance.
	 */
	public enum Resistance {
		/** Full damage, no resistance */
		ZERO(0.0f),
		/** 50% damage reduction */
		HALF_RESIST(0.5f),
		/** 50% damage increase (weakness) */
		WEAKNESS(-0.5f),
		/** Complete immunity */
		IMMUNE(1.0f);

		private final float value;

		Resistance(float value) {
			this.value = value;
		}

		public float getValue() {
			return value;
		}

		/**
		 * Applies resistance to damage.
		 * @param baseDamage the base damage
		 * @return the damage after resistance
		 */
		public float applyToDamage(float baseDamage) {
			return baseDamage * (1.0f - value);
		}

		/**
		 * Applies resistance to accumulation points.
		 * @param basePoints the base points
		 * @return the points after resistance
		 */
		public int applyToAccumulation(int basePoints) {
			return (int) (basePoints * (1.0f - value));
		}
	}
}
