package com.auranite.abloom.util;

import com.auranite.abloom.config.AbloomConfig;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.auranite.abloom.network.ClientEntityEffectsStorage;

import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class EffectRenderUtil {

    private static final float ICON_BASE_SIZE = 10.0F;
    private static final float ICON_SPACING = 2.0F;
    private static final float SCALE_FACTOR = 0.0267F;
    private static final float BACKGROUND_OPACITY = 0.25F;
    private static final float DURATION_TEXT_SCALE = 0.5F;
    private static final int WARNING_THRESHOLD_SECONDS = 5;

    public static void renderAllMobEffects(Entity entity, PoseStack poseStack, MultiBufferSource buffers, Camera camera, EntityRenderer<? super Entity> entityRenderer, float partialTicks, double x, double y, double z, List<MobEffectInstance> effects, boolean isGuiEnvironment) {
        int entityId = entity.getId();
        Vec3 renderOffset = entityRenderer.getRenderOffset(entity, partialTicks);

        poseStack.pushPose();
        poseStack.translate(x + renderOffset.x(), y + renderOffset.y(), z + renderOffset.z());
        poseStack.translate(0.0F, getVerticalOffset(entity, poseStack), 0.0F);
        if (!isGuiEnvironment) {
            poseStack.mulPose(camera.rotation());
        }
        poseStack.translate(AbloomConfig.CLIENT_CONFIG.getHorizontalOffset(), 0.0F, 0.0F);
        poseStack.mulPose(new Quaternionf(new AxisAngle4f(180.0F * (float) Math.toRadians(1.0), 0.0F, 1.0F, 0.0F)));
        poseStack.scale(-SCALE_FACTOR, -SCALE_FACTOR, SCALE_FACTOR);

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        float scale = (float) AbloomConfig.CLIENT_CONFIG.getRenderScale();
        float iconWidth = ICON_BASE_SIZE * scale;
        float iconHeight = ICON_BASE_SIZE * scale;
        int maxIconsPerRow = Math.max(1, (int) Math.floor((entity.getBbWidth() / SCALE_FACTOR * 3.0F + ICON_SPACING) / (iconWidth + ICON_SPACING)));

        int totalRows = (int) Math.ceil((double) effects.size() / maxIconsPerRow);
        for (int row = 0; row < totalRows; ++row) {
            renderRow(effects, row, maxIconsPerRow, iconWidth, iconHeight, entityId, font, buffers, minecraft, poseStack);
        }

        poseStack.popPose();
    }

    private static void renderRow(List<MobEffectInstance> effects, int row, int maxIconsPerRow, float iconWidth, float iconHeight, int entityId, Font font, MultiBufferSource buffers, Minecraft minecraft, PoseStack basePoseStack) {
        int startIndex = row * maxIconsPerRow;
        int endIndex = Math.min(startIndex + maxIconsPerRow, effects.size());

        // Count visible icons to calculate row width
        int visibleIcons = 0;
        for (int i = startIndex; i < endIndex; ++i) {
            if (isDisplayEffect(effects.get(i).getEffect())) visibleIcons++;
        }
        if (visibleIcons == 0) return;

        float rowWidth = visibleIcons * iconWidth + (visibleIcons - 1) * ICON_SPACING;
        float startX = -rowWidth / 2.0F + iconWidth / 2.0F;
        float rowY = -(float) row * (iconHeight + ICON_SPACING);
        int visibleIndex = 0;

        for (int i = startIndex; i < endIndex; ++i) {
            MobEffectInstance effectInstance = effects.get(i);
            if (!isDisplayEffect(effectInstance.getEffect())) continue;

            TextureAtlasSprite sprite = minecraft.getMobEffectTextures().get(effectInstance.getEffect());
            float halfSize = iconWidth / 2.0F;
            float iconX = startX + visibleIndex * (iconWidth + ICON_SPACING);

            PoseStack iconPoseStack = new PoseStack();
            iconPoseStack.mulPose(basePoseStack.last().pose());
            iconPoseStack.translate(iconX, rowY, 0.0F);

            drawBackground(iconPoseStack, font, buffers);
            try {
                drawSprite(iconPoseStack, sprite, halfSize, buffers);
            } catch (Exception e) {
                drawFallbackText(iconPoseStack, font, buffers, effectInstance.getDescriptionId());
            }
            drawDuration(iconPoseStack, halfSize, entityId, effectInstance, font, buffers);
            visibleIndex++;
        }
    }

    private static void drawBackground(PoseStack poseStack, Font font, MultiBufferSource buffers) {
        font.drawInBatch("", 0.0F, 0.0F, Float.floatToIntBits(BACKGROUND_OPACITY) << 24, false, poseStack.last().pose(), buffers, DisplayMode.SEE_THROUGH, 0, 15728880);
    }

    private static void drawSprite(PoseStack poseStack, TextureAtlasSprite sprite, float halfSize, MultiBufferSource buffers) throws Exception {
        CompositeState state = CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(sprite.atlasLocation(), false, false))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                .createCompositeState(false);

        RenderType renderType = RenderType.create("buffered_effect_icon", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 1536, state);
        VertexConsumer buffer = buffers.getBuffer(renderType);
        Matrix4f matrix = poseStack.last().pose();

        float u0 = sprite.getU0(), u1 = sprite.getU1(), v0 = sprite.getV0(), v1 = sprite.getV1();
        buffer.addVertex(matrix, -halfSize, -halfSize, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u0, v0).setUv1(0, 10).setUv2(240, 240).setNormal(0.0F, 0.0F, 1.0F);
        buffer.addVertex(matrix, -halfSize, halfSize, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u0, v1).setUv1(0, 10).setUv2(240, 240).setNormal(0.0F, 0.0F, 1.0F);
        buffer.addVertex(matrix, halfSize, halfSize, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u1, v1).setUv1(0, 10).setUv2(240, 240).setNormal(0.0F, 0.0F, 1.0F);
        buffer.addVertex(matrix, halfSize, -halfSize, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u1, v0).setUv1(0, 10).setUv2(240, 240).setNormal(0.0F, 0.0F, 1.0F);
    }

    private static void drawFallbackText(PoseStack poseStack, Font font, MultiBufferSource buffers, String text) {
        float width = font.width(text);
        font.drawInBatch(text, -width / 2.0F, 0.0F, Float.floatToIntBits(BACKGROUND_OPACITY) << 24, false, poseStack.last().pose(), buffers, DisplayMode.SEE_THROUGH, 0, 15728880);
        font.drawInBatch(text, -width / 2.0F, 0.0F, 0xFF0000, false, poseStack.last().pose(), buffers, DisplayMode.SEE_THROUGH, 0, 15728880);
    }

    private static void drawDuration(PoseStack poseStack, float halfSize, int entityId, MobEffectInstance effectInstance, Font font, MultiBufferSource buffers) {
        int realDuration = ClientEntityEffectsStorage.getRemainingTicks(
                entityId,
                effectInstance.getEffect(),
                effectInstance.getDuration());
        int seconds = realDuration > 0 ? (realDuration + 9) / 20 : 0;
        String text = seconds > 0 ? seconds + "s" : "--";
        int color = seconds > WARNING_THRESHOLD_SECONDS ? -1 : 0xFF0000;

        PoseStack textPose = new PoseStack();
        textPose.mulPose(poseStack.last().pose());
        textPose.scale(DURATION_TEXT_SCALE, DURATION_TEXT_SCALE, 1.0F);

        float tw = font.width(text) * DURATION_TEXT_SCALE;
        float th = 9.0F * DURATION_TEXT_SCALE;
        font.drawInBatch(text, halfSize - tw, halfSize - th, color, false, textPose.last().pose(), buffers, DisplayMode.NORMAL, 0, 15728880);
    }

    public static float getVerticalOffset(Entity entity, PoseStack poseStack) {
        return entity.getBbHeight() + 0.6F + (float) AbloomConfig.CLIENT_CONFIG.getVerticalOffset();
    }

    private static boolean isDisplayEffect(Holder<MobEffect> effectHolder) {
        return effectHolder.unwrap().left()
                .map(key -> AbloomConfig.DISPLAY_EFFECTS.contains(key.location().getPath()))
                .orElse(false);
    }
}
