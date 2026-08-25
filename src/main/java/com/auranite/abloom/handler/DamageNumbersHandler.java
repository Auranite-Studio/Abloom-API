package com.auranite.abloom.handler;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.auranite.abloom.util.ElementType;

public interface DamageNumbersHandler {
   void onEntityHealthChange(@NotNull LivingEntity entity, float oldHealth, float newHealth);
   void spawnDamageNumber(int entityId, float damage, @Nullable ElementType elementType, int color, boolean isCrit, boolean hasBreak);
   void spawnStatusText(int entityId, @NotNull Component text, int color);
}
