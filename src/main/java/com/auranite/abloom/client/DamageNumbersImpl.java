package com.auranite.abloom.client;

import com.auranite.abloom.config.AbloomConfig;
import com.auranite.abloom.handler.DamageNumbersHandler;
import com.auranite.abloom.util.ElementType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;

public class DamageNumbersImpl implements DamageNumbersHandler {
   private final Deque<TextParticle> particles = new ArrayDeque<>();
   private final Map<ElementType, Integer> damageColors = new EnumMap<>(ElementType.class);

   public DamageNumbersImpl() {
      initDefaultColors();
   }

   private void initDefaultColors() {
      damageColors.put(ElementType.FIRE, 0xFF5500);
      damageColors.put(ElementType.PHYSICAL, 0xC0C0C0);
      damageColors.put(ElementType.WIND, 0x00FFFF);
      damageColors.put(ElementType.WATER, 0x0080FF);
      damageColors.put(ElementType.EARTH, 0x8B4513);
      damageColors.put(ElementType.ICE, 0x00BFFF);
      damageColors.put(ElementType.ELECTRIC, 0xFF19FF);
      damageColors.put(ElementType.ENERGY, 0xFFFF00);
      damageColors.put(ElementType.NATURAL, 0x32CD32);
      damageColors.put(ElementType.QUANTUM, 0x9400D3);
      damageColors.put(ElementType.ETHER, 0x24B3A7);
      damageColors.put(ElementType.LIGHT, 0xFFF1A5);
      damageColors.put(ElementType.SHADOW, 0x4B0082);
      damageColors.put(ElementType.PRISMATIC, 0xFFFFFF);
   }

   @Override
   public void onEntityHealthChange(@NotNull LivingEntity entity, float oldHealth, float newHealth) {
      if (!AbloomConfig.areDamageNumbersEnabled()) {
         return;
      }
      
      float damage = oldHealth - newHealth;
      if (damage <= 0.0F) {
         return;
      }
      
      Minecraft client = Minecraft.getInstance();
      
      // Skip if entity is player and player damage is not shown
      if (entity == client.player && !AbloomConfig.areDamageNumbersEnabled()) {
         return;
      }
      
      ClientLevel world = client.level;
      if (world == null || world != entity.level()) {
         return;
      }
      
      // Check distance from config
      if (entity.distanceToSqr(client.player) > AbloomConfig.getDamageNumberSpawnRadiusSq()) {
         return;
      }
      
      // Get particle limit based on settings
      int particleLimit;
      switch ((ParticleStatus)client.options.particles().get()) {
         case ALL -> particleLimit = 256;
         case DECREASED -> particleLimit = 64;
         case MINIMAL -> particleLimit = 16;
         default -> particleLimit = 64;
      }
      
      // Remove old particles if limit exceeded
      while(this.particles.size() >= particleLimit) {
         TextParticle particle = this.particles.poll();
         if (particle != null) {
            particle.remove();
         }
      }
      
      // Calculate particle position and velocity
      Vec3 particlePos = entity.position().add(0.0F, entity.getBbHeight() + 0.25F, 0.0F);
      Vec3 particleVelocity = entity.getDeltaMovement();
      Vec3 cameraPos = client.gameRenderer.getMainCamera().getPosition();
      Vec3 forward = entity.position().subtract(cameraPos).normalize()
              .scale(entity.getBbWidth() * 10.0F);
      particleVelocity = particleVelocity.subtract(forward.x, -20.0F, forward.z);
      
      // Create particle
      TextParticle particle = new TextParticle(world, particlePos, particleVelocity);
      
      // Format damage text
      String text = String.format("%.1f", damage);
      if (text.endsWith(".0")) {
         text = text.substring(0, text.length() - 2);
      }
      particle.setText(text);
      
      // Set color based on damage amount
      if (damage >= 16.0F) {
         particle.setColor(new Color(1.0F, 0.0F, 0.0F, 1.0F));
      } else if (damage >= 8.0F) {
         Color md = new Color(1.0F, 0.0F, 0.0F, 1.0F);
         Color lg = new Color(1.0F, 0.0F, 0.0F, 1.0F);
         particle.setColor(Color.lerp(md, lg, (damage - 8.0F) / 8.0F));
      } else if (damage >= 2.0F) {
         Color sm = new Color(1.0F, 0.67F, 0.0F, 1.0F);
         Color md = new Color(1.0F, 0.0F, 0.0F, 1.0F);
         particle.setColor(Color.lerp(sm, md, (damage - 2.0F) / 6.0F));
      } else {
         particle.setColor(new Color(1.0F, 0.67F, 0.0F, 1.0F));
      }
      
      this.particles.add(particle);
      client.particleEngine.add(particle);
   }

   @Override
   public void spawnDamageNumber(int entityId, float damage, @Nullable ElementType elementType, int color, boolean isCrit, boolean hasBreak) {
      if (!AbloomConfig.areDamageNumbersEnabled()) {
         return;
      }
      
      Minecraft client = Minecraft.getInstance();
      ClientLevel world = client.level;
      if (world == null) {
         return;
      }
      
      Entity entity = world.getEntity(entityId);
      if (entity == null || !entity.isAlive()) {
         return;
      }
      
      // Check distance from config
      if (entity.distanceToSqr(client.player) > AbloomConfig.getDamageNumberSpawnRadiusSq()) {
         return;
      }
      
      // Get particle limit
      int particleLimit;
      switch ((ParticleStatus)client.options.particles().get()) {
         case ALL -> particleLimit = 256;
         case DECREASED -> particleLimit = 64;
         case MINIMAL -> particleLimit = 16;
         default -> particleLimit = 64;
      }
      
      // Remove old particles if limit exceeded
      while(this.particles.size() >= particleLimit) {
         TextParticle particle = this.particles.poll();
         if (particle != null) {
            particle.remove();
         }
      }
      
      // Calculate particle position and velocity
      Vec3 particlePos = entity.position().add(0.0F, entity.getBbHeight() + 0.25F, 0.0F);
      Vec3 particleVelocity = entity.getDeltaMovement();
      Vec3 cameraPos = client.gameRenderer.getMainCamera().getPosition();
      Vec3 forward = entity.position().subtract(cameraPos).normalize()
              .scale(entity.getBbWidth() * 10.0F);
      particleVelocity = particleVelocity.subtract(forward.x, -20.0F, forward.z);
      
      // Create particle
      TextParticle particle = new TextParticle(world, particlePos, particleVelocity);
      
      // Format damage text
      String text = String.format("%.1f", damage);
      if (text.endsWith(".0")) {
         text = text.substring(0, text.length() - 2);
      }
      if (isCrit) {
         text += "!!";
      }
      particle.setText(text);
      
      // Use color from packet (set by server based on element type)
      if (color == -1 || elementType == ElementType.PRISMATIC) {
         // Prismatic - rainbow effect handled in particle render
         particle.setColor(new Color(1.0F, 1.0F, 1.0F, 1.0F));
         particle.setPrismatic(true);
      } else {
         int r = (color >> 16) & 0xFF;
         int g = (color >> 8) & 0xFF;
         int b = color & 0xFF;
         particle.setColor(new Color(r / 255.0F, g / 255.0F, b / 255.0F, 1.0F));
      }
      
      // Set break shimmer from packet flag
      if (hasBreak) {
         particle.setBreak(true);
      }
      
      this.particles.add(particle);
      client.particleEngine.add(particle);
   }

   @Override
   public void spawnStatusText(int entityId, @NotNull Component textComponent, int color) {
      if (!AbloomConfig.areStatusTextsEnabled()) {
         return;
      }
      
      Minecraft client = Minecraft.getInstance();
      ClientLevel world = client.level;
      if (world == null) {
         return;
      }
      
      Entity entity = world.getEntity(entityId);
      if (entity == null || !entity.isAlive()) {
         return;
      }
      
      // Check distance from config
      if (entity.distanceToSqr(client.player) > AbloomConfig.getDamageNumberSpawnRadiusSq()) {
         return;
      }
      
      // Create status text particle
      StatusTextParticle particle = new StatusTextParticle(
              world,
              entity.position().add(0.0F, entity.getBbHeight() + 1.2F, 0.0F),
              entity.getDeltaMovement()
      );
      
      particle.setText(textComponent.getString());
      particle.setColor(new Color(
              ((color >> 16) & 0xFF) / 255.0F,
              ((color >> 8) & 0xFF) / 255.0F,
              (color & 0xFF) / 255.0F,
              1.0F
      ));
      particle.setStatusText(true);
      particle.setTargetEntityId(entityId);
      
      client.particleEngine.add(particle);
   }

   public int getDamageColor(@Nullable ElementType type) {
      if (type == null) return 0xFFFFFF;
      if (type == ElementType.PRISMATIC) return -1;
      return damageColors.getOrDefault(type, 0xFFFFFF);
   }

   public void setDamageColor(ElementType type, int color) {
      damageColors.put(type, color);
   }

   public Map<ElementType, Integer> getAllDamageColors() {
      return new EnumMap<>(damageColors);
   }

   /**
    * Particle for status texts with shimmer effect
    */
   public static class StatusTextParticle extends TextParticle {
      private boolean isStatusText = false;
      private int ticksAlive = 0;
      private double floatPhase = 0.0;
      private int originalColor = 0xFFFFFF;
      private int targetEntityId = -1;
      private double spawnX, spawnY, spawnZ;
      private static final double STATUS_FLOAT_SPEED = 0.15;
      private static final double STATUS_FLOAT_AMPLITUDE = 0.02;

      public StatusTextParticle(ClientLevel world, Vec3 pos, Vec3 velocity) {
         super(world, pos, velocity);
         this.lifetime = 50;
         this.friction = 1.0F;
         this.spawnX = pos.x;
         this.spawnY = pos.y;
         this.spawnZ = pos.z;
      }

      public void setStatusText(boolean status) {
         this.isStatusText = status;
         if (status) {
            this.originalColor = new Color(this.rCol, this.gCol, this.bCol, this.alpha).getValue();
         }
      }

      @Override
      public void tick() {
         // Don't call super.tick() - we control position manually
         // Update xo/yo/zo for smooth interpolation
         this.xo = this.x;
         this.yo = this.y;
         this.zo = this.z;
         this.ticksAlive++;
         
         if (this.ticksAlive >= this.lifetime) {
            this.remove();
            return;
         }
         
         if (this.isStatusText && this.level != null && this.targetEntityId >= 0) {
            this.floatPhase += STATUS_FLOAT_SPEED;
            
            // Follow the target entity
            Entity target = this.level.getEntity(this.targetEntityId);
            if (target != null && target.isAlive()) {
               double targetY = target.getY() + target.getBbHeight() + 1.2;
               double targetX = target.getX();
               double targetZ = target.getZ();
               
               // Smoothly interpolate towards target position
               double dx = targetX - this.x;
               double dy = targetY - this.y;
               double dz = targetZ - this.z;
               this.x += dx * 0.5;
               this.y += dy * 0.5;
               this.z += dz * 0.5;
               
               // Add gentle bobbing motion (old implementation)
               this.y += Math.sin(this.floatPhase) * STATUS_FLOAT_AMPLITUDE;
            }
         }
      }

      public void setTargetEntityId(int entityId) {
         this.targetEntityId = entityId;
      }

      @Override
      public void render(com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer, 
                         net.minecraft.client.Camera camera, float tickDelta) {
         if (this.isStatusText && this.originalColor != 0) {
            // Old formula: pulse = (Math.sin(floatPhase * 2) + 1) / 2
            float pulse = (float)(Math.sin(this.floatPhase * 2) + 1) / 2;
            int r = (this.originalColor >> 16) & 0xFF;
            int g = (this.originalColor >> 8) & 0xFF;
            int b = this.originalColor & 0xFF;
            // Old formula: shimmer = color + (255 - color) * pulse * 0.5
            int shimmerR = (int)(r + (255 - r) * pulse * 0.5);
            int shimmerG = (int)(g + (255 - g) * pulse * 0.5);
            int shimmerB = (int)(b + (255 - b) * pulse * 0.5);
            this.rCol = shimmerR / 255.0F;
            this.gCol = shimmerG / 255.0F;
            this.bCol = shimmerB / 255.0F;
         }
         super.render(vertexConsumer, camera, tickDelta);
      }
   }
}
