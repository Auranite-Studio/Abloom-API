package com.auranite.abloom.handler;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks weapon stage progression for multi-stage weapons.
 * Each (attacker, target) pair has its own stage counter that advances on successive hits.
 */
public class WeaponStageTracker {

    /**
     * Tracks the current stage for each (attacker, target) pair in multi-stage weapons.
     * Value: integer stage index.
     */
    private static final Map<String, Integer> STAGE_TRACKER = new ConcurrentHashMap<>();

    /**
     * Tracks the last access time (in ticks) for each stage tracker key.
     * Used for periodic cleanup of stale entries.
     */
    private static final Map<String, Long> STAGE_TRACKER_TIMES = new ConcurrentHashMap<>();

    /** Time in ticks after which a stage tracker entry is considered stale and can be removed. */
    private static final int STAGE_TRACKER_STALE_TICKS = 600; // 30 seconds

    private WeaponStageTracker() {
        // Utility class
    }

    /**
     * Generates a unique key for tracking stage progress between two entities.
     */
    public static String getStageKey(LivingEntity attacker, LivingEntity target) {
        return attacker.getId() + "_" + target.getId();
    }

    /**
     * Gets the current stage for an attacker-target pair.
     *
     * @param attacker the attacking entity
     * @param target the target entity
     * @return the current stage index, or 0 if not tracked
     */
    public static int getCurrentStage(LivingEntity attacker, LivingEntity target) {
        String stageKey = getStageKey(attacker, target);
        return STAGE_TRACKER.getOrDefault(stageKey, 0);
    }

    /**
     * Sets the stage for an attacker-target pair.
     *
     * @param attacker the attacking entity
     * @param target the target entity
     * @param stage the stage index to set
     * @param currentTime the current game time in ticks
     */
    public static void setStage(LivingEntity attacker, LivingEntity target, int stage, long currentTime) {
        String stageKey = getStageKey(attacker, target);
        STAGE_TRACKER.put(stageKey, stage);
        STAGE_TRACKER_TIMES.put(stageKey, currentTime);
    }

    /**
     * Advances the stage for an attacker-target pair to the next stage.
     *
     * @param attacker the attacking entity
     * @param target the target entity
     * @param stages the list of stage data
     * @param currentTime the current game time in ticks
     * @return the new stage index
     */
    public static int advanceStage(LivingEntity attacker, LivingEntity target, int stagesSize, long currentTime) {
        String stageKey = getStageKey(attacker, target);
        int currentStage = STAGE_TRACKER.getOrDefault(stageKey, 0);
        int newStage = (currentStage + 1) % stagesSize;
        STAGE_TRACKER.put(stageKey, newStage);
        STAGE_TRACKER_TIMES.put(stageKey, currentTime);
        return newStage;
    }

    /**
     * Resets the stage tracker for an attacker-target pair.
     *
     * @param attacker the attacking entity
     * @param target the target entity
     * @param currentTime the current game time in ticks
     */
    public static void resetStage(LivingEntity attacker, LivingEntity target, long currentTime) {
        String stageKey = getStageKey(attacker, target);
        STAGE_TRACKER.remove(stageKey);
        STAGE_TRACKER_TIMES.remove(stageKey);
    }

    /**
     * Checks if a stage tracker entry exists for an attacker-target pair.
     *
     * @param attacker the attacking entity
     * @param target the target entity
     * @return true if the pair is being tracked
     */
    public static boolean hasStageTracker(LivingEntity attacker, LivingEntity target) {
        String stageKey = getStageKey(attacker, target);
        return STAGE_TRACKER_TIMES.containsKey(stageKey);
    }

    /**
     * Clean up stale stage tracker entries that haven't been accessed for STAGE_TRACKER_STALE_TICKS.
     */
    public static void cleanupStaleStageTracker() {
        // Note: This requires access to currentServer which should be passed in or obtained elsewhere
        // For now, this method is a placeholder - actual implementation needs server reference
        STAGE_TRACKER_TIMES.entrySet().removeIf(entry -> {
            // Actual cleanup logic requires server tick time
            // This will be called from ElementDamageHandler with proper context
            return false; // Placeholder
        });
    }

    /**
     * Removes all stage tracker entries for a specific entity (used when entity dies or leaves).
     *
     * @param entityId the entity ID to clean up
     */
    public static void cleanupEntity(int entityId) {
        STAGE_TRACKER.keySet().removeIf(key -> key.contains("_" + entityId + "_"));
        STAGE_TRACKER_TIMES.keySet().removeIf(key -> key.contains("_" + entityId + "_"));
    }

    /**
     * Gets the last access time for a stage tracker entry.
     *
     * @param attacker the attacking entity
     * @param target the target entity
     * @return the last access time in ticks, or null if not tracked
     */
    public static Long getLastAccessTime(LivingEntity attacker, LivingEntity target) {
        String stageKey = getStageKey(attacker, target);
        return STAGE_TRACKER_TIMES.get(stageKey);
    }
}
