package com.auranite.abloom.client;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.handler.DamageNumbersHandler;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class DamageNumbers {

   private static DamageNumbers INSTANCE = null;
   private final @NotNull DamageNumbersHandler handler;

   public static @NotNull DamageNumbersHandler getHandler() {
      if (INSTANCE == null) {
         throw new IllegalStateException("DamageNumbers not initialized");
      }
      return INSTANCE.handler;
   }

   public DamageNumbers(@NotNull Path configDir) {
      INSTANCE = this;
      this.handler = new DamageNumbersImpl();
      AbloomMod.LOGGER.info("DamageNumbers initialized successfully");
   }
}
