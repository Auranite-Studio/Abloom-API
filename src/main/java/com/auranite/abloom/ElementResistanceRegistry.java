package com.auranite.abloom;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

import java.util.EnumMap;
import java.util.Map;

public class ElementResistanceRegistry {

    private ElementResistanceRegistry() {}

    public static TagKey<EntityType<?>> createEntityTag(String element, String modifier) {
        return TagKey.create(Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(AbloomMod.MODID,
                        "element_resistance/" + element.toLowerCase() + "/" + modifier));
    }

    public static void init(net.minecraft.core.HolderLookup.Provider lookupProvider) {
        AbloomMod.LOGGER.info("Initializing Element Resistance Registry (Tag-based)...");

        try {
            for (ElementType elementType : ElementType.values()) {
                String tagName = elementType.name().toLowerCase();

                ElementResistanceManager.loadFromTag(
                        elementType,
                        createEntityTag(tagName, "resistance"),
                        ElementResistanceManager.Resistance.HALF_RESIST,
                        lookupProvider
                );

                ElementResistanceManager.loadFromTag(
                        elementType,
                        createEntityTag(tagName, "weakness"),
                        ElementResistanceManager.Resistance.WEAKNESS,
                        lookupProvider
                );
            }

            AbloomMod.LOGGER.info("Element Resistance Registry initialized! Total: {} entities",
                    ElementResistanceManager.getRegisteredEntityCount());

        } catch (Exception e) {
            AbloomMod.LOGGER.error("Failed to initialize Element Resistance Registry!", e);
        }
    }

    public static void init() {
        AbloomMod.LOGGER.info("Initializing Element Resistance Registry (Lazy tag loading)...");
    }

    @SafeVarargs
    public static void registerUniform(ElementType elementType, float resistance, EntityType<?>... entityTypes) {
        if (elementType == null || entityTypes == null) return;

        for (EntityType<?> type : entityTypes) {
            if (type == null) continue;
            ElementResistanceManager.registerResistance(type, Map.of(
                    elementType, new ElementResistanceManager.Resistance(resistance)
            ));
        }
    }

    public static void registerSingle(EntityType<?> entityType, ElementType elementType, float resistance) {
        if (entityType == null || elementType == null) return;
        ElementResistanceManager.registerResistance(entityType, Map.of(
                elementType, new ElementResistanceManager.Resistance(resistance)
        ));
    }

    public static void registerSingleUniform(EntityType<?> entityType, ElementType elementType, float resistance) {
        registerSingle(entityType, elementType, resistance);
    }

    public static void registerMultiple(EntityType<?> entityType,
                                        Map<ElementType, ElementResistanceManager.Resistance> resistanceMap) {
        if (entityType == null || resistanceMap == null || resistanceMap.isEmpty()) return;
        ElementResistanceManager.registerResistance(entityType, new EnumMap<>(resistanceMap));
    }

    public static boolean hasResistances(EntityType<?> entityType) {
        return ElementResistanceManager.hasResistanceFor(entityType);
    }

    public static boolean hasResistances(Entity entity) {
        if (entity == null) return false;
        return ElementResistanceManager.hasResistanceFor(entity.getType());
    }

    public static boolean hasResistance(EntityType<?> entityType, ElementType elementType) {
        return ElementResistanceManager.hasResistanceFor(entityType, elementType);
    }

    public static ElementResistanceManager.Resistance getResistance(EntityType<?> entityType, ElementType elementType) {
        return ElementResistanceManager.getResistance(entityType, elementType);
    }

    public static void clearAll() {
        ElementResistanceManager.clearAllResistances();
    }

    public static void debugPrint() {
        ElementResistanceManager.debugPrintRegistry();
    }

    public static final class Tags {
        private Tags() {}

        public static final TagKey<EntityType<?>> FIRE_RESISTANCE = createEntityTag("fire", "resistance");
        public static final TagKey<EntityType<?>> FIRE_WEAKNESS = createEntityTag("fire", "weakness");

        public static final TagKey<EntityType<?>> WATER_RESISTANCE = createEntityTag("water", "resistance");
        public static final TagKey<EntityType<?>> WATER_WEAKNESS = createEntityTag("water", "weakness");

        public static final TagKey<EntityType<?>> EARTH_RESISTANCE = createEntityTag("earth", "resistance");
        public static final TagKey<EntityType<?>> EARTH_WEAKNESS = createEntityTag("earth", "weakness");

        public static final TagKey<EntityType<?>> WIND_RESISTANCE = createEntityTag("wind", "resistance");
        public static final TagKey<EntityType<?>> WIND_WEAKNESS = createEntityTag("wind", "weakness");

        public static final TagKey<EntityType<?>> ICE_RESISTANCE = createEntityTag("ice", "resistance");
        public static final TagKey<EntityType<?>> ICE_WEAKNESS = createEntityTag("ice", "weakness");

        public static final TagKey<EntityType<?>> ELECTRIC_RESISTANCE = createEntityTag("electric", "resistance");
        public static final TagKey<EntityType<?>> ELECTRIC_WEAKNESS = createEntityTag("electric", "weakness");

        public static final TagKey<EntityType<?>> PHYSICAL_RESISTANCE = createEntityTag("physical", "resistance");
        public static final TagKey<EntityType<?>> PHYSICAL_WEAKNESS = createEntityTag("physical", "weakness");

        public static final TagKey<EntityType<?>> SOURCE_RESISTANCE = createEntityTag("energy", "resistance");
        public static final TagKey<EntityType<?>> SOURCE_WEAKNESS = createEntityTag("energy", "weakness");

        public static final TagKey<EntityType<?>> NATURAL_RESISTANCE = createEntityTag("natural", "resistance");
        public static final TagKey<EntityType<?>> NATURAL_WEAKNESS = createEntityTag("natural", "weakness");

        public static final TagKey<EntityType<?>> QUANTUM_RESISTANCE = createEntityTag("quantum", "resistance");
        public static final TagKey<EntityType<?>> QUANTUM_WEAKNESS = createEntityTag("quantum", "weakness");

        public static final TagKey<EntityType<?>> ETHER_RESISTANCE = createEntityTag("ether", "resistance");
        public static final TagKey<EntityType<?>> ETHER_WEAKNESS = createEntityTag("ether", "weakness");

        public static final TagKey<EntityType<?>> LIGHT_RESISTANCE = createEntityTag("light", "resistance");
        public static final TagKey<EntityType<?>> LIGHT_WEAKNESS = createEntityTag("light", "weakness");

        public static final TagKey<EntityType<?>> SHADOW_RESISTANCE = createEntityTag("shadow", "resistance");
        public static final TagKey<EntityType<?>> SHADOW_WEAKNESS = createEntityTag("shadow", "weakness");
    }
}
