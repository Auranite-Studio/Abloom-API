package com.auranite.abloom.util;

import com.auranite.abloom.config.EffectDisplayConfig;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.auranite.abloom.network.ClientEntityEffectsStorage;

import java.util.List;
import java.util.Objects;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class EffectRenderUtil {
    private static final ResourceLocation HEALTH_BAR_TEXTURE = isNeatModLoaded() ? ResourceLocation.parse("neat:textures/ui/health_bar_texture.png") : null;
    private static final RenderType HEALTH_BAR_RENDER_TYPE = createHealthBarRenderType();

    private static RenderType createHealthBarRenderType() {
        if (HEALTH_BAR_TEXTURE == null) {
            return null;
        } else {
            return RenderType.entityTranslucent(HEALTH_BAR_TEXTURE);
        }
    }

    public static void renderAllMobEffects(Entity entity, PoseStack poseStack, MultiBufferSource buffers, Camera camera, EntityRenderer<? super Entity> entityRenderer, float partialTicks, double x, double y, double z, List<MobEffectInstance> effects, boolean isGuiEnvironment) {
        int light = 15728880;
        float globalScale = 0.0267F;
        Vec3 renderOffset = entityRenderer.getRenderOffset(entity, partialTicks);
        double d2 = x + renderOffset.x();
        double d3 = y + renderOffset.y();
        double d0 = z + renderOffset.z();
        poseStack.pushPose();
        poseStack.translate(d2, d3, d0);
        poseStack.translate((double) 0.0F, (double) getVerticalOffset(entity), (double) 0.0F);
        if (isGuiEnvironment) {
            poseStack.mulPose(new Quaternionf());
        } else {
            poseStack.mulPose(camera.rotation());
        }

        poseStack.translate(EffectDisplayConfig.getHorizontalOffset(), (double) 0.0F, (double) 0.0F);
        poseStack.mulPose(new Quaternionf(new AxisAngle4f((float) Math.toRadians((double) 180.0F), 0.0F, 1.0F, 0.0F)));
        poseStack.scale(-0.0267F, -0.0267F, 0.0267F);
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        float iconWidth = 10.0F * (float) EffectDisplayConfig.getRenderScale();
        float iconHeight = 10.0F * (float) EffectDisplayConfig.getRenderScale();
        float iconSpacing = 2.0F;
        float entityWidthInBlocks = entity.getBbWidth();
        float entityWidthInRenderUnits = entityWidthInBlocks / 0.0267F;
        float maxAllowedWidth = entityWidthInRenderUnits * 3.0F;
        int maxIconsPerRow = (int) Math.floor((double) ((maxAllowedWidth + iconSpacing) / (iconWidth + iconSpacing)));
        if (maxIconsPerRow < 1) {
            maxIconsPerRow = 1;
        }

        int totalRows = (int) Math.ceil((double) effects.size() / (double) maxIconsPerRow);
        float rowSpacing = iconHeight + 2.0F;

        for (int row = 0; row < totalRows; ++row) {
            int startIndex = row * maxIconsPerRow;
            int endIndex = Math.min(startIndex + maxIconsPerRow, effects.size());
            int iconsInCurrentRow = endIndex - startIndex;
            float currentRowWidth = (float) iconsInCurrentRow * iconWidth + (float) (iconsInCurrentRow - 1) * iconSpacing;
            float startX = -currentRowWidth / 2.0F + iconWidth / 2.0F;
            float currentRowY = (float) (-row) * rowSpacing;

            for (int i = startIndex; i < endIndex; ++i) {
                MobEffectInstance effectInstance = effects.get(i);
                Holder<MobEffect> effectHolder = effectInstance.getEffect();

                if (!isDisplayEffect(effectHolder)) {
                    continue;
                }

                // Get the effect's resource location to fetch the texture
                ResourceLocation effectTextureLocation = effectHolder.unwrapKey()
                    .map(key -> key.location())
                    .orElse(null);
                if (effectTextureLocation == null) continue;
                
                TextureAtlasSprite sprite = minecraft.getTextureAtlas().getSprite(effectTextureLocation);
                if (sprite == null) continue;
                float halfWidth = iconWidth / 2.0F;
                float halfHeight = iconHeight / 2.0F;
                float iconAlpha = 1.0F;

                PoseStack iconPoseStack = new PoseStack();
                iconPoseStack.mulPose(poseStack.last().pose());
                int iconIndexInRow = i - startIndex;
                float iconX = startX + (float) iconIndexInRow * (iconWidth + iconSpacing);
                iconPoseStack.translate(iconX, currentRowY, 0.0F);
                float backgroundOpacity = 0.25F;
                font.drawInBatch("", -halfWidth, -halfHeight, (int) (backgroundOpacity * 255.0F) << 24, false, iconPoseStack.last().pose(), buffers, DisplayMode.SEE_THROUGH, 0, 15728880);

                try {
                    RenderType renderType = RenderType.itemEntityTranslucentCull(sprite.atlasLocation());
                    VertexConsumer buffer = buffers.getBuffer(renderType);
                    Matrix4f iconMatrix = iconPoseStack.last().pose();
                    buffer.addVertex(iconMatrix, -halfWidth, -halfHeight, 0.0F).setColor(1.0F, 1.0F, 1.0F, iconAlpha).setUv(sprite.getU0(), sprite.getV0()).setUv1(0, 10).setUv2(240, 240).setNormal(0.0F, 0.0F, 1.0F);
                    buffer.addVertex(iconMatrix, -halfWidth, halfHeight, 0.0F).setColor(1.0F, 1.0F, 1.0F, iconAlpha).setUv(sprite.getU0(), sprite.getV1()).setUv1(0, 10).setUv2(240, 240).setNormal(0.0F, 0.0F, 1.0F);
                    buffer.addVertex(iconMatrix, halfWidth, halfHeight, 0.0F).setColor(1.0F, 1.0F, 1.0F, iconAlpha).setUv(sprite.getU1(), sprite.getV1()).setUv1(0, 10).setUv2(240, 240).setNormal(0.0F, 0.0F, 1.0F);
                    buffer.addVertex(iconMatrix, halfWidth, -halfHeight, 0.0F).setColor(1.0F, 1.0F, 1.0F, iconAlpha).setUv(sprite.getU1(), sprite.getV0()).setUv1(0, 10).setUv2(240, 240).setNormal(0.0F, 0.0F, 1.0F);
                } catch (Exception var58) {
                    String text = effectInstance.getDescriptionId();
                    float scaledTextWidth = (float) font.width(text);
                    float textHalfSize = scaledTextWidth / 2.0F;
                    font.drawInBatch(text, -textHalfSize, 0.0F, (int) (backgroundOpacity * 255.0F) << 24, false, iconPoseStack.last().pose(), buffers, DisplayMode.SEE_THROUGH, 0, 15728880);
                    font.drawInBatch(text, -textHalfSize, 0.0F, 16711680, false, iconPoseStack.last().pose(), buffers, DisplayMode.SEE_THROUGH, 0, 15728880);
                }

                int realDuration = ClientEntityEffectsStorage.getRemainingTicks(entity.getId(), effectHolder, effectInstance.getDuration());
                int seconds = realDuration > 0 ? (realDuration + 9) / 20 : 0;
                String durationText = seconds > 0 ? seconds + "s" : "--";
                iconPoseStack.scale(0.5F, 0.5F, 1.0F);
                float scaledTextWidth = (float) font.width(durationText) * 0.5F;
                Objects.requireNonNull(font);
                float scaledTextHeight = 9.0F * 0.5F;
                float textX = halfWidth - scaledTextWidth;
                float textY = halfHeight - scaledTextHeight;
                int textColor = seconds > 5 ? -1 : 0xFF0000;
                font.drawInBatch(durationText, textX, textY, textColor, false, iconPoseStack.last().pose(), buffers, DisplayMode.NORMAL, 0, 15728880);
            }
        }

        poseStack.popPose();
    }

    public static float getVerticalOffset(Entity entity) {
        return isNeatModLoaded() ? entity.getBbHeight() + 0.92F + (float) EffectDisplayConfig.getVerticalOffset() : entity.getBbHeight() + 0.6F + (float) EffectDisplayConfig.getVerticalOffset();
    }

    public static boolean isNeatModLoaded() {
        try {
            return ModList.get() != null ? ModList.get().isLoaded("neat") : true;
        } catch (Exception var1) {
            return false;
        }
    }

    public static boolean isWarframeModLoaded() {
        try {
            return ModList.get() != null ? ModList.get().isLoaded("warframe") : true;
        } catch (Exception var1) {
            return false;
        }
    }

    private static boolean isDisplayEffect(Holder<MobEffect> effectHolder) {
        var either = effectHolder.unwrap();
        var key = either.left().orElse(null);
        if (key == null) return false;
        return EffectDisplayConfig.DISPLAY_EFFECTS.contains(key.location().getPath());
    }
}
