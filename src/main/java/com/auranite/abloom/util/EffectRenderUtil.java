package com.auranite.abloom.util;

import com.auranite.abloom.config.AbloomConfig;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.auranite.abloom.network.ClientEntityEffectsStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private static final float TEXT_SCALE = 0.5F;
    private static final int WARNING_THRESHOLD_SECONDS = 5;
    private static final int THRESHOLD = 100;

    public static void renderAllMobEffects(Entity entity, PoseStack poseStack, MultiBufferSource buffers, Camera camera, EntityRenderer<? super Entity> entityRenderer, float partialTicks, double x, double y, double z, List<MobEffectInstance> effects, boolean isGuiEnvironment) {
        renderAllMobEffects(entity, poseStack, buffers, camera, entityRenderer, partialTicks, x, y, z, effects, isGuiEnvironment, null);
    }

    public static void renderAllMobEffects(Entity entity, PoseStack poseStack, MultiBufferSource buffers, Camera camera, EntityRenderer<? super Entity> entityRenderer, float partialTicks, double x, double y, double z, List<MobEffectInstance> effects, boolean isGuiEnvironment, Map<ElementType, Integer> resonancePoints) {
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

        // Build map: ElementType -> icon data (effect or points)
        Map<ElementType, IconData> iconMap = buildIconMap(effects, resonancePoints);

        List<ElementType> sortedTypes = new ArrayList<>(iconMap.keySet());
        int totalRows = (int) Math.ceil((double) sortedTypes.size() / maxIconsPerRow);
        for (int row = 0; row < totalRows; ++row) {
            renderRow(sortedTypes, row, maxIconsPerRow, iconWidth, iconHeight, entityId, font, buffers, minecraft, poseStack, iconMap, resonancePoints);
        }

        poseStack.popPose();
    }

    private static Map<ElementType, IconData> buildIconMap(List<MobEffectInstance> effects, Map<ElementType, Integer> resonancePoints) {
        java.util.Map<ElementType, IconData> map = new java.util.LinkedHashMap<>();

        // Active effects
        for (MobEffectInstance effect : effects) {
            ElementType type = getElementTypeForEffect(effect.getEffect());
            if (type != null && isDisplayEffect(effect.getEffect())) {
                map.put(type, new IconData(effect, null));
            }
        }

        // Resonance only (no active effect)
        if (resonancePoints != null && !resonancePoints.isEmpty()) {
            for (ElementType type : ElementType.values()) {
                if (!map.containsKey(type)) {
                    int points = resonancePoints.getOrDefault(type, 0);
                    if (points > 0) {
                        map.put(type, new IconData(null, points));
                    }
                }
            }
        }

        return map;
    }

    private static void renderRow(List<ElementType> types, int row, int maxIconsPerRow, float iconWidth, float iconHeight, int entityId, Font font, MultiBufferSource buffers, Minecraft minecraft, PoseStack basePoseStack, Map<ElementType, IconData> iconMap, Map<ElementType, Integer> resonancePoints) {
        int startIndex = row * maxIconsPerRow;
        int endIndex = Math.min(startIndex + maxIconsPerRow, types.size());

        float rowWidth = (endIndex - startIndex) * iconWidth + Math.max(0, (endIndex - startIndex - 1)) * ICON_SPACING;
        float startX = -rowWidth / 2.0F + iconWidth / 2.0F;
        float rowY = -(float) row * (iconHeight + ICON_SPACING);

        for (int i = startIndex; i < endIndex; i++) {
            ElementType type = types.get(i);
            IconData data = iconMap.get(type);
            float halfSize = iconWidth / 2.0F;
            float iconX = startX + (i - startIndex) * (iconWidth + ICON_SPACING);

            PoseStack iconPose = new PoseStack();
            iconPose.mulPose(basePoseStack.last().pose());
            iconPose.translate(iconX, rowY, 0.0F);

            drawBackground(iconPose, font, buffers);
            drawElementIcon(iconPose, type, halfSize, buffers, minecraft);

            if (data.effect != null) {
                // Active effect: percentage above, duration below
                int points = resonancePoints != null ? resonancePoints.getOrDefault(type, 0) : 0;
                drawResonanceAbove(iconPose, halfSize, points, font, buffers);
                drawDurationBelow(iconPose, halfSize, entityId, data.effect, font, buffers);
            } else {
                // No effect: percentage below
                drawResonanceBelow(iconPose, halfSize, data.points, font, buffers);
            }
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

    private static void drawResonanceAbove(PoseStack poseStack, float halfSize, int points, Font font, MultiBufferSource buffers) {
        if (points <= 0) return;
        int percentage = THRESHOLD > 0 ? (points * 100) / THRESHOLD : 0;
        String text = percentage + "";

        PoseStack textPose = new PoseStack();
        textPose.mulPose(poseStack.last().pose());
        textPose.scale(TEXT_SCALE, TEXT_SCALE, 1.0F);

        float tw = font.width(text) * TEXT_SCALE;
        float th = 9.0F * TEXT_SCALE;
        float offsetY = -(halfSize + th);
        float offsetX = halfSize - tw + 2.0F;
        font.drawInBatch(text, offsetX, offsetY, 0xFFFFFFFF, false, textPose.last().pose(), buffers, DisplayMode.NORMAL, 0, 15728880);
    }

    private static void drawDurationBelow(PoseStack poseStack, float halfSize, int entityId, MobEffectInstance effectInstance, Font font, MultiBufferSource buffers) {
        int realDuration = ClientEntityEffectsStorage.getRemainingTicks(
                entityId,
                effectInstance.getEffect(),
                effectInstance.getDuration());
        int seconds = realDuration > 0 ? (realDuration + 9) / 20 : 0;
        String text = seconds > 0 ? seconds + "s" : "--";
        int color = seconds > WARNING_THRESHOLD_SECONDS ? -1 : 0xFF0000;

        PoseStack textPose = new PoseStack();
        textPose.mulPose(poseStack.last().pose());
        textPose.scale(TEXT_SCALE, TEXT_SCALE, 1.0F);

        float tw = font.width(text) * TEXT_SCALE;
        float th = 9.0F * TEXT_SCALE;
        font.drawInBatch(text, halfSize - tw + 2.0F, halfSize - th, color, false, textPose.last().pose(), buffers, DisplayMode.NORMAL, 0, 15728880);
    }

    private static void drawElementIcon(PoseStack poseStack, ElementType type, float halfSize, MultiBufferSource buffers, Minecraft minecraft) {
        Holder<MobEffect> effectHolder = getEffectHolderForElementType(type);
        if (effectHolder != null) {
            TextureAtlasSprite sprite = minecraft.getMobEffectTextures().get(effectHolder);
            if (sprite != null) {
                try {
                    drawSprite(poseStack, sprite, halfSize, buffers);
                    return;
                } catch (Exception e) {
                    // Fall through to colored square
                }
            }
        }
        int color = com.auranite.abloom.handler.ElementDamageHandler.getDamageColor(type);
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

    private static void drawResonanceBelow(PoseStack poseStack, float halfSize, int points, Font font, MultiBufferSource buffers) {
        if (points <= 0) return;
        int percentage = THRESHOLD > 0 ? (points * 100) / THRESHOLD : 0;
        String text = percentage + "";

        PoseStack textPose = new PoseStack();
        textPose.mulPose(poseStack.last().pose());
        textPose.scale(TEXT_SCALE, TEXT_SCALE, 1.0F);

        float tw = font.width(text) * TEXT_SCALE;
        float th = 9.0F * TEXT_SCALE;
        font.drawInBatch(text, halfSize - tw + 2.0F, halfSize - th, 0xFFFFFFFF, false, textPose.last().pose(), buffers, DisplayMode.NORMAL, 0, 15728880);
    }

    private static ElementType getElementTypeForEffect(Holder<MobEffect> effectHolder) {
        String effectName = effectHolder.unwrap().left()
                .map(key -> key.location().getPath())
                .orElse("");

        return switch (effectName) {
            case "burn" -> ElementType.FIRE;
            case "freeze" -> ElementType.ICE;
            case "shock" -> ElementType.ELECTRIC;
            case "bloom" -> ElementType.NATURAL;
            case "overload" -> ElementType.ENERGY;
            case "wetness" -> ElementType.WATER;
            case "stun" -> ElementType.EARTH;
            case "rupture" -> ElementType.PHYSICAL;
            case "break" -> ElementType.QUANTUM;
            case "windswept" -> ElementType.WIND;
            case "corruption" -> ElementType.ETHER;
            case "dispersion" -> ElementType.LIGHT;
            case "eclipse" -> ElementType.SHADOW;
            default -> null;
        };
    }

    private static Holder<MobEffect> getEffectHolderForElementType(ElementType type) {
        net.minecraft.world.effect.MobEffect effect = switch (type) {
            case FIRE -> com.auranite.abloom.init.AbloomModEffects.BURN.value();
            case ICE -> com.auranite.abloom.init.AbloomModEffects.FREEZE.value();
            case ELECTRIC -> com.auranite.abloom.init.AbloomModEffects.SHOCK.value();
            case NATURAL -> com.auranite.abloom.init.AbloomModEffects.BLOOM.value();
            case ENERGY -> com.auranite.abloom.init.AbloomModEffects.OVERLOAD.value();
            case WATER -> com.auranite.abloom.init.AbloomModEffects.WETNESS.value();
            case EARTH -> com.auranite.abloom.init.AbloomModEffects.STUN.value();
            case QUANTUM -> com.auranite.abloom.init.AbloomModEffects.BREAK.value();
            case ETHER -> com.auranite.abloom.init.AbloomModEffects.CORRUPTION.value();
            case LIGHT -> com.auranite.abloom.init.AbloomModEffects.DISPERSION.value();
            case SHADOW -> com.auranite.abloom.init.AbloomModEffects.ECLIPSE.value();
            case WIND -> com.auranite.abloom.init.AbloomModEffects.WINDSWEPT.value();
            case PHYSICAL -> com.auranite.abloom.init.AbloomModEffects.RUPTURE.value();
            case PRISMATIC -> null;
        };
        if (effect == null) return null;
        return net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);
    }

    public static float getVerticalOffset(Entity entity, PoseStack poseStack) {
        return entity.getBbHeight() + 0.6F + (float) AbloomConfig.CLIENT_CONFIG.getVerticalOffset();
    }

    private static boolean isDisplayEffect(Holder<MobEffect> effectHolder) {
        return effectHolder.unwrap().left()
                .map(key -> AbloomConfig.DISPLAY_EFFECTS.contains(key.location().getPath()))
                .orElse(false);
    }

    private static class IconData {
        final MobEffectInstance effect;
        final int points;

        IconData(MobEffectInstance effect, Integer points) {
            this.effect = effect;
            this.points = points != null ? points : 0;
        }
    }
}
