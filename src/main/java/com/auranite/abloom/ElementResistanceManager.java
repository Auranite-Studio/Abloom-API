package com.auranite.abloom;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ElementResistanceManager {

	private static final Map<EntityType<?>, Map<String, Resistance>> ENTITY_RESISTANCES = new ConcurrentHashMap<>();

	private static final Map<EntityType<?>, Boolean> TAG_CHECKED_ENTITIES = new ConcurrentHashMap<>();

	private ElementResistanceManager() {}

	/**
	 * Register resistance for an entity type against a specific element type.
	 * @param entityType the entity type
	 * @param elementType the built-in element type
	 * @param resistance the resistance value
	 */
	public static void registerResistance(EntityType<?> entityType, ElementType elementType, Resistance resistance) {
		if (entityType == null || elementType == null || resistance == null) return;
		registerResistance(entityType, elementType.getDamageTypeId(), resistance);
	}

	/**
	 * Register resistance for an entity type against a damage type ID (built-in or custom).
	 * @param entityType the entity type
	 * @param damageTypeId the damage type ID (e.g., "wind_dmg" or "abloom:custom_dmg")
	 * @param resistance the resistance value
	 */
	public static void registerResistance(EntityType<?> entityType, String damageTypeId, Resistance resistance) {
		if (entityType == null || damageTypeId == null || resistance == null) return;

		Map<String, Resistance> existing = ENTITY_RESISTANCES.computeIfAbsent(
				entityType, k -> new ConcurrentHashMap<>()
		);
		existing.put(damageTypeId, resistance);

		AbloomMod.LOGGER.debug("Registered resistance for {}: {} = {}",
				entityType.getDescriptionId(), damageTypeId, resistance);
	}

	/**
	 * Register multiple resistances for an entity type using ElementType keys.
	 * @param entityType the entity type
	 * @param resistanceMap map of ElementType to Resistance
	 */
	public static void registerResistance(EntityType<?> entityType, Map<ElementType, Resistance> resistanceMap) {
		if (entityType == null || resistanceMap == null || resistanceMap.isEmpty()) return;

		Map<String, Resistance> existing = ENTITY_RESISTANCES.computeIfAbsent(
				entityType, k -> new ConcurrentHashMap<>()
		);
		for (Map.Entry<ElementType, Resistance> entry : resistanceMap.entrySet()) {
			if (entry.getKey() != null && entry.getValue() != null) {
				existing.put(entry.getKey().getDamageTypeId(), entry.getValue());
			}
		}

		AbloomMod.LOGGER.debug("Registered resistances for {}: {}",
				entityType.getDescriptionId(), resistanceMap);
	}

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

				Map<String, Resistance> resistanceMap = ENTITY_RESISTANCES
						.computeIfAbsent(entityType, k -> new ConcurrentHashMap<>());
				resistanceMap.put(elementType.getDamageTypeId(), resistance);
				count++;

				AbloomMod.LOGGER.debug("  └─ Loaded {} for {} from tag {}",
						resistance, entityType.getDescriptionId(), tag.location());
			}
			AbloomMod.LOGGER.info("Loaded {} entities from tag {} → {}", count, tag.location(), resistance);
		}, () -> {
			AbloomMod.LOGGER.warn("Tag {} not found! Check your datapack.", tag.location());
		});
	}

	/**
	 * Load resistance from a tag for a custom damage type ID.
	 * @param damageTypeId the custom damage type ID
	 * @param tag the entity type tag
	 * @param resistance the resistance value
	 * @param lookupProvider the registry lookup provider
	 */
	public static void loadFromTag(String damageTypeId, TagKey<EntityType<?>> tag,
								   Resistance resistance, net.minecraft.core.HolderLookup.Provider lookupProvider) {
		if (damageTypeId == null || tag == null || resistance == null || lookupProvider == null) {
			AbloomMod.LOGGER.warn("loadFromTag called with null params: damageTypeId={}, tag={}, resistance={}, lookup={}",
					damageTypeId, tag, resistance, lookupProvider != null);
			return;
		}

		var entityLookup = lookupProvider.lookupOrThrow(Registries.ENTITY_TYPE);

		entityLookup.get(tag).ifPresentOrElse(tagged -> {
			int count = 0;
			for (var holder : tagged) {
				EntityType<?> entityType = holder.value();
				if (entityType == null) continue;

				Map<String, Resistance> resistanceMap = ENTITY_RESISTANCES
						.computeIfAbsent(entityType, k -> new ConcurrentHashMap<>());
				resistanceMap.put(damageTypeId, resistance);
				count++;

				AbloomMod.LOGGER.debug("  └─ Loaded {} for {} from tag {} (custom damage type)",
						resistance, entityType.getDescriptionId(), tag.location());
			}
			AbloomMod.LOGGER.info("Loaded {} entities from tag {} → {} (custom damage type)", count, tag.location(), resistance);
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

		TagKey<EntityType<?>> immuneTag = createTag(modid, elementLower, "immune");
		if (entityType.is(immuneTag)) {
			registerResistance(entityType, elementType, Resistance.IMMUNE);
			AbloomMod.LOGGER.debug("Lazy-loaded IMMUNE for {} ({})", entityType.getDescriptionId(), elementType);
			return;
		}

		TagKey<EntityType<?>> resistTag = createTag(modid, elementLower, "resistance");
		if (entityType.is(resistTag)) {
			registerResistance(entityType, elementType, Resistance.HALF_RESIST);
			AbloomMod.LOGGER.debug("Lazy-loaded RESIST for {} ({})", entityType.getDescriptionId(), elementType);
			return;
		}

		TagKey<EntityType<?>> weaknessTag = createTag(modid, elementLower, "weakness");
		if (entityType.is(weaknessTag)) {
			registerResistance(entityType, elementType, Resistance.WEAKNESS);
			AbloomMod.LOGGER.debug("Lazy-loaded WEAKNESS for {} ({})", entityType.getDescriptionId(), elementType);
			return;
		}

		AbloomMod.LOGGER.debug("No tag found for {} ({})", entityType.getDescriptionId(), elementType);
	}

	/**
	 * Try lazy loading resistance from tags for a custom damage type ID.
	 * @param entityType the entity type
	 * @param damageTypeId the custom damage type ID
	 */
	private static void tryLazyLoadFromTags(EntityType<?> entityType, String damageTypeId) {
		if (entityType == null || damageTypeId == null) return;

		// For custom damage types, we don't have built-in tag support
		// Custom types should be registered via data packs or programmatically
		AbloomMod.LOGGER.debug("No built-in tag support for custom damage type: {}", damageTypeId);
	}

	private static TagKey<EntityType<?>> createTag(String modid, String element, String modifier) {
		return TagKey.create(Registries.ENTITY_TYPE,
				ResourceLocation.fromNamespaceAndPath(modid, "element/" + element + "/" + modifier));
	}

	/**
	 * Get resistance for an entity against a built-in element type.
	 * @param entity the entity
	 * @param type the element type
	 * @return the resistance value
	 */
	public static Resistance getResistance(Entity entity, ElementType type) {
		if (entity == null || type == null) return Resistance.ZERO;
		return getResistance(entity.getType(), type);
	}

	/**
	 * Get resistance for an entity type against a built-in element type.
	 * @param entityType the entity type
	 * @param type the element type
	 * @return the resistance value
	 */
	public static Resistance getResistance(EntityType<?> entityType, ElementType type) {
		if (entityType == null || type == null) return Resistance.ZERO;
		return getResistance(entityType, type.getDamageTypeId());
	}

	/**
	 * Get resistance for an entity against a damage type ID (built-in or custom).
	 * @param entity the entity
	 * @param damageTypeId the damage type ID
	 * @return the resistance value
	 */
	public static Resistance getResistance(Entity entity, String damageTypeId) {
		if (entity == null || damageTypeId == null) return Resistance.ZERO;
		return getResistance(entity.getType(), damageTypeId);
	}

	/**
	 * Get resistance for an entity type against a damage type ID (built-in or custom).
	 * @param entityType the entity type
	 * @param damageTypeId the damage type ID
	 * @return the resistance value
	 */
	public static Resistance getResistance(EntityType<?> entityType, String damageTypeId) {
		if (entityType == null || damageTypeId == null) return Resistance.ZERO;

		Map<String, Resistance> typeMap = ENTITY_RESISTANCES.get(entityType);

		if (typeMap == null || !typeMap.containsKey(damageTypeId)) {
			// Try lazy loading for built-in types
			ElementType elementType = ElementType.fromDamageTypeId(damageTypeId).orElse(null);
			if (elementType != null) {
				tryLazyLoadFromTags(entityType, elementType);
			} else {
				tryLazyLoadFromTags(entityType, damageTypeId);
			}
			typeMap = ENTITY_RESISTANCES.get(entityType);
		}

		if (typeMap == null) return Resistance.ZERO;

		Resistance res = typeMap.get(damageTypeId);
		return res != null ? res : Resistance.ZERO;
	}

	/**
	 * Calculate accumulation points for an entity against a built-in element type.
	 */
	public static int calculateAccumulationPoints(Entity entity, ElementType type, int basePoints) {
		Resistance resistance = getResistance(entity, type);
		float multiplier = 1f - resistance.accumulationResistance();
		multiplier = Math.max(0f, multiplier);
		return Math.round(basePoints * multiplier);
	}

	/**
	 * Calculate accumulation points for an entity against a damage type ID.
	 */
	public static int calculateAccumulationPoints(Entity entity, String damageTypeId, int basePoints) {
		Resistance resistance = getResistance(entity, damageTypeId);
		float multiplier = 1f - resistance.accumulationResistance();
		multiplier = Math.max(0f, multiplier);
		return Math.round(basePoints * multiplier);
	}

	/**
	 * Calculate reduced damage for an entity against a built-in element type.
	 */
	public static float calculateReducedDamage(Entity entity, ElementType type, float baseDamage) {
		Resistance resistance = getResistance(entity, type);
		return applyResistanceMultiplier(entity, resistance, baseDamage);
	}

	/**
	 * Calculate reduced damage for an entity against a damage type ID.
	 */
	public static float calculateReducedDamage(Entity entity, String damageTypeId, float baseDamage) {
		Resistance resistance = getResistance(entity, damageTypeId);
		return applyResistanceMultiplier(entity, resistance, baseDamage);
	}

	private static float applyResistanceMultiplier(Entity entity, Resistance resistance, float baseDamage) {
		float multiplier = 1f - resistance.damageResistance();
		multiplier = Math.max(0f, multiplier);

		if (entity instanceof LivingEntity livingEntity &&
				livingEntity.hasEffect(AbloomModEffects.CORRUPTION)) {
			float resistanceReduction = resistance.damageResistance() * 0.2f;
			multiplier = Math.min(1.0f, multiplier + resistanceReduction);
		}
		
		return Math.max(0f, baseDamage * multiplier);
	}

	/**
	 * Check if an entity is immune to a built-in element type.
	 */
	public static boolean isImmune(Entity entity, ElementType type) {
		return getResistance(entity, type).isImmune();
	}

	/**
	 * Check if an entity is immune to a damage type ID.
	 */
	public static boolean isImmune(Entity entity, String damageTypeId) {
		return getResistance(entity, damageTypeId).isImmune();
	}

	/**
	 * Check if an entity has weakness to a built-in element type.
	 */
	public static boolean isWeakness(Entity entity, ElementType type) {
		return getResistance(entity, type).isWeakness();
	}

	/**
	 * Check if an entity has weakness to a damage type ID.
	 */
	public static boolean isWeakness(Entity entity, String damageTypeId) {
		return getResistance(entity, damageTypeId).isWeakness();
	}

	public static boolean hasResistanceFor(EntityType<?> entityType) {
		return entityType != null && ENTITY_RESISTANCES.containsKey(entityType);
	}

	public static boolean hasResistanceFor(Entity entity, ElementType type) {
		if (entity == null || type == null) return false;
		return hasResistanceFor(entity.getType(), type);
	}

	public static boolean hasResistanceFor(EntityType<?> entityType, ElementType type) {
		if (entityType == null || type == null) return false;
		return hasResistanceFor(entityType, type.getDamageTypeId());
	}

	/**
	 * Check if an entity type has resistance for a damage type ID.
	 */
	public static boolean hasResistanceFor(EntityType<?> entityType, String damageTypeId) {
		if (entityType == null || damageTypeId == null) return false;

		Map<String, Resistance> typeMap = ENTITY_RESISTANCES.get(entityType);
		if (typeMap != null && typeMap.containsKey(damageTypeId)) {
			Resistance res = typeMap.get(damageTypeId);
			return res != null && res != Resistance.ZERO;
		}

		// Try lazy loading for built-in types
		ElementType elementType = ElementType.fromDamageTypeId(damageTypeId).orElse(null);
		if (elementType != null) {
			tryLazyLoadFromTags(entityType, elementType);
		} else {
			tryLazyLoadFromTags(entityType, damageTypeId);
		}
		typeMap = ENTITY_RESISTANCES.get(entityType);

		if (typeMap == null) return false;
		Resistance res = typeMap.get(damageTypeId);
		return res != null && res != Resistance.ZERO;
	}

	/**
	 * Check if an entity has resistance for a damage type ID.
	 */
	public static boolean hasResistanceFor(Entity entity, String damageTypeId) {
		if (entity == null || damageTypeId == null) return false;
		return hasResistanceFor(entity.getType(), damageTypeId);
	}

	public static void clearAllResistances() {
		ENTITY_RESISTANCES.clear();
		TAG_CHECKED_ENTITIES.clear();
		AbloomMod.LOGGER.info("Cleared all element resistances");
	}

	public static int getRegisteredEntityCount() {
		return ENTITY_RESISTANCES.size();
	}

	public static int getTotalResistanceEntries() {
		return ENTITY_RESISTANCES.values().stream().mapToInt(Map::size).sum();
	}

	public static void debugPrintRegistry() {
		AbloomMod.LOGGER.info("=== RESISTANCE REGISTRY ===");
		AbloomMod.LOGGER.info("Entities: {}, Entries: {}",
				getRegisteredEntityCount(), getTotalResistanceEntries());

		ENTITY_RESISTANCES.forEach((type, map) -> {
			AbloomMod.LOGGER.info("  {} → {}", type.getDescriptionId(), map);
		});
	}

	public record Resistance(float accumulationResistance, float damageResistance) {
		public static final Resistance ZERO = new Resistance(0.0f, 0.0f);
		public static final Resistance IMMUNE = new Resistance(1.0f, 0.99f);
		public static final Resistance HALF_RESIST = new Resistance(0.5f, 0.5f);
		public static final Resistance WEAKNESS = new Resistance(-0.5f, -0.5f);

		public boolean isImmune() { return accumulationResistance >= 1.0f && damageResistance >= 1.0f; }
		public boolean isWeakness() { return accumulationResistance < 0f || damageResistance < 0f; }
		public float getAccumulationMultiplier() { return Math.max(0f, 1f - accumulationResistance); }
		public float getDamageMultiplier() { return Math.max(0f, 1f - damageResistance); }
	}
}