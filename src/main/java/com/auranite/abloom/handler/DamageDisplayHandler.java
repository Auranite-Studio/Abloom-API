package com.auranite.abloom.handler;

import com.auranite.abloom.init.AbloomModEffects;
import com.auranite.abloom.network.SpawnDamageNumberPacket;
import com.auranite.abloom.network.SpawnStatusTextPacket;
import com.auranite.abloom.util.ElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Handles spawning damage numbers and status text packets for clients.
 */
public class DamageDisplayHandler {

    private DamageDisplayHandler() {
        // Utility class
    }

    /**
     * Spawns a damage number display for an entity.
     *
     * @param entity the entity that was damaged
     * @param amount the damage amount
     * @param type the element type (can be null)
     */
    public static void spawnDamageNumber(LivingEntity entity, float amount, ElementType type) {
        spawnDamageNumber(entity, amount, type, false, false);
    }

    /**
     * Spawns a damage number display for an entity with crit information.
     *
     * @param entity the entity that was damaged
     * @param amount the damage amount
     * @param type the element type (can be null)
     * @param isCrit true if this was a critical hit
     * @param isMultiCrit true if this was a multi-crit hit
     */
    public static void spawnDamageNumber(LivingEntity entity, float amount, ElementType type, boolean isCrit, boolean isMultiCrit) {
        // Only send packet from server side
        if (entity.level().isClientSide) return;

        int color = DamageColorManager.getDamageColor(type);
        boolean hasBreak = entity.hasEffect(AbloomModEffects.BREAK);
        PacketDistributor.sendToPlayersTrackingEntity(
            entity,
            new SpawnDamageNumberPacket(entity.getId(), amount, type, color, isCrit, hasBreak, isMultiCrit)
        );
    }

    /**
     * Spawns a status text display for an entity.
     *
     * @param entity the entity
     * @param textComponent the text to display
     * @param color the color of the text
     */
    public static void spawnStatusText(LivingEntity entity, Component textComponent, int color) {
        // Only send packet from server side
        if (entity.level().isClientSide) return;

        PacketDistributor.sendToPlayersTrackingEntity(
            entity,
            new SpawnStatusTextPacket(entity.getId(), textComponent, color)
        );
    }

    /**
     * Spawns a status text display for an entity.
     *
     * @param entity the entity
     * @param text the text to display
     * @param color the color of the text
     */
    public static void spawnStatusText(LivingEntity entity, String text, int color) {
        spawnStatusText(entity, Component.literal(text), color);
    }
}
