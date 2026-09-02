package com.auranite.abloom.client;

import com.auranite.abloom.config.AbloomConfig;
import com.auranite.abloom.network.ClientEntityEffectsStorage;
import com.auranite.abloom.util.EffectRenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;

@OnlyIn(Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != Stage.AFTER_ENTITIES) return;

        var poseStack = event.getPoseStack();
        var mc = Minecraft.getInstance();
        var buffers = mc.renderBuffers().bufferSource();
        var camera = event.getCamera();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        var level = mc.level;
        if (level == null) return;

        var camPos = camera.getPosition();

        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof LivingEntity livingEntity && ClientEntityEffectsStorage.hasEntityEffects(entity.getId())) {
                boolean isPlayerSelf = entity instanceof Player && mc.player != null && entity.getId() == mc.player.getId();
                boolean isOtherPlayer = entity instanceof Player && !isPlayerSelf;
                boolean shouldRender = !(entity instanceof Player) ||
                        isPlayerSelf && AbloomConfig.CLIENT_CONFIG.SHOW_SELF_POTION.get() ||
                        isOtherPlayer && AbloomConfig.CLIENT_CONFIG.SHOW_OTHER_POTION.get();

                if (!shouldRender) continue;

                double x = entity.getX();
                double y = entity.getY();
                double z = entity.getZ();

                float distance = (float) Math.sqrt(
                        (x - camPos.x) * (x - camPos.x) +
                        (y - camPos.y) * (y - camPos.y) +
                        (z - camPos.z) * (z - camPos.z));

                float maxDist = ((Number) AbloomConfig.CLIENT_CONFIG.MAX_DISTANCE.get()).floatValue();
                float maxDistNoLoS = ((Number) AbloomConfig.CLIENT_CONFIG.MAX_DISTANCE_WITHOUT_LINE_OF_SIGHT.get()).floatValue();

                if (distance > maxDist) continue;
                if (distance > maxDistNoLoS) {
                    if (!livingEntity.hasLineOfSight(camera.getEntity())) continue;
                }

                var effects = ClientEntityEffectsStorage.getEntityEffects(entity.getId());
                if (effects == null || effects.isEmpty()) {
                    ClientEntityEffectsStorage.removeEntityEffects(entity.getId());
                    continue;
                }

                var dispatcher = mc.getEntityRenderDispatcher();
                var entityRenderer = dispatcher.getRenderer(entity);
                EffectRenderUtil.renderAllMobEffects(entity, poseStack, buffers, camera, entityRenderer, partialTick,
                        x - camPos.x, y - camPos.y, z - camPos.z, effects, false);
            }
        }
    }
}
