package com.auranite.abloom;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ElementResistanceManager {

	private static final Map<EntityType<?>, Map<ElementType, Resistance>> ENTITY_RESISTANCES = new ConcurrentHashMap<>();

	// Поле TAG_CHECKED_ENTITIES удалено, так как больше не используется.

	private ElementResistanceManager() {}

	public static void registerResistance(EntityType<?> entityType, Map<ElementType, Resistance> resistanceMap) {
		if (entityType == null || resistanceMap == null || resistanceMap.isEmpty()) return;

		Map<ElementType, Resistance> existing = ENTITY_RESISTANCES.computeIfAbsent(
				entityType, k -> new EnumMap<>(ElementType.class)
		);
		existing.putAll(resistanceMap);

		AbloomMod.LOGGER.debug("Registered resistance for {}: {}",
				entityType.getDescriptionId(), resistanceMap);
	}

	// Все методы, связанные с тегами данных, были удалены.
	// Регистрация сопротивлений теперь осуществляется исключительно через MobResistanceRegistry.

	public static Resistance getResistance(Entity entity, ElementType type) {
		if (entity == null || type == null) return Resistance.ZERO;
		return getResistance(entity.getType(), type);
	}

	public static Resistance getResistance(EntityType<?> entityType, ElementType type) {
		if (entityType == null || type == null) return Resistance.ZERO;

		Map<ElementType, Resistance> typeMap = ENTITY_RESISTANCES.get(entityType);
		if (typeMap == null) return Resistance.ZERO;

		Resistance res = typeMap.get(type);
		return res != null ? res : Resistance.ZERO;
	}

	public static int calculateAccumulationPoints(Entity entity, ElementType type, int basePoints) {
		Resistance resistance = getResistance(entity, type);
		float multiplier = 1f - resistance.resistance();
		// Защита от нулевого или отрицательного множителя (если resistance > 1, что невозможно по constraints)
		return Math.round(basePoints * Math.max(0.001f, multiplier));
	}

	public static float calculateReducedDamage(Entity entity, ElementType type, float baseDamage) {
		Resistance resistance = getResistance(entity, type);
		float multiplier = 1f - resistance.resistance();
		
		if (entity instanceof LivingEntity livingEntity &&
				livingEntity.hasEffect(AbloomModEffects.CORRUPTION)) {
			// CORRUPTION снижает сопротивление на 20% (making entity more vulnerable)
			float newResistance = resistance.resistance() * 0.8f;
			multiplier = 1f - newResistance;
		}
		
		return Math.max(0.001f, baseDamage * multiplier);
	}

	public static boolean isImmune(Entity entity, ElementType type) {
		return false;
	}

	public static boolean isWeakness(Entity entity, ElementType type) {
		return getResistance(entity, type).isWeakness();
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

		Map<ElementType, Resistance> typeMap = ENTITY_RESISTANCES.get(entityType);
		if (typeMap == null) return false;

		Resistance res = typeMap.get(type);
		return res != null && res != Resistance.ZERO;
	}

	public static void clearAllResistances() {
		ENTITY_RESISTANCES.clear();
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

	public record Resistance(float resistance) {
		public static final Resistance ZERO = new Resistance(0.0f);
		public static final Resistance HALF_RESIST = new Resistance(0.5f);
		public static final Resistance WEAKNESS = new Resistance(-0.5f);

		public boolean isWeakness() { return resistance < 0f; }
		public float getMultiplier() { return 1f - resistance; }
	}
}