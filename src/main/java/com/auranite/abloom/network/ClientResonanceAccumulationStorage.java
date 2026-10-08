package com.auranite.abloom.network;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.util.IElementalType;

import java.util.HashMap;
import java.util.Map;

public class ClientResonanceAccumulationStorage {
    private static final Map<Integer, Map<IElementalType, Integer>> entityAccumulation = new HashMap<>();

    public static void updateEntityAccumulation(int entityId, Map<IElementalType, Integer> newPoints) {
        AbloomMod.LOGGER.info("Client storing accumulation for entity {}: {}", entityId, newPoints);
        entityAccumulation.put(entityId, new HashMap<>(newPoints));
    }

    public static Map<IElementalType, Integer> getEntityAccumulation(int entityId) {
        return entityAccumulation.getOrDefault(entityId, new HashMap<>());
    }

    public static void removeEntityAccumulation(int entityId) {
        entityAccumulation.remove(entityId);
    }

    public static boolean hasEntityAccumulation(int entityId) {
        Map<IElementalType, Integer> map = entityAccumulation.get(entityId);
        return map != null && !map.isEmpty();
    }

    public static void clearAll() {
        entityAccumulation.clear();
    }
}
