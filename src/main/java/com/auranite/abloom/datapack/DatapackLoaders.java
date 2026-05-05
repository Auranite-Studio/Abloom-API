package com.auranite.abloom.datapack;

import com.auranite.abloom.AbloomMod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Event subscriber that registers datapack loaders for custom elemental damage types.
 */
@EventBusSubscriber(modid = AbloomMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DatapackLoaders {
    
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        // Register custom element types loader
        event.addListener(new CustomElementTypesLoader());
        
        // Register custom element resistances loader
        event.addListener(new CustomElementResistancesLoader());
        
        AbloomMod.LOGGER.info("Registered Abloom datapack loaders");
    }
}
