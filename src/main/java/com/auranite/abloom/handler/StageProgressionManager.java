package com.auranite.abloom.handler;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.datapack.ElementalWeaponRegistry;
import com.auranite.abloom.init.AbloomModEffects;
import com.auranite.abloom.util.ElementType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages multi-stage weapon progression tracking.
 * Handles stage advancement, cooldown tracking, and element override logic.
 */
public class StageProgressionManager {
    
    private static final Map<String, Integer> STAGE_TRACKER = new ConcurrentHashMap<>();
    private static final Map<String, Long> STAGE_TRACKER_TIMES = new ConcurrentHashMap<>();
    private static final int STAGE_TRACKER_STALE_TICKS = 600; // 30 seconds

    /**
     * Gets a unique key for tracking stage progression between attacker and target.
     */
    private static String getStageKey(LivingEntity attacker, LivingEntity target) {
        return attacker.getId() + "_" + target.getId();
    }

    /**
     * Cleans up stale stage tracker entries.
     */
    public static void cleanupStaleStageTracker() {
        long currentTime = System.currentTimeMillis();
        STAGE_TRACKER_TIMES.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > STAGE_TRACKER_STALE_TICKS * 50L);
    }

    /**
     * Updates stage progression for a weapon hit and returns the current stage data.
     * @return StageData for the current stage, or null if not a multi-stage weapon
     */
    public static StageResult updateStageProgression(LivingEntity attacker, LivingEntity target, 
                                                      ItemStack weapon) {
        ResourceLocation weaponId = BuiltInRegistries.ITEM.getKey(weapon.getItem());
        
        if (!ElementalWeaponRegistry.hasStages(weaponId)) {
            return null;
        }

        List<ElementalWeaponRegistry.StageData> stages = ElementalWeaponRegistry.getStages(weaponId);
        String stageKey = getStageKey(attacker, target);
        Integer currentStage = STAGE_TRACKER.getOrDefault(stageKey, 0);
        long currentTime = target.level().getGameTime();

        boolean isFirstHitEver = !STAGE_TRACKER_TIMES.containsKey(stageKey);
        boolean isCooldownExpired = ElementalWeaponRegistry.isCooldownExpired(attacker, target, currentTime);
        int prevStage = currentStage;

        if (isFirstHitEver) {
            currentStage = 0;
            if (AbloomMod.LOGGER.isDebugEnabled()) {
                AbloomMod.LOGGER.debug("Multi-stage weapon {} first hit ever, using stage {}", weaponId, currentStage);
            }
            ElementalWeaponRegistry.resetStagesAndCooldown(attacker, target, currentTime);
        } else if (currentStage == -1) {
            currentStage = 0;
            if (AbloomMod.LOGGER.isDebugEnabled()) {
                AbloomMod.LOGGER.debug("Multi-stage weapon {} after cooldown reset, using stage 0", weaponId);
            }
            ElementalWeaponRegistry.resetStagesAndCooldown(attacker, target, currentTime);
        } else if (!isCooldownExpired) {
            currentStage = (currentStage + 1) % stages.size();
            if (AbloomMod.LOGGER.isDebugEnabled()) {
                AbloomMod.LOGGER.debug("Advancing multi-stage weapon {} from stage {} to {}", weaponId, prevStage, currentStage);
            }
            ElementalWeaponRegistry.resetStagesAndCooldown(attacker, target, currentTime);
        } else {
            currentStage = -1;
            if (AbloomMod.LOGGER.isDebugEnabled()) {
                AbloomMod.LOGGER.debug("Cooldown expired for multi-stage weapon {}, resetting to stage 0 on next hit", weaponId);
            }
            ElementalWeaponRegistry.resetStagesAndCooldown(attacker, target, currentTime);
        }

        STAGE_TRACKER.put(stageKey, currentStage);
        STAGE_TRACKER_TIMES.put(stageKey, currentTime);

        // Handle special case: if stage is -1 (cooldown just expired), use stage 0 for this hit
        int effectiveStage = currentStage == -1 ? 0 : currentStage;
        ElementalWeaponRegistry.StageData stageData = stages.get(effectiveStage);

        return new StageResult(stageData, effectiveStage, currentStage);
    }

    /**
     * Resets stage progression for a specific attacker-target pair.
     */
    public static void resetStageProgression(LivingEntity attacker, LivingEntity target) {
        String stageKey = getStageKey(attacker, target);
        STAGE_TRACKER.remove(stageKey);
        STAGE_TRACKER_TIMES.remove(stageKey);
    }

    /**
     * Gets the current stage number for an attacker-target pair.
     */
    public static int getCurrentStage(LivingEntity attacker, LivingEntity target) {
        String stageKey = getStageKey(attacker, target);
        return STAGE_TRACKER.getOrDefault(stageKey, 0);
    }

    /**
     * Record containing stage progression result.
     */
    public record StageResult(ElementalWeaponRegistry.StageData stageData, 
                              int effectiveStage, 
                              int rawStage) {}
}
