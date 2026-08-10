package com.yanghao.effect_display;

import com.mojang.logging.LogUtils;
import com.yanghao.effect_display.gui.KeyBindings;
import com.yanghao.effect_display.network.ClientEntityEffectsStorage;
import com.yanghao.effect_display.network.EffectDisplayNetworking;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.slf4j.Logger;

@Mod("effect_display")
public class EffectDisplayMod {
   public static final String MODID = "effect_display";
   public static final Logger LOGGER = LogUtils.getLogger();

   public EffectDisplayMod(IEventBus modEventBus, ModContainer modContainer) {
      NeoForge.EVENT_BUS.register(this);
      modEventBus.addListener(KeyBindings::registerKeyBindings);
      modEventBus.addListener(EffectDisplayNetworking::register);
      modContainer.registerConfig(Type.CLIENT, EffectDisplayConfig.CLIENT_SPEC);
   }

   @SubscribeEvent
   public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      ClientEntityEffectsStorage.clearAll();
   }
}
