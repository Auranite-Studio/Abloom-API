package com.auranite.abloom.handler;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.init.AbloomModAttachments;
import com.auranite.abloom.init.AbloomModEffects;
import com.auranite.abloom.network.SpawnDamageNumberPacket;
import com.auranite.abloom.network.SpawnStatusTextPacket;
import com.auranite.abloom.util.ElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Handles visual feedback for damage including damage numbers and status text.
 */
public class DamageDisplayManager {
    
    private static final int DEFAULT_COLOR = 0xFFFFFF;
    
    /**
     * Spawns a damage number at the entity's location.
     */
    public static void spawnDamageNumber(LivingEntity entity, float amount, ElementType type) {
        spawnDamageNumber(entity, amount, type, false, false);
    }
    
    /**
     * Spawns a damage number with crit indicators.
     */
    public static void spawnDamageNumber(LivingEntity entity, float amount, ElementType type, 
                                          boolean isCrit, boolean isMultiCrit) {
        if (entity == null || entity.level().isClientSide) return;
        
        int color = ElementDamageHandler.getDamageColor(type);
        
        if (entity.level() instanceof ServerLevel serverLevel) {
            SpawnDamageNumberPacket packet = new SpawnDamageNumberPacket(
                entity.getId(), amount, type, color, isCrit, false, isMultiCrit
            );
            
            for (ServerPlayer player : serverLevel.players()) {
                if (canPlayerSeeEntity(player, entity)) {
                    PacketDistributor.sendToPlayer(player, packet);
                }
            }
        }
    }
    
    /**
     * Spawns status text at the entity's location.
     */
    public static void spawnStatusText(LivingEntity entity, Component textComponent, int color) {
        if (entity == null || entity.level().isClientSide) return;
        
        if (entity.level() instanceof ServerLevel serverLevel) {
            SpawnStatusTextPacket packet = new SpawnStatusTextPacket(entity.getId(), textComponent, color);
            
            for (ServerPlayer player : serverLevel.players()) {
                if (canPlayerSeeEntity(player, entity)) {
                    packet.send(player);
                }
            }
        }
    }
    
    /**
     * Spawns status text with string parameter.
     */
    public static void spawnStatusText(LivingEntity entity, String text, int color) {
        spawnStatusText(entity, Component.literal(text), color);
    }
    
    /**
     * Checks if damage display should be shown for an entity.
     */
    public static boolean canShowDamage(LivingEntity entity) {
        // Implement visibility logic based on game settings
        return entity != null && !entity.isSpectator();
    }
    
    /**
     * Checks if a player can see an entity (for network optimization).
     */
    private static boolean canPlayerSeeEntity(ServerPlayer player, Entity entity) {
        return player.distanceToSqr(entity) < 256.0; // 16 blocks squared
    }
}
