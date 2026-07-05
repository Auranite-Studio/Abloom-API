package com.auranite.abloom;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;
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

	private static final Map<EntityType<?>, Map<ElementType, Resistance>> ENTITY_RESISTANCES = new ConcurrentHashMap<>();
	private static final Map<EntityType<?>, Boolean> TAG_CHECKED_ENTITIES = new ConcurrentHashMap<>();

	private ElementResistanceManager() {}

	/**
	 * Registers elemental resistances for an entity type.
	 * Existing resistances are preserved and merged with new values.
	 * @param entityType the entity type
	 * @param resistanceMap map of element types to resistance values
	 */
	public static void registerResistance(EntityType<?> entityType, Map<ElementType, Resistance> resistanceMap) {
		if (entityType == null || resistanceMap == null || resistanceMap.isEmpty()) return;

		Map<ElementType, Resistance> existing = ENTITY_RESISTANCES.computeIfAbsent(
				entityType, k -> new EnumMap<>(ElementType.class)
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
	public static void loadFromTag(ElementType elementType, TagKey<EntityType<?>> tag,
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

				Map<ElementType, Resistance> resistanceMap = ENTITY_RESISTANCES
						.computeIfAbsent(entityType, k -> new EnumMap<>(ElementType.class));
				resistanceMap.put(elementType, resistance);
				count++;
			}
			AbloomMod.LOGGER.info("Loaded {} entities from tag {} → {}", count, tag.location(), resistance);
		}, () -> {
			AbloomMod.LOGGER.warn("Tag {} not found! Check your datapack.", tag.location());
		});
	}

	private static void tryLazyLoadFromTags(EntityType<?> entityType, ElementType elementType) {
		if (entityType == null || elementType == null) return;

		if (TAG_CHECKED_ENTITIES.getOrDefault(entityType, false)) {
			return;
		}
		TAG_CHECKED_ENTITIES.put(entityType, true);

		String elementLower = elementType.name().toLowerCase();
		String modid = AbloomMod.MODID;

		TagKey<EntityType<?>> resistTag = createTag(modid, elementLower, "resistance");
		if (entityType.getTags().equals(resistTag)) {
			registerResistance(entityType, Map.of(elementType, Resistance.HALF_RESIST));
			return;
		}

		TagKey<EntityType<?>> weaknessTag = createTag(modid, elementLower, "weakness");
		if (entityType.getTags().equals(weaknessTag)) {
			registerResistance(entityType, Map.of(elementType, Resistance.WEAKNESS));
			return;
		}
	}

	private static TagKey<EntityType<?>> createTag(String modid, String element, String modifier) {
		return TagKey.create(Registries.ENTITY_TYPE,
				Identifier.fromNamespaceAndPath(modid, "element_resistance/" + element + "/" + modifier));
	}

	/**
	 * Gets the resistance value for an entity and element type.
	 * @param entity the entity
	 * @param type the element type
	 * @return the resistance value
	 */
	public static Resistance getResistance(Entity entity, ElementType type) {
		if (entity == null || type == null) return Resistance.ZERO;
		return getResistance(entity.getType(), type);
	}

	/**
	 * Gets the resistance value for an entity type and element type.
	 * @param entityType the entity type
	 * @param type the element type
	 * @return the resistance value
	 */
	public static Resistance getResistance(EntityType<?> entityType, ElementType type) {
		if (entityType == null || type == null) return Resistance.ZERO;

		Map<ElementType, Resistance> typeMap = ENTITY_RESISTANCES.get(entityType);

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
	public static int calculateAccumulationPoints(Entity entity, ElementType type, int basePoints) {
		Resistance resistance = getResistance(entity, type);
		float multiplier = 1f - resistance.resistance();
		return Math.round(basePoints * Math.max(0.001f, multiplier));
	}

	/**
	 * Calculates reduced damage with resistance applied.
	 * Also applies additional reduction for corruption effect.
	 * @param entity the target entity
	 * @param type the element type
	 * @param baseDamage the base damage
	 * @return the reduced damage
	 */
	public static float calculateReducedDamage(Entity entity, ElementType type, float baseDamage) {
		Resistance resistance = getResistance(entity, type);
		float multiplier = 1f - resistance.resistance();
		
		if (entity instanceof LivingEntity livingEntity &&
				livingEntity.hasEffect(AbloomModEffects.CORRUPTION)) {
			float newResistance = resistance.resistance() * 0.8f;
			multiplier = 1f - newResistance;
		}
		
		return Math.max(0.001f, baseDamage * multiplier);
	}

	/**
	 * Checks if an entity is immune to an element type.
	 * Currently always returns false (reserved for future implementation).
	 * @param entity the entity
	 * @param type the element type
	 * @return true if immune
	 */
	public static boolean isImmune(Entity entity, ElementType type) {
		return false;
	}

	/**
	 * Checks if an entity has weakness to an element type.
	 * @param entity the entity
	 * @param type the element type
	 * @return true if has weakness
	 */
	public static boolean isWeakness(Entity entity, ElementType type) {
		return getResistance(entity, type).isWeakness();
	}

	/**
	 * Checks if an entity type has any resistance values registered.
	 * @param entityType the entity type
	 * @return true if has resistances
	 */
	public static boolean hasResistanceFor(EntityType<?> entityType) {
		return entityType != null && ENTITY_RESISTANCES.containsKey(entityType);
	}

	/**
	 * Checks if an entity has resistance to a specific element type.
	 * @param entity the entity
	 * @param type the element type
	 * @return true if has resistance
	 */
	public static boolean hasResistanceFor(Entity entity, ElementType type) {
		if (entity == null || type == null) return false;
		return hasResistanceFor(entity.getType(), type);
	}

	/**
	 * Checks if an entity type has resistance to a specific element type.
	 * @param entityType the entity type
	 * @param type the element type
	 * @return true if has resistance
	 */
	public static boolean hasResistanceFor(EntityType<?> entityType, ElementType type) {
		if (entityType == null || type == null) return false;

		Map<ElementType, Resistance> typeMap = ENTITY_RESISTANCES.get(entityType);

		if (typeMap != null && typeMap.containsKey(type)) {
			Resistance res = typeMap.get(type);
			return res != null && res != Resistance.ZERO;
		}

		tryLazyLoadFromTags(entityType, type);
		typeMap = ENTITY_RESISTANCES.get(entityType);

		if (typeMap == null) return false;

		Resistance res = typeMap.get(type);
		return res != null && res != Resistance.ZERO;
	}

	/**
	 * Clears all registered resistances.
	 */
	public static void clearAllResistances() {
		ENTITY_RESISTANCES.clear();
		TAG_CHECKED_ENTITIES.clear();
		AbloomMod.LOGGER.info("Cleared all element resistances");
	}

	/**
	 * Gets the count of registered entity types with resistances.
	 * @return number of registered entities
	 */
	public static int getRegisteredEntityCount() {
		return ENTITY_RESISTANCES.size();
	}

	/**
	 * Gets the total count of resistance entries.
	 * @return total number of resistance mappings
	 */
	public static int getTotalResistanceEntries() {
		return ENTITY_RESISTANCES.values().stream().mapToInt(Map::size).sum();
	}

	/**
	 * Prints debug information about the resistance registry.
	 */
	public static void debugPrintRegistry() {
		AbloomMod.LOGGER.info("=== RESISTANCE REGISTRY ===");
		AbloomMod.LOGGER.info("Entities: {}, Entries: {}",
				getRegisteredEntityCount(), getTotalResistanceEntries());

		ENTITY_RESISTANCES.forEach((type, map) -> {
			AbloomMod.LOGGER.info("  {} → {}", type.getDescriptionId(), map);
		});
	}

	/**
	 * Records the resistance value for an element type.
	 * Values range from -0.99 (weakness) to 0.99 (high resistance).
	 * Negative values increase damage, positive values reduce it.
	 */
	public record Resistance(float resistance) {
		public static final Resistance ZERO = new Resistance(0.0f);
		public static final Resistance HALF_RESIST = new Resistance(0.5f);
		public static final Resistance WEAKNESS = new Resistance(-0.5f);

		public boolean isWeakness() { return resistance < 0f; }
		public float getMultiplier() { return 1f - resistance; }
	}
}
