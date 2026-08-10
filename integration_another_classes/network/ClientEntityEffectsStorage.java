package com.yanghao.effect_display.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public class ClientEntityEffectsStorage {
   private static final Map<Integer, List<MobEffectInstance>> entityEffects = new HashMap();
   private static final Map<Integer, Map<Holder<MobEffect>, Long>> effectTimestamps = new HashMap();

   public static void updateEntityEffects(int entityId, List<MobEffectInstance> newEffects) {
      List<MobEffectInstance> currentEffects = (List)entityEffects.get(entityId);
      long now = getCurrentGameTick();
      Map<Holder<MobEffect>, Long> timestamps = (Map)effectTimestamps.computeIfAbsent(entityId, (k) -> new HashMap());
      if (currentEffects != null && !currentEffects.isEmpty()) {
         List<MobEffectInstance> mergedEffects = new ArrayList(currentEffects);

         for(MobEffectInstance newEffect : newEffects) {
            boolean effectExists = false;

            for(int i = 0; i < mergedEffects.size(); ++i) {
               MobEffectInstance existingEffect = (MobEffectInstance)mergedEffects.get(i);
               if (existingEffect.getEffect().equals(newEffect.getEffect())) {
                  mergedEffects.set(i, newEffect);
                  effectExists = true;
                  break;
               }
            }

            if (!effectExists) {
               mergedEffects.add(newEffect);
            }

            timestamps.put(newEffect.getEffect(), now);
         }

         mergedEffects.removeIf((existingEffectx) -> newEffects.stream().noneMatch((newEffect) -> newEffect.getEffect().equals(existingEffectx.getEffect())));
         timestamps.keySet().removeIf((holder) -> mergedEffects.stream().noneMatch((e) -> e.getEffect().equals(holder)));
         entityEffects.put(entityId, mergedEffects);
      } else {
         entityEffects.put(entityId, new ArrayList(newEffects));

         for(MobEffectInstance effect : newEffects) {
            timestamps.put(effect.getEffect(), now);
         }

      }
   }

   private static long getCurrentGameTick() {
      Minecraft mc = Minecraft.getInstance();
      return mc.level != null ? mc.level.getGameTime() : System.currentTimeMillis() / 50L;
   }

   public static int getRemainingTicks(int entityId, Holder<MobEffect> effectHolder, int snapshotDuration) {
      Map<Holder<MobEffect>, Long> timestamps = (Map)effectTimestamps.get(entityId);
      if (timestamps == null) {
         return -1;
      } else {
         Long receivedTick = (Long)timestamps.get(effectHolder);
         if (receivedTick == null) {
            return -1;
         } else {
            long currentTick = getCurrentGameTick();
            long elapsedTicks = currentTick - receivedTick;
            int remaining = snapshotDuration - (int)elapsedTicks;
            return Math.max(remaining, -1);
         }
      }
   }

   public static List<MobEffectInstance> getEntityEffects(int entityId) {
      return (List)entityEffects.getOrDefault(entityId, new ArrayList());
   }

   public static void removeEntityEffects(int entityId) {
      entityEffects.remove(entityId);
      effectTimestamps.remove(entityId);
   }

   public static boolean hasEntityEffects(int entityId) {
      return entityEffects.containsKey(entityId);
   }

   public static void clearAll() {
      entityEffects.clear();
      effectTimestamps.clear();
   }
}
