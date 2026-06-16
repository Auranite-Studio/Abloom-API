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

    public static void init() {
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


}
