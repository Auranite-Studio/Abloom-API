package com.auranite.abloom.handler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages cooldowns for damage events and erosion effects.
 */
public class DamageCooldownManager {

    private static final Map<Integer, Long> DAMAGE_COOLDOWNS = new ConcurrentHashMap<>();
    private static final int COOLDOWN_TICKS = 5;

    /** Erosion cooldown map: entityId -> last erosion trigger time (in ticks) */
    private static final Map<Integer, Long> EROSION_COOLDOWNS = new ConcurrentHashMap<>();
    private static final int EROSION_COOLDOWN_TICKS = 100; // 5 seconds

    private DamageCooldownManager() {
        // Utility class
    }

    /**
     * Checks if enough time has passed to show damage numbers for an entity.
     *
     * @param entity the entity to check
     * @return true if damage can be shown, false if on cooldown
     */
    public static boolean canShowDamage(LivingEntity entity) {
        long currentTime = entity.level().getGameTime();
        Long lastTime = DAMAGE_COOLDOWNS.get(entity.getId());
        if (lastTime != null && currentTime - lastTime < COOLDOWN_TICKS) return false;
        DAMAGE_COOLDOWNS.put(entity.getId(), currentTime);
        return true;
    }

    /**
     * Gets the last erosion trigger time for an entity.
     *
     * @param entity the entity to check
     * @return the last erosion trigger time in ticks, or 0 if never triggered
     */
    public static long getLastErosionTime(LivingEntity entity) {
        return EROSION_COOLDOWNS.getOrDefault(entity.getId(), 0L);
    }

    /**
     * Records the current time as the last erosion trigger for an entity.
     *
     * @param entity the entity
     * @param currentTime the current game time in ticks
     */
    public static void recordErosionTrigger(LivingEntity entity, long currentTime) {
        EROSION_COOLDOWNS.put(entity.getId(), currentTime);
    }

    /**
     * Checks if the erosion cooldown has expired for an entity.
     *
     * @param entity the entity to check
     * @param currentTime the current game time in ticks
     * @return true if erosion can be triggered, false if on cooldown
     */
    public static boolean isErosionCooldownExpired(LivingEntity entity, long currentTime) {
        long lastErosionTime = EROSION_COOLDOWNS.getOrDefault(entity.getId(), 0L);
        return currentTime - lastErosionTime >= EROSION_COOLDOWN_TICKS;
    }

    /**
     * Removes all cooldown data for an entity (used when entity dies or leaves).
     *
     * @param entity the entity to clean up
     */
    public static void cleanupEntity(Entity entity) {
        DAMAGE_COOLDOWNS.remove(entity.getId());
        EROSION_COOLDOWNS.remove(entity.getId());
    }

    /**
     * Gets the erosion cooldown duration in ticks.
     *
     * @return the erosion cooldown duration
     */
    public static int getErosionCooldownTicks() {
        return EROSION_COOLDOWN_TICKS;
    }

    /**
     * Gets the damage display cooldown duration in ticks.
     *
     * @return the damage display cooldown duration
     */
    public static int getDamageCooldownTicks() {
        return COOLDOWN_TICKS;
    }
}
