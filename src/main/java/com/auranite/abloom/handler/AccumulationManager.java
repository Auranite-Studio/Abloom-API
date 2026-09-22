package com.auranite.abloom.handler;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.init.AbloomModAttachments;
import com.auranite.abloom.init.AbloomModEffects;
import com.auranite.abloom.util.ElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Handles resonance accumulation point management and reset logic.
 * Manages the threshold-based effect activation system.
 */
public class AccumulationManager {

    private static float baseAccumulation = 0f;
    private static final int THRESHOLD = 100;
    private static final int RESET_DELAY_TICKS = 300;

    private AccumulationManager() {
        // Utility class
    }

    /**
     * Gets the base accumulation value.
     *
     * @return the base accumulation
     */
    public static float getBaseAccumulation() {
        return baseAccumulation;
    }

    /**
     * Sets the base accumulation value.
     *
     * @param value the new base accumulation
     */
    public static void setBaseAccumulation(float value) {
        baseAccumulation = value;
    }

    /**
     * Gets the threshold for triggering elemental effects.
     *
     * @return the threshold value
     */
    public static int getThreshold() {
        return THRESHOLD;
    }

    /**
     * Gets the reset delay in ticks (time after which inactive points are reset).
     *
     * @return the reset delay in ticks
     */
    public static int getResetDelayTicks() {
        return RESET_DELAY_TICKS;
    }

    /**
     * Checks if accumulation points should be reset due to inactivity.
     * Iterates through all tracked entities and resets points for types that haven't been damaged recently.
     *
     * @param currentServer the current server instance
     */
    public static void checkAndResetInactivePoints(MinecraftServer currentServer) {
        if (currentServer == null) return;
        long currentTime = currentServer.overworld().getGameTime();
        long expiryTime = currentTime - RESET_DELAY_TICKS;

        synchronized (DamageTimeTracker.getLock()) {
            var entityIterator = DamageTimeTracker.getInternalMap().entrySet().iterator();
            while (entityIterator.hasNext()) {
                var entityEntry = entityIterator.next();
                int entityId = entityEntry.getKey();
                var typeTimes = entityEntry.getValue();

                LivingEntity livingEntity = null;
                for (var level : currentServer.getAllLevels()) {
                    Entity entity = level.getEntity(entityId);
                    if (entity instanceof LivingEntity le && le.isAlive()) {
                        livingEntity = le;
                        break;
                    }
                }

                if (livingEntity == null) {
                    entityIterator.remove();
                    continue;
                }

                var typeIterator = typeTimes.entrySet().iterator();
                while (typeIterator.hasNext()) {
                    var typeEntry = typeIterator.next();
                    if (typeEntry.getValue() <= expiryTime) {
                        AbloomModAttachments.resetPoints(livingEntity, typeEntry.getKey());
                        ElementDamageHandler.syncAccumulationToClients(livingEntity);
                        typeIterator.remove();
                    }
                }

                if (typeTimes.isEmpty()) entityIterator.remove();
            }
        }
    }

    /**
     * Adds accumulation points to an entity for a specific element type.
     *
     * @param entity the entity
     * @param type the element type
     * @param points the points to add
     */
    public static void addPoints(LivingEntity entity, ElementType type, int points) {
        AbloomModAttachments.addPoints(entity, type, points);
        DamageTimeTracker.updateLastDamageTime(entity, type, entity.level().getGameTime());
    }

    /**
     * Resets accumulation points for an entity and element type.
     *
     * @param entity the entity
     * @param type the element type
     */
    public static void resetPoints(LivingEntity entity, ElementType type) {
        AbloomModAttachments.resetPoints(entity, type);
        DamageTimeTracker.removeLastDamageTime(entity, type);
    }

    /**
     * Resets all accumulation points for an entity.
     *
     * @param entity the entity
     */
    public static void resetAllPoints(LivingEntity entity) {
        for (ElementType type : ElementType.values()) {
            AbloomModAttachments.resetPoints(entity, type);
        }
        DamageTimeTracker.removeAllDamageTimes(entity);
    }

    /**
     * Gets the current accumulation points for an entity and element type.
     *
     * @param entity the entity
     * @param type the element type
     * @return the current points
     */
    public static int getPoints(LivingEntity entity, ElementType type) {
        return AbloomModAttachments.getPoints(entity, type);
    }

    /**
     * Calculates the progress percentage towards the threshold.
     *
     * @param entity the entity
     * @param type the element type
     * @return the progress as a percentage (0-100)
     */
    public static int getProgressPercent(LivingEntity entity, ElementType type) {
        return THRESHOLD > 0 ? (AbloomModAttachments.getPoints(entity, type) * 100) / THRESHOLD : 0;
    }

    /**
     * Checks if the threshold has been reached for an entity and element type.
     *
     * @param entity the entity
     * @param type the element type
     * @return true if threshold is reached or exceeded
     */
    public static boolean isThresholdReached(LivingEntity entity, ElementType type) {
        return AbloomModAttachments.getPoints(entity, type) >= THRESHOLD;
    }
}
