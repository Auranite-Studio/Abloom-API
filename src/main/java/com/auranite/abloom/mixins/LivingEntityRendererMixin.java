package com.auranite.abloom.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.auranite.abloom.config.AbloomConfig;
import com.auranite.abloom.network.ClientEntityEffectsStorage;
import com.auranite.abloom.util.EffectRenderUtil;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin({LevelRenderer.class})
public class LivingEntityRendererMixin {
    private static final Logger log = LoggerFactory.getLogger(LivingEntityRendererMixin.class);
    @Shadow
    @Final
    private EntityRenderDispatcher entityRenderDispatcher;

    @Inject(
        method = {"renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"},
        at = {@At(
    value = "INVOKE",
    target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
    shift = Shift.AFTER
)}
    )
    private void renderMobEffectsAboveHead(Entity entity, double camX, double camY, double camZ, float partialTick, PoseStack poseStack, MultiBufferSource buffers, CallbackInfo ci) {
        double d0 = Mth.lerp((double) partialTick, entity.xOld, entity.getX());
        double d1 = Mth.lerp((double) partialTick, entity.yOld, entity.getY());
        double d2 = Mth.lerp((double) partialTick, entity.zOld, entity.getZ());
        if (entity instanceof LivingEntity livingEntity) {
            Level level = entity.level();
            if (level.isClientSide && ClientEntityEffectsStorage.hasEntityEffects(entity.getId())) {
                boolean isPlayerSelf = entity instanceof Player && Minecraft.getInstance().player != null && entity.getId() == Minecraft.getInstance().player.getId();
                boolean isOtherPlayer = entity instanceof Player && !isPlayerSelf;
                boolean shouldRender = !(entity instanceof Player) || isPlayerSelf && (Boolean) AbloomConfig.CLIENT_CONFIG.SHOW_SELF_POTION.get() || isOtherPlayer && (Boolean) AbloomConfig.CLIENT_CONFIG.SHOW_OTHER_POTION.get();
                if (shouldRender) {
                    double dx = d0 - camX;
                    double dy = d1 - camY;
                    double dz = d2 - camZ;
                    float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                    float maxDist = ((Double) AbloomConfig.CLIENT_CONFIG.MAX_DISTANCE.get()).floatValue();
                    float maxDistNoLoS = ((Double) AbloomConfig.CLIENT_CONFIG.MAX_DISTANCE_WITHOUT_LINE_OF_SIGHT.get()).floatValue();
                    if (distance > maxDist) {
                        return;
                    }

                    if (distance > maxDistNoLoS) {
                        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
                        if (cameraEntity != null && !livingEntity.hasLineOfSight(cameraEntity)) {
                            return;
                        }
                    }

                    List<MobEffectInstance> effects = ClientEntityEffectsStorage.getEntityEffects(entity.getId());
                    if (effects == null || effects.isEmpty()) {
                        ClientEntityEffectsStorage.removeEntityEffects(entity.getId());
                        return;
                    }

                    EffectRenderUtil.renderAllMobEffects(entity, poseStack, buffers, this.entityRenderDispatcher.camera, this.entityRenderDispatcher.getRenderer(entity), partialTick, d0 - camX, d1 - camY, d2 - camZ, effects, false);
                }
            }
        }

    }
}
