package com.auranite.abloom.handler;

import com.auranite.abloom.util.ElementType;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.EnumMap;

/**
 * Tracks the last damage time for each entity and element type.
 * Used to determine when to reset accumulation points due to inactivity.
 */
public class DamageTimeTracker {

    private static final Map<Integer, Map<ElementType, Long>> LAST_DAMAGE_TIME = new ConcurrentHashMap<>();
    private static final Object LAST_DAMAGE_LOCK = new Object();

    private DamageTimeTracker() {
        // Utility class
    }

    /**
     * Updates the last damage time for an entity and element type.
     *
     * @param entity the entity that was damaged
     * @param type the element type of the damage
     * @param currentTime the current game time in ticks
     */
    public static void updateLastDamageTime(LivingEntity entity, ElementType type, long currentTime) {
        synchronized (LAST_DAMAGE_LOCK) {
            LAST_DAMAGE_TIME.computeIfAbsent(entity.getId(), k -> new EnumMap<>(ElementType.class))
                    .put(type, currentTime);
        }
    }

    /**
     * Gets the last damage time for an entity and element type.
     *
     * @param entity the entity
     * @param type the element type
     * @return the last damage time in ticks, or null if not tracked
     */
    public static Long getLastDamageTime(LivingEntity entity, ElementType type) {
        synchronized (LAST_DAMAGE_LOCK) {
            Map<ElementType, Long> typeTimes = LAST_DAMAGE_TIME.get(entity.getId());
            return typeTimes != null ? typeTimes.get(type) : null;
        }
    }

    /**
     * Removes the last damage time entry for a specific entity and element type.
     *
     * @param entity the entity
     * @param type the element type
     */
    public static void removeLastDamageTime(LivingEntity entity, ElementType type) {
        synchronized (LAST_DAMAGE_LOCK) {
            LAST_DAMAGE_TIME.computeIfPresent(entity.getId(), (id, map) -> {
                map.remove(type);
                return map.isEmpty() ? null : map;
            });
        }
    }

    /**
     * Removes all last damage time entries for an entity.
     *
     * @param entity the entity
     */
    public static void removeAllDamageTimes(LivingEntity entity) {
        synchronized (LAST_DAMAGE_LOCK) {
            LAST_DAMAGE_TIME.remove(entity.getId());
        }
    }

    /**
     * Gets all tracked element types for an entity.
     *
     * @param entity the entity
     * @return a map of element types to their last damage times
     */
    public static Map<ElementType, Long> getTrackedTypes(LivingEntity entity) {
        synchronized (LAST_DAMAGE_LOCK) {
            Map<ElementType, Long> typeTimes = LAST_DAMAGE_TIME.get(entity.getId());
            return typeTimes != null ? new EnumMap<>(typeTimes) : null;
        }
    }

    /**
     * Gets the lock object for thread-safe operations.
     *
     * @return the lock object
     */
    public static Object getLock() {
        return LAST_DAMAGE_LOCK;
    }

    /**
     * Gets the internal map for iteration during cleanup.
     * Should only be used with proper synchronization.
     *
     * @return the internal map
     */
    public static Map<Integer, Map<ElementType, Long>> getInternalMap() {
        return LAST_DAMAGE_TIME;
    }
}
