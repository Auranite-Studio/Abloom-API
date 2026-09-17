package com.auranite.abloom.client;

import com.auranite.abloom.handler.ElementDamageHandler;
import com.auranite.abloom.init.AbloomModEffects;
import com.auranite.abloom.util.ElementType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResonanceDisplayUtil {

    private static final int THRESHOLD = 100;
    private static final float ICON_BASE_SIZE = 10.0F;
    private static final float ICON_SPACING = 2.0F;
    private static final float TEXT_SCALE = 0.5F;
    private static final float ROW_GAP = 4.0F;
    private static final float BACKGROUND_OPACITY = 0.25F;

    /**
     * Renders accumulated resonance points below existing MobEffect icons.
     * Must be called WHILE poseStack is still in the transformed state (inside push/pop block).
     */
    public static void renderResonanceAccumulation(Entity entity, PoseStack poseStack,
            MultiBufferSource buffers, int entityId, Map<ElementType, Integer> points,
            float iconWidth, float iconHeight, Font font, Minecraft minecraft) {
        if (points.isEmpty()) return;

        float scale = (float) com.auranite.abloom.config.AbloomConfig.CLIENT_CONFIG.getRenderScale();
        float scaledIconWidth = ICON_BASE_SIZE * scale;
        float scaledIconHeight = ICON_BASE_SIZE * scale;

        // Collect visible elements (points > 0) in enum order
        List<ElementType> visibleElements = new ArrayList<>();
        for (ElementType type : ElementType.values()) {
            if (points.getOrDefault(type, 0) > 0) {
                visibleElements.add(type);
            }
        }

        int visibleCount = visibleElements.size();
        if (visibleCount == 0) return;

        // Calculate max icons per row (same logic as EffectRenderUtil)
        int maxIconsPerRow = Math.max(1, (int) Math.floor(
                (entity.getBbWidth() / 0.0267F * 3.0F + ICON_SPACING) / (scaledIconWidth + ICON_SPACING)));
        int totalRows = (int) Math.ceil((double) visibleCount / maxIconsPerRow);

        // Position resonance row BELOW all effect rows + gap
        float resonanceRowStartY = -(float) (totalRows - 1) * (scaledIconHeight + ICON_SPACING) - ROW_GAP;

        for (int row = 0; row < totalRows; row++) {
            int rowStartIdx = row * maxIconsPerRow;
            int rowEndIdx = Math.min(rowStartIdx + maxIconsPerRow, visibleElements.size());

            float rowWidth = (rowEndIdx - rowStartIdx) * scaledIconWidth + Math.max(0, (rowEndIdx - rowStartIdx - 1)) * ICON_SPACING;
            float startX = -rowWidth / 2.0F + scaledIconWidth / 2.0F;
            float rowY = resonanceRowStartY - (float) row * (scaledIconHeight + ICON_SPACING);

            for (int i = rowStartIdx; i < rowEndIdx; i++) {
                ElementType type = visibleElements.get(i);
                int pointsVal = points.get(type);
                float halfSize = scaledIconWidth / 2.0F;
                float iconX = startX + (i - rowStartIdx) * (scaledIconWidth + ICON_SPACING);

                PoseStack iconPose = new PoseStack();
                iconPose.mulPose(poseStack.last().pose());
                iconPose.translate(iconX, rowY, 0.0F);

                // Draw background
                drawBackground(iconPose, font, buffers);

                // Draw element icon
                drawElementIcon(iconPose, type, halfSize, buffers, minecraft);

                // Draw percentage text
                drawPercentageText(iconPose, pointsVal, halfSize, font, buffers);
            }
        }
    }

    private static void drawBackground(PoseStack poseStack, Font font, MultiBufferSource buffers) {
        font.drawInBatch("", 0.0F, 0.0F, Float.floatToIntBits(BACKGROUND_OPACITY) << 24, false,
                poseStack.last().pose(), buffers, DisplayMode.SEE_THROUGH, 0, 15728880);
    }

    private static void drawElementIcon(PoseStack poseStack, ElementType type, float halfSize,
            MultiBufferSource buffers, Minecraft minecraft) {
        // Try to get texture from MobEffect texture map
        Holder<MobEffect> effectHolder = getEffectForElementType(type);
        if (effectHolder != null) {
            TextureAtlasSprite sprite = minecraft.getMobEffectTextures().get(effectHolder);
            if (sprite != null) {
                try {
                    drawSprite(poseStack, sprite, halfSize, buffers);
                } catch (Exception e) {
                    drawColoredSquare(poseStack, halfSize, type, buffers);
                }
                return;
            }
        }

        // Fallback: colored square
        drawColoredSquare(poseStack, halfSize, type, buffers);
    }

    private static Holder<MobEffect> getEffectForElementType(ElementType type) {
        MobEffect effect = switch (type) {
            case FIRE -> AbloomModEffects.BURN.value();
            case ICE -> AbloomModEffects.FREEZE.value();
            case ELECTRIC -> AbloomModEffects.SHOCK.value();
            case NATURAL -> AbloomModEffects.BLOOM.value();
            case ENERGY -> AbloomModEffects.OVERLOAD.value();
            case WATER -> AbloomModEffects.WETNESS.value();
            case EARTH -> AbloomModEffects.STUN.value();
            case QUANTUM -> AbloomModEffects.BREAK.value();
            case ETHER -> AbloomModEffects.CORRUPTION.value();
            case LIGHT -> AbloomModEffects.DISPERSION.value();
            case SHADOW -> AbloomModEffects.ECLIPSE.value();
            case WIND -> AbloomModEffects.WINDSWEPT.value();
            case PHYSICAL -> AbloomModEffects.RUPTURE.value();
            case PRISMATIC -> null;
        };
        if (effect == null) return null;
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);
    }

    private static void drawSprite(PoseStack poseStack, TextureAtlasSprite sprite, float halfSize,
            MultiBufferSource buffers) throws Exception {
        RenderType renderType = RenderType.entityTranslucentCull(sprite.atlasLocation());
        VertexConsumer buffer = buffers.getBuffer(renderType);
        Matrix4f matrix = poseStack.last().pose();

        float u0 = sprite.getU0(), u1 = sprite.getU1(), v0 = sprite.getV0(), v1 = sprite.getV1();
        buffer.addVertex(matrix, -halfSize, -halfSize, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u0, v0).setUv1(0, 10).setUv2(240, 240).setNormal(0.0F, 0.0F, 1.0F);
        buffer.addVertex(matrix, -halfSize, halfSize, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u0, v1).setUv1(0, 10).setUv2(240, 240).setNormal(0.0F, 0.0F, 1.0F);
        buffer.addVertex(matrix, halfSize, halfSize, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u1, v1).setUv1(0, 10).setUv2(240, 240).setNormal(0.0F, 0.0F, 1.0F);
        buffer.addVertex(matrix, halfSize, -halfSize, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u1, v0).setUv1(0, 10).setUv2(240, 240).setNormal(0.0F, 0.0F, 1.0F);
    }

    private static void drawColoredSquare(PoseStack poseStack, float halfSize, ElementType type,
            MultiBufferSource buffers) {
        int color = ElementDamageHandler.getDamageColor(type);
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(r, g, b, 1.0F);
        VertexConsumer buffer = buffers.getBuffer(RenderType.gui());
        Matrix4f matrix = poseStack.last().pose();
        buffer.addVertex(matrix, -halfSize, -halfSize, 0.0F).setColor(r, g, b, 1.0F);
        buffer.addVertex(matrix, -halfSize, halfSize, 0.0F).setColor(r, g, b, 1.0F);
        buffer.addVertex(matrix, halfSize, halfSize, 0.0F).setColor(r, g, b, 1.0F);
        buffer.addVertex(matrix, halfSize, -halfSize, 0.0F).setColor(r, g, b, 1.0F);
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawPercentageText(PoseStack poseStack, int points, float halfSize,
            Font font, MultiBufferSource buffers) {
        // Calculate percentage
        int percentage = THRESHOLD > 0 ? (points * 100) / THRESHOLD : 0;
        String text = percentage + "%";

        PoseStack textPose = new PoseStack();
        textPose.mulPose(poseStack.last().pose());
        textPose.scale(TEXT_SCALE, TEXT_SCALE, 1.0F);

        float tw = font.width(text) * TEXT_SCALE;
        float th = 9.0F * TEXT_SCALE;
        // Position text at bottom center of the icon
        font.drawInBatch(text, halfSize - tw, halfSize - th, 0xFFFFFFFF, false,
                textPose.last().pose(), buffers, DisplayMode.NORMAL, 0, 15728880);
    }
}
