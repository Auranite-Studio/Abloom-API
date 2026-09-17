package com.auranite.abloom.network;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.util.ElementType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ClientResonanceAccumulationStorage {
    private static final Map<Integer, Map<ElementType, Integer>> entityAccumulation = new HashMap<>();

    public static void updateEntityAccumulation(int entityId, Map<ElementType, Integer> newPoints) {
        AbloomMod.LOGGER.info("Client storing accumulation for entity {}: {}", entityId, newPoints);
        entityAccumulation.put(entityId, new EnumMap<>(newPoints));
    }

    public static Map<ElementType, Integer> getEntityAccumulation(int entityId) {
        return entityAccumulation.getOrDefault(entityId, new EnumMap<>(ElementType.class));
    }

    public static void removeEntityAccumulation(int entityId) {
        entityAccumulation.remove(entityId);
    }

    public static boolean hasEntityAccumulation(int entityId) {
        Map<ElementType, Integer> map = entityAccumulation.get(entityId);
        return map != null && !map.isEmpty();
    }

    public static void clearAll() {
        entityAccumulation.clear();
    }
}
