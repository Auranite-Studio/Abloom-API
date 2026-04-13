package com.auranite.abloom;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class ElementResistanceManager {
	private static final Map<EntityType<?>, Map<ElementType, Resistance>> ENTITY_RESISTANCES = new ConcurrentHashMap<>();
	private static final Map<EntityType<?>, Boolean> TAG_CHECKED_ENTITIES = new ConcurrentHashMap<>();
	private ElementResistanceManager() {}
	public static void registerResistance(EntityType<?> entityType, Map<ElementType, Resistance> resistanceMap) {
		if (entityType == null || resistanceMap == null || resistanceMap.isEmpty()) return;
		Map<ElementType, Resistance> existing = ENTITY_RESISTANCES.computeIfAbsent(
				entityType, k -> new EnumMap<>(ElementType.class)
		);
		existing.putAll(resistanceMap);
		, resistanceMap);
	}
	public static void loadFromTag(ElementType elementType, TagKey<EntityType<?>> tag,
								   Resistance resistance, net.minecraft.core.HolderLookup.Provider lookupProvider) {
		if (elementType == null || tag == null || resistance == null || lookupProvider == null) {
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
				, tag.location());
			}
, resistance);
		}, () -> {
);
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
			registerResistance(entityType, Map.of(elementType, Resistance.IMMUNE));
", entityType.getDescriptionId(), elementType);
			return;
		}
		TagKey<EntityType<?>> resistTag = createTag(modid, elementLower, "resistance");
		if (entityType.is(resistTag)) {
			registerResistance(entityType, Map.of(elementType, Resistance.HALF_RESIST));
", entityType.getDescriptionId(), elementType);
			return;
		}
		TagKey<EntityType<?>> weaknessTag = createTag(modid, elementLower, "weakness");
		if (entityType.is(weaknessTag)) {
			registerResistance(entityType, Map.of(elementType, Resistance.WEAKNESS));
", entityType.getDescriptionId(), elementType);
			return;
		}
", entityType.getDescriptionId(), elementType);
	}
	private static TagKey<EntityType<?>> createTag(String modid, String element, String modifier) {
		return TagKey.create(Registries.ENTITY_TYPE,
				ResourceLocation.fromNamespaceAndPath(modid, "element/" + element + "/" + modifier));
	}
	public static Resistance getResistance(Entity entity, ElementType type) {
		if (entity == null || type == null) return Resistance.ZERO;
		return getResistance(entity;
	}
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
	public static int calculateAccumulationPoints(Entity entity, ElementType type, int basePoints) {
		Resistance resistance = getResistance(entity, type);
		float multiplier = 1f - resistance.accumulationResistance();
		multiplier = Math.max(0f, multiplier);
		return Math.round(basePoints * multiplier);
	}
	public static float calculateReducedDamage(Entity entity, ElementType type, float baseDamage) {
		Resistance resistance = getResistance(entity, type);
		float multiplier = 1f - resistance.damageResistance();
		multiplier = Math.max(0f, multiplier);
		return Math.max(0f, baseDamage * multiplier);
	}
	public static boolean isImmune(Entity entity, ElementType type) {
		return getResistance(entity, type).isImmune();
	}
	public static boolean isWeakness(Entity entity, ElementType type) {
		return getResistance(entity, type).isWeakness();
	}
	public static boolean hasResistanceFor(EntityType<?> entityType) {
		return entityType != null && ENTITY_RESISTANCES.containsKey(entityType);
	}
	public static boolean hasResistanceFor(Entity entity, ElementType type) {
		if (entity == null || type == null) return false;
		return hasResistanceFor(entity;
	}
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
	public static void clearAllResistances() {
		ENTITY_RESISTANCES.clear();
		TAG_CHECKED_ENTITIES.clear();
	}
	public static int getRegisteredEntityCount() {
		return ENTITY_RESISTANCES.size();
	}
	public static int getTotalResistanceEntries() {
		return ENTITY_RESISTANCES.values().stream().mapToInt(Map::size).sum();
	}
	public static void debugPrintRegistry() {
		, getTotalResistanceEntries());
		ENTITY_RESISTANCES.forEach((type, map) -> {
, map);
		});
	}
	public record Resistance(float accumulationResistance, float damageResistance) {
		public static final Resistance ZERO = new Resistance(0.0f, 0.0f);
		public static final Resistance IMMUNE = new Resistance(1.0f, 1.0f);
		public static final Resistance HALF_RESIST = new Resistance(0.5f, 0.5f);
		public static final Resistance WEAKNESS = new Resistance(-0.5f, -0.5f);
		public boolean isImmune() { return accumulationResistance >= 1.0f && damageResistance >= 1.0f; }
		public boolean isWeakness() { return accumulationResistance < 0f || damageResistance < 0f; }
		public float getAccumulationMultiplier() { return Math.max(0f, 1f - accumulationResistance); }
		public float getDamageMultiplier() { return Math.max(0f, 1f - damageResistance); }
	}
}