package com.auranite.abloom;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages damage modification callbacks from other mods.
 * Allows other mods to register damage modification handlers with specific priorities.
 */
public class DamageModificationManager {

    private static final List<DamageModifierWrapper> MODIFIERS = new CopyOnWriteArrayList<>();

    private DamageModificationManager() {}

    /**
     * Damage modifier callback interface.
     * Mods can register their damage modification logic through this interface.
     */
    @FunctionalInterface
    public interface DamageModifier {
        /**
         * Called when damage is being processed.
         * @param target the entity taking damage
         * @param source the damage source
         * @param baseDamage the original damage before any modifications
         * @param currentDamage the current damage (may have been modified by other mods)
         * @return the modified damage
         */
        float modifyDamage(LivingEntity target, DamageSource source, float baseDamage, float currentDamage);
    }

    /**
     * Registers a damage modifier with the specified priority.
     * Higher priority modifiers are called first.
     * When priorities are equal, modifiers are called in registration order (FIFO).
     * After registration, all registered modifiers are sorted by priority (descending).
     *
     * @param modifier the damage modifier callback
     * @param priority the priority (higher = called first)
     */
    public static void registerModifier(DamageModifier modifier, int priority) {
        if (modifier == null) return;
        MODIFIERS.add(new DamageModifierWrapper(modifier, priority));
        // Sort modifiers by priority in descending order (highest priority first)
        // For equal priorities, maintain registration order (stable sort)
        Collections.sort(MODIFIERS, (a, b) -> Integer.compare(b.priority, a.priority));
    }

    /**
     * Processes damage through registered modifiers with priority > 0 (before Abloom).
     * Modifiers are called in priority order (highest first).
     *
     * @param target the entity taking damage
     * @param source the damage source
     * @param baseDamage the original damage
     * @return the damage after high-priority modifications
     */
    public static float processHighPriorityDamage(LivingEntity target, DamageSource source, float baseDamage) {
        float currentDamage = baseDamage;

        for (DamageModifierWrapper wrapper : MODIFIERS) {
            // Only process modifiers with priority > 0 (before Abloom)
            if (wrapper.priority > 0) {
                try {
                    currentDamage = wrapper.modifyDamage(target, source, baseDamage, currentDamage);
                } catch (Exception e) {
                    AbloomMod.LOGGER.error("Error in high-priority damage modifier callback", e);
                }
            }
        }

        return currentDamage;
    }

    /**
     * Processes damage through registered modifiers with priority <= 0 (after Abloom).
     * Modifiers are called in priority order (highest first).
     *
     * @param target the entity taking damage
     * @param source the damage source
     * @param currentDamage the damage after Abloom's processing
     * @return the damage after low-priority modifications
     */
    public static float processLowPriorityDamage(LivingEntity target, DamageSource source, float currentDamage) {
        float finalDamage = currentDamage;

        for (DamageModifierWrapper wrapper : MODIFIERS) {
            // Only process modifiers with priority <= 0 (after Abloom)
            if (wrapper.priority <= 0) {
                try {
                    finalDamage = wrapper.modifyDamage(target, source, currentDamage, finalDamage);
                } catch (Exception e) {
                    AbloomMod.LOGGER.error("Error in low-priority damage modifier callback", e);
                }
            }
        }

        return finalDamage;
    }

    /**
     * Gets the number of registered modifiers.
     * @return number of registered modifiers
     */
    public static int getRegisteredModifierCount() {
        return MODIFIERS.size();
    }

    /**
     * Clears all registered modifiers.
     */
    public static void clearAllModifiers() {
        MODIFIERS.clear();
    }

    private static class DamageModifierWrapper {
        final DamageModifier modifier;
        final int priority;

        DamageModifierWrapper(DamageModifier modifier, int priority) {
            this.modifier = modifier;
            this.priority = priority;
        }

        float modifyDamage(LivingEntity target, DamageSource source, float baseDamage, float currentDamage) {
            return modifier.modifyDamage(target, source, baseDamage, currentDamage);
        }
    }
}
