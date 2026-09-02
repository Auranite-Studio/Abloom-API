package com.auranite.abloom.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.auranite.abloom.AbloomMod;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.network.chat.Style;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class TextParticle extends Particle {
   private String text;
   private boolean isPrismatic = false;
   private boolean isBreak = false;
   private int ticksAlive = 0;
   private int originalColor = 0xFFFFFF;
   private static final double BREAK_SHIMMER_SPEED = 0.3;
   private static final float BREAK_SHIMMER_INTENSITY = 0.6f;

   public TextParticle(ClientLevel world, Vec3 pos, Vec3 velocity) {
      super(world, pos.x, pos.y, pos.z, velocity.x, velocity.y, velocity.z);
      this.friction = 0.99F;
      this.gravity = 0.75F;
      this.lifetime = 32;
      this.rCol = 1.0F;
      this.gCol = 1.0F;
      this.bCol = 1.0F;
      this.alpha = 1.0F;
   }

   public void setText(@NotNull String text) {
      this.text = text;
   }

   public void setColor(@NotNull Color color) {
      this.rCol = color.r();
      this.gCol = color.g();
      this.bCol = color.b();
      this.alpha = color.a();
   }

   public void setPrismatic(boolean prismatic) {
      this.isPrismatic = prismatic;
   }

   public void setBreak(boolean isBreak) {
      this.isBreak = isBreak;
      if (isBreak) {
         this.originalColor = new Color(this.rCol, this.gCol, this.bCol, this.alpha).getValue();
      }
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.CUSTOM;
   }

   @Override
   public void tick() {
      super.tick();
      this.ticksAlive++;
   }

   public void render(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
      Vec3 cameraPos = camera.getPosition();
      float particleX = (float)(this.xo + (this.x - this.xo) * (double)tickDelta - cameraPos.x);
      float particleY = (float)(this.yo + (this.y - this.yo) * (double)tickDelta - cameraPos.y);
      float particleZ = (float)(this.zo + (this.z - this.zo) * (double)tickDelta - cameraPos.z);
      Matrix4f matrix = new Matrix4f();
      matrix = matrix.translation(particleX, particleY, particleZ);
      matrix = matrix.rotate(camera.rotation());
      matrix = matrix.rotate((float)Math.PI, 0.0F, 1.0F, 0.0F);
      matrix = matrix.scale(-0.025F, -0.025F, -0.025F);
      Minecraft client = Minecraft.getInstance();
      Font textRenderer = client.font;
      MultiBufferSource.BufferSource vertexConsumers = client.renderBuffers().bufferSource();
      float textX = (float)textRenderer.width(this.text) / -2.0F;
      float textY = 0.0F;
      int textColor;
      float finalAlpha = this.alpha;
      
      // Create bold component for rendering (bold without shadow)
      net.minecraft.network.chat.Component boldComponent = net.minecraft.network.chat.Component.literal(this.text).withStyle(Style.EMPTY.withBold(true));
      
      if (isPrismatic) {
         // Rainbow color effect (old: ticksAlive * 20)
         float hue = (ticksAlive * 20.0F) % 360.0F;
         int[] rgb = hsbToRgb((int)hue, 1.0F, 1.0F);
         textColor = (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
      } else if (isBreak && originalColor != 0) {
         // Shimmer effect for Break damage (old formula)
         double pulse = (Math.sin(ticksAlive * BREAK_SHIMMER_SPEED) + 1.0) / 2.0;
         int r = (originalColor >> 16) & 0xFF;
         int g = (originalColor >> 8) & 0xFF;
         int b = originalColor & 0xFF;
         int shimmerR = (int)(r + (255 - r) * pulse * BREAK_SHIMMER_INTENSITY);
         int shimmerG = (int)(g + (255 - g) * pulse * BREAK_SHIMMER_INTENSITY);
         int shimmerB = (int)(b + (255 - b) * pulse * BREAK_SHIMMER_INTENSITY);
         textColor = (shimmerR << 16) | (shimmerG << 8) | shimmerB;
      } else {
         textColor = (new Color(this.rCol, this.gCol, this.bCol, this.alpha)).getValue();
      }
      
      // Fade out at end (old: 70% of lifetime)
      int fadeStartTick = (int)(lifetime * 0.7);
      if (ticksAlive >= fadeStartTick) {
         int fadeTicks = lifetime - fadeStartTick;
         int currentFadeTick = ticksAlive - fadeStartTick;
         finalAlpha = 1.0f - ((float)currentFadeTick / (float)fadeTicks);
         finalAlpha = Math.max(0.0f, Math.min(1.0f, finalAlpha));
      }
      // Apply final alpha to color
      if (finalAlpha < 1.0f) {
         int a = (int)(finalAlpha * 255);
         textColor = (a << 24) | (textColor & 0x00FFFFFF);
      }
      
      try {
         textRenderer.drawInBatch(boldComponent, textX, textY, textColor, false, matrix, vertexConsumers, DisplayMode.NORMAL, 0, 15728880);
         vertexConsumers.endBatch();
      } catch (Exception e) {
         AbloomMod.LOGGER.warn("Failed to render text particle: {}", e.getMessage());
      }
   }

   /**
    * Converts HSB color to RGB
    */
   private static int[] hsbToRgb(int hue, float saturation, float brightness) {
      int h = hue % 360;
      float s = saturation;
      float b = brightness;

      float c = b * s;
      float x = c * (1 - Math.abs((h / 60f) % 2 - 1));
      float m = b - c;

      float r, g, bVal;

      if (h < 60) {
         r = c;
         g = x;
         bVal = 0;
      } else if (h < 120) {
         r = x;
         g = c;
         bVal = 0;
      } else if (h < 180) {
         r = 0;
         g = c;
         bVal = x;
      } else if (h < 240) {
         r = 0;
         g = x;
         bVal = c;
      } else if (h < 300) {
         r = x;
         g = 0;
         bVal = c;
      } else {
         r = c;
         g = 0;
         bVal = x;
      }

      int red = (int) ((r + m) * 255);
      int green = (int) ((g + m) * 255);
      int blue = (int) ((bVal + m) * 255);

      return new int[]{red, green, blue};
   }
}
