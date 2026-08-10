package com.yanghao.effect_display.utils;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import com.yanghao.effect_display.EffectDisplayConfig;
import com.yanghao.effect_display.network.ClientEntityEffectsStorage;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class EffectRenderUtil {
   private static final ResourceLocation HEALTH_BAR_TEXTURE = isNeatModLoaded() ? ResourceLocation.fromNamespaceAndPath("neat", "textures/ui/health_bar_texture.png") : null;
   private static final RenderType HEALTH_BAR_RENDER_TYPE = createHealthBarRenderType();

   private static RenderType createHealthBarRenderType() {
      if (HEALTH_BAR_TEXTURE == null) {
         return null;
      } else {
         RenderType.CompositeState renderTypeState = CompositeState.builder().setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER).setTextureState(new RenderStateShard.TextureStateShard(HEALTH_BAR_TEXTURE, false, false)).setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).setCullState(RenderStateShard.NO_CULL).setLightmapState(RenderStateShard.LIGHTMAP).createCompositeState(false);
         return RenderType.create("warframe_health_bar", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, Mode.QUADS, 256, true, false, renderTypeState);
      }
   }

   public static void renderHealthBar(Entity entity, PoseStack poseStack, MultiBufferSource buffers, Camera camera, EntityRenderer<? super Entity> entityRenderer, float partialTicks, double x, double y, double z) {
      if (isNeatModLoaded()) {
         if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            int var13 = 15728880;
            float var14 = 0.0267F;
            float var15 = 0.5F;
            boolean var16 = true;
            String name = living.getDisplayName().getString();
            float nameLen = (float)Minecraft.getInstance().font.width(name) * 0.5F;
            float halfSize = Math.max(20.0F, nameLen / 2.0F + 10.0F);
            Vec3 renderOffset = entityRenderer.getRenderOffset(entity, partialTicks);
            double d2 = x + renderOffset.x();
            double d3 = y + renderOffset.y();
            double d0 = z + renderOffset.z();
            poseStack.pushPose();
            poseStack.translate(d2, d3, d0);
            Vec3 attachmentPoint = entity.getAttachments().get(EntityAttachment.NAME_TAG, 0, entity.getViewYRot(partialTicks));
            poseStack.translate(attachmentPoint.x, attachmentPoint.y + (double)0.6F, attachmentPoint.z);
            poseStack.mulPose(new Quaternionf());
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.pushPose();
            poseStack.scale(-0.0267F, -0.0267F, 0.0267F);
            float padding = 2.0F;
            int black = 6;
            VertexConsumer builder = buffers.getBuffer(HEALTH_BAR_RENDER_TYPE);
            builder.addVertex(poseStack.last().pose(), -halfSize - padding, (float)(-black), 0.01F).setColor(0, 0, 0, 60).setUv(0.0F, 0.0F).setLight(15728880);
            builder.addVertex(poseStack.last().pose(), -halfSize - padding, 4.0F + padding, 0.01F).setColor(0, 0, 0, 60).setUv(0.0F, 0.5F).setLight(15728880);
            builder.addVertex(poseStack.last().pose(), halfSize + padding, 4.0F + padding, 0.01F).setColor(0, 0, 0, 60).setUv(1.0F, 0.5F).setLight(15728880);
            builder.addVertex(poseStack.last().pose(), halfSize + padding, (float)(-black), 0.01F).setColor(0, 0, 0, 60).setUv(1.0F, 0.0F).setLight(15728880);
            int argb = getHealthColor(living);
            black = argb >> 16 & 255;
            int g = argb >> 8 & 255;
            int h = argb & 255;
            float maxHealth = Math.max(living.getHealth(), living.getMaxHealth());
            float healthHalfSize = halfSize * (living.getHealth() / maxHealth);
            VertexConsumer builder = buffers.getBuffer(HEALTH_BAR_RENDER_TYPE);
            builder.addVertex(poseStack.last().pose(), -halfSize, 0.0F, 0.001F).setColor(black, g, h, 127).setUv(0.0F, 0.75F).setLight(15728880);
            builder.addVertex(poseStack.last().pose(), -halfSize, 4.0F, 0.001F).setColor(black, g, h, 127).setUv(0.0F, 1.0F).setLight(15728880);
            builder.addVertex(poseStack.last().pose(), -halfSize + 2.0F * healthHalfSize, 4.0F, 0.001F).setColor(black, g, h, 127).setUv(1.0F, 1.0F).setLight(15728880);
            builder.addVertex(poseStack.last().pose(), -halfSize + 2.0F * healthHalfSize, 0.0F, 0.001F).setColor(black, g, h, 127).setUv(1.0F, 0.75F).setLight(15728880);
            if (healthHalfSize < halfSize) {
               builder.addVertex(poseStack.last().pose(), -halfSize + 2.0F * healthHalfSize, 0.0F, 0.001F).setColor(0, 0, 0, 127).setUv(0.0F, 0.5F).setLight(15728880);
               builder.addVertex(poseStack.last().pose(), -halfSize + 2.0F * healthHalfSize, 4.0F, 0.001F).setColor(0, 0, 0, 127).setUv(0.0F, 0.75F).setLight(15728880);
               builder.addVertex(poseStack.last().pose(), halfSize, 4.0F, 0.001F).setColor(0, 0, 0, 127).setUv(1.0F, 0.75F).setLight(15728880);
               builder.addVertex(poseStack.last().pose(), halfSize, 0.0F, 0.001F).setColor(0, 0, 0, 127).setUv(1.0F, 0.5F).setLight(15728880);
            }

            argb = 16777215;
            black = 0;
            poseStack.pushPose();
            poseStack.translate(-halfSize, -4.5F, 0.0F);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            Minecraft.getInstance().font.drawInBatch(name, 0.0F, 0.0F, 16777215, false, poseStack.last().pose(), buffers, DisplayMode.NORMAL, 0, 15728880);
            poseStack.popPose();
            float healthValueTextScale = 0.375F;
            poseStack.pushPose();
            poseStack.translate(-halfSize, -4.5F, 0.0F);
            poseStack.scale(0.375F, 0.375F, 0.375F);
            h = 14;
            DecimalFormat health_format = new DecimalFormat("#.##");
            String hpStr = health_format.format((double)living.getHealth());
            Minecraft.getInstance().font.drawInBatch(hpStr, 2.0F, (float)h, 16777215, false, poseStack.last().pose(), buffers, DisplayMode.NORMAL, 0, 15728880);
            String maxHpStr = health_format.format((double)living.getMaxHealth());
            Minecraft.getInstance().font.drawInBatch(maxHpStr, (float)((int)(halfSize / 0.375F * 2.0F) - Minecraft.getInstance().font.width(maxHpStr) - 2), (float)h, 16777215, false, poseStack.last().pose(), buffers, DisplayMode.NORMAL, 0, 15728880);
            int var10000 = (int)(100.0F * living.getHealth() / living.getMaxHealth());
            String percStr = var10000 + "%";
            Minecraft.getInstance().font.drawInBatch(percStr, (float)((int)(halfSize / 0.375F)) - (float)Minecraft.getInstance().font.width(percStr) / 2.0F, (float)h, 16777215, false, poseStack.last().pose(), buffers, DisplayMode.NORMAL, 0, 15728880);
            poseStack.popPose();
            poseStack.popPose();
         }
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
      poseStack.translate((double)0.0F, (double)getVerticalOffset(entity), (double)0.0F);
      if (isGuiEnvironment) {
         poseStack.mulPose(new Quaternionf());
      } else {
         poseStack.mulPose(camera.rotation());
      }

      poseStack.translate(EffectDisplayConfig.getHorizontalOffset(), (double)0.0F, (double)0.0F);
      poseStack.mulPose(new Quaternionf(new AxisAngle4f((float)Math.toRadians((double)180.0F), 0.0F, 1.0F, 0.0F)));
      poseStack.scale(-0.0267F, -0.0267F, 0.0267F);
      Minecraft minecraft = Minecraft.getInstance();
      Font font = minecraft.font;
      float iconWidth = 10.0F * (float)EffectDisplayConfig.getRenderScale();
      float iconHeight = 10.0F * (float)EffectDisplayConfig.getRenderScale();
      float iconSpacing = 2.0F;
      float entityWidthInBlocks = entity.getBbWidth();
      float entityWidthInRenderUnits = entityWidthInBlocks / 0.0267F;
      float maxAllowedWidth = entityWidthInRenderUnits * 3.0F;
      int maxIconsPerRow = (int)Math.floor((double)((maxAllowedWidth + iconSpacing) / (iconWidth + iconSpacing)));
      if (maxIconsPerRow < 1) {
         maxIconsPerRow = 1;
      }

      int totalRows = (int)Math.ceil((double)effects.size() / (double)maxIconsPerRow);
      float rowSpacing = iconHeight + 2.0F;

      for(int row = 0; row < totalRows; ++row) {
         int startIndex = row * maxIconsPerRow;
         int endIndex = Math.min(startIndex + maxIconsPerRow, effects.size());
         int iconsInCurrentRow = endIndex - startIndex;
         float currentRowWidth = (float)iconsInCurrentRow * iconWidth + (float)(iconsInCurrentRow - 1) * iconSpacing;
         float startX = -currentRowWidth / 2.0F + iconWidth / 2.0F;
         float currentRowY = (float)(-row) * rowSpacing;

         for(int i = startIndex; i < endIndex; ++i) {
            MobEffectInstance effectInstance = (MobEffectInstance)effects.get(i);
            Holder<MobEffect> effectHolder = effectInstance.getEffect();
            TextureAtlasSprite sprite = minecraft.getMobEffectTextures().get(effectHolder);
            float halfWidth = iconWidth / 2.0F;
            float halfHeight = iconHeight / 2.0F;
            float iconAlpha = 1.0F;
            if ((Boolean)EffectDisplayConfig.BLINK_ON_LOW_DURATION.get()) {
               int realDuration = ClientEntityEffectsStorage.getRemainingTicks(entity.getId(), effectHolder, effectInstance.getDuration());
               if (realDuration > 0 && realDuration <= 200) {
                  float minAlpha = 0.2F;
                  iconAlpha = minAlpha + (1.0F - minAlpha) * (0.5F + 0.5F * (float)Math.sin((double)System.currentTimeMillis() / (double)150.0F));
               }
            }

            PoseStack iconPoseStack = new PoseStack();
            iconPoseStack.mulPose(poseStack.last().pose());
            int iconIndexInRow = i - startIndex;
            float iconX = startX + (float)iconIndexInRow * (iconWidth + iconSpacing);
            iconPoseStack.translate(iconX, currentRowY, 0.0F);
            float backgroundOpacity = 0.25F;
            font.drawInBatch("", -halfWidth, -halfHeight, (int)(backgroundOpacity * 255.0F) << 24, false, iconPoseStack.last().pose(), buffers, DisplayMode.SEE_THROUGH, 0, 15728880);

            try {
               RenderType.CompositeState state = CompositeState.builder().setShaderState(RenderStateShard.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_SHADER).setTextureState(new RenderStateShard.TextureStateShard(sprite.atlasLocation(), false, false)).setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).setOutputState(RenderStateShard.ITEM_ENTITY_TARGET).setLightmapState(RenderStateShard.LIGHTMAP).setOverlayState(RenderStateShard.OVERLAY).setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE).setDepthTestState(RenderStateShard.NO_DEPTH_TEST).createCompositeState(false);
               RenderType.CompositeRenderType renderType = RenderType.create("buffered_effect_icon", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 1536, state);
               VertexConsumer buffer = buffers.getBuffer(renderType);
               Matrix4f iconMatrix = iconPoseStack.last().pose();
               buffer.addVertex(iconMatrix, -halfWidth, -halfHeight, 0.0F).setColor(1.0F, 1.0F, 1.0F, iconAlpha).setUv(sprite.getU0(), sprite.getV0()).setUv1(0, 10).setUv2(240, 240).setNormal(0.0F, 0.0F, 1.0F);
               buffer.addVertex(iconMatrix, -halfWidth, halfHeight, 0.0F).setColor(1.0F, 1.0F, 1.0F, iconAlpha).setUv(sprite.getU0(), sprite.getV1()).setUv1(0, 10).setUv2(240, 240).setNormal(0.0F, 0.0F, 1.0F);
               buffer.addVertex(iconMatrix, halfWidth, halfHeight, 0.0F).setColor(1.0F, 1.0F, 1.0F, iconAlpha).setUv(sprite.getU1(), sprite.getV1()).setUv1(0, 10).setUv2(240, 240).setNormal(0.0F, 0.0F, 1.0F);
               buffer.addVertex(iconMatrix, halfWidth, -halfHeight, 0.0F).setColor(1.0F, 1.0F, 1.0F, iconAlpha).setUv(sprite.getU1(), sprite.getV0()).setUv1(0, 10).setUv2(240, 240).setNormal(0.0F, 0.0F, 1.0F);
            } catch (Exception var58) {
               String text = effectInstance.getDescriptionId();
               float scaledTextWidth = (float)font.width(text);
               float textHalfSize = scaledTextWidth / 2.0F;
               font.drawInBatch(text, -textHalfSize, 0.0F, (int)(backgroundOpacity * 255.0F) << 24, false, iconPoseStack.last().pose(), buffers, DisplayMode.SEE_THROUGH, 0, 15728880);
               font.drawInBatch(text, -textHalfSize, 0.0F, 16711680, false, iconPoseStack.last().pose(), buffers, DisplayMode.SEE_THROUGH, 0, 15728880);
            }

            int var10000 = effectInstance.getAmplifier();
            String levelText = "" + (var10000 + 1);
            iconPoseStack.scale(0.5F, 0.5F, 1.0F);
            float scaledLevelWidth = (float)font.width(levelText) * 0.5F;
            Objects.requireNonNull(font);
            float scaledLevelHeight = 9.0F * 0.5F;
            float textX = halfWidth - scaledLevelWidth;
            float textY = halfHeight - scaledLevelHeight;
            int textColor = -1;
            font.drawInBatch(levelText, textX, textY, textColor, false, iconPoseStack.last().pose(), buffers, DisplayMode.NORMAL, 0, 15728880);
         }
      }

      poseStack.popPose();
   }

   public static float getVerticalOffset(Entity entity) {
      return isNeatModLoaded() ? entity.getBbHeight() + 0.92F + (float)EffectDisplayConfig.getVerticalOffset() : entity.getBbHeight() + 0.6F + (float)EffectDisplayConfig.getVerticalOffset();
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

   private static int getHealthColor(LivingEntity entity) {
      float health = Mth.clamp(entity.getHealth(), 0.0F, entity.getMaxHealth());
      float hue = Math.max(0.0F, health / entity.getMaxHealth() / 3.0F - 0.07F);
      return Mth.hsvToRgb(hue, 1.0F, 1.0F);
   }
}
