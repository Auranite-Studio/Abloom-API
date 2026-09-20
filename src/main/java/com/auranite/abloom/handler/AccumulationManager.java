package com.auranite.abloom.handler;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.init.AbloomModAttachments;
import com.auranite.abloom.init.AbloomModEffects;
import com.auranite.abloom.util.ElementResistanceManager;
import com.auranite.abloom.util.ElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import com.auranite.abloom.network.SyncResonanceAccumulationMessage;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages resonance accumulation tracking for entities.
 * Handles point addition, threshold checks, resets, and client synchronization.
 */
public class AccumulationManager {
    
    private static final Map<Integer, Long> DAMAGE_COOLDOWNS = new ConcurrentHashMap<>();
    private static final int COOLDOWN_TICKS = 5;
    private static final Map<Integer, Map<ElementType, Long>> LAST_DAMAGE_TIME = new ConcurrentHashMap<>();
    private static final Object LAST_DAMAGE_LOCK = new Object();
    
    private static float baseAccumulation = 0f;
    private static final int THRESHOLD = 100;
    private static final int RESET_DELAY_TICKS = 300;
    
    // Erosion cooldown map
    private static final Map<Integer, Long> EROSION_COOLDOWNS = new ConcurrentHashMap<>();
    private static final int EROSION_COOLDOWN_TICKS = 100;

    /**
     * Adds accumulation points to an entity for a specific element type.
     */
    public static void addPoints(LivingEntity entity, ElementType type, int points) {
        int calculatedPoints = ElementResistanceManager.calculateAccumulationPoints(entity, type, points);
        if (calculatedPoints > 0) {
            AbloomModAttachments.addPoints(entity, type, calculatedPoints);
            updateLastDamageTime(entity, type);
        }
    }

    /**
     * Checks if threshold is reached and applies the effect via callback.
     * @return true if threshold was reached and reset performed
     */
    public static boolean checkAndApplyThreshold(LivingEntity target, ElementType type, 
                                                  ThresholdEffectApplier effectApplier) {
        int currentPoints = AbloomModAttachments.getPoints(target, type);
        if (currentPoints >= THRESHOLD) {
            if (AbloomMod.LOGGER.isDebugEnabled()) {
                AbloomMod.LOGGER.debug("Accumulation threshold reached for {} (type: {}). Applying effect.", 
                    target.getName().getString(), type);
            }
            effectApplier.applyThreshold(target, type);
            AbloomModAttachments.resetPoints(target, type);
            syncAccumulationToClients(target);
            return true;
        }
        return false;
    }

    /**
     * Syncs resonance accumulation data for an entity to all tracking players.
     */
    public static void syncAccumulationToClients(LivingEntity entity) {
        if (entity.level().isClientSide) return;

        Map<ElementType, Integer> accumulator = AbloomModAttachments.getAccumulator(entity);
        Map<ElementType, Integer> nonZeroPoints = new EnumMap<>(ElementType.class);
        for (Map.Entry<ElementType, Integer> entry : accumulator.entrySet()) {
            if (entry.getValue() > 0) {
                nonZeroPoints.put(entry.getKey(), entry.getValue());
            }
        }

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                entity,
                new SyncResonanceAccumulationMessage(entity.getId(), nonZeroPoints)
        );
    }

    /**
     * Gets current accumulation points for an entity and element type.
     */
    public static int getPoints(LivingEntity entity, ElementType type) {
        return AbloomModAttachments.getPoints(entity, type);
    }

    /**
     * Resets accumulation points for a specific element type.
     */
    public static void resetPoints(LivingEntity entity, ElementType type) {
        AbloomModAttachments.resetPoints(entity, type);
        synchronized (LAST_DAMAGE_LOCK) {
            LAST_DAMAGE_TIME.computeIfPresent(entity.getId(), (id, map) -> {
                map.remove(type);
                return map.isEmpty() ? null : map;
            });
        }
        syncAccumulationToClients(entity);
    }

    /**
     * Resets all accumulation points for an entity.
     */
    public static void resetAllPoints(LivingEntity entity) {
        for (ElementType type : ElementType.values()) {
            AbloomModAttachments.resetPoints(entity, type);
        }
        synchronized (LAST_DAMAGE_LOCK) {
            LAST_DAMAGE_TIME.remove(entity.getId());
        }
        syncAccumulationToClients(entity);
    }

    /**
     * Gets accumulation progress as percentage (0-100).
     */
    public static int getProgressPercent(LivingEntity entity, ElementType type) {
        return THRESHOLD > 0 ? (AbloomModAttachments.getPoints(entity, type) * 100) / THRESHOLD : 0;
    }

    /**
     * Updates last damage time for an entity and element type.
     */
    private static void updateLastDamageTime(LivingEntity entity, ElementType type) {
        synchronized (LAST_DAMAGE_LOCK) {
            LAST_DAMAGE_TIME.computeIfAbsent(entity.getId(), k -> new ConcurrentHashMap<>())
                    .put(type, entity.level().getGameTime());
        }
    }

    /**
     * Checks and resets inactive points after timeout.
     */
    public static void checkAndResetInactivePoints() {
        // Implementation for cleaning up stale accumulation data
        // This would be called periodically from server tick handler
    }

    /**
     * Gets erosion cooldown status for an entity.
     */
    public static boolean canTriggerErosion(LivingEntity entity) {
        long currentTime = entity.level().getGameTime();
        long lastErosionTime = EROSION_COOLDOWNS.getOrDefault(entity.getId(), 0L);
        return currentTime - lastErosionTime >= EROSION_COOLDOWN_TICKS;
    }

    /**
     * Records erosion trigger for cooldown tracking.
     */
    public static void recordErosionTrigger(LivingEntity entity) {
        EROSION_COOLDOWNS.put(entity.getId(), entity.level().getGameTime());
    }

    public static float getBaseAccumulation() {
        return baseAccumulation;
    }

    public static void setBaseAccumulation(float value) {
        baseAccumulation = value;
    }

    public static int getThreshold() {
        return THRESHOLD;
    }

    /**
     * Functional interface for applying threshold effects.
     */
    @FunctionalInterface
    public interface ThresholdEffectApplier {
        void applyThreshold(LivingEntity target, ElementType type);
    }
}
