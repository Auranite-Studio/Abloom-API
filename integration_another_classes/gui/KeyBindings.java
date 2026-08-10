package com.yanghao.effect_display.gui;

import com.mojang.blaze3d.platform.InputConstants.Type;
import com.yanghao.effect_display.gui.config.RenderConfigScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public class KeyBindings {
   private static final String KEY_CATEGORY_EFFECT_DISPLAY = "key.category.effect_display.effect_display";
   private static final String KEY_EFFECT_DISPLAY_CONFIG = "key.effect_display.config";
   public static final KeyMapping EFFECT_DISPLAY_CONFIG_KEY;

   public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
      event.register(EFFECT_DISPLAY_CONFIG_KEY);
   }

   static {
      EFFECT_DISPLAY_CONFIG_KEY = new KeyMapping("key.effect_display.config", Type.KEYSYM, -1, "key.category.effect_display.effect_display");
   }

   @EventBusSubscriber(
      modid = "effect_display",
      value = {Dist.CLIENT}
   )
   public static class ClientEvents {
      @SubscribeEvent
      public static void onKeyInput(InputEvent.Key event) {
         if (KeyBindings.EFFECT_DISPLAY_CONFIG_KEY.consumeClick()) {
            Minecraft.getInstance().setScreen(new RenderConfigScreen(Component.literal("药水效果显示渲染配置")));
         }

      }
   }
}
