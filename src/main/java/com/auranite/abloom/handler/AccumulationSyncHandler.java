package com.auranite.abloom.handler;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.init.AbloomModAttachments;
import com.auranite.abloom.network.SyncResonanceAccumulationMessage;
import com.auranite.abloom.util.ElementType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.EnumMap;
import java.util.Map;

/**
 * Handles synchronization of resonance accumulation data to clients.
 */
public class AccumulationSyncHandler {

    private AccumulationSyncHandler() {
        // Utility class
    }

    /**
     * Syncs resonance accumulation data for an entity to all tracking players.
     * Only sends elements with points > 0.
     *
     * @param entity the entity to sync
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
}
