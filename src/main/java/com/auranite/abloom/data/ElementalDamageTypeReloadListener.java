package com.auranite.abloom.data;

import com.auranite.abloom.AbloomMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = AbloomMod.MODID)
public class ElementalDamageTypeReloadListener extends SimpleJsonResourceReloadListener<ElementalDamageTypeData> {

    private static final Map<ResourceLocation, ElementalDamageTypeData> CUSTOM_DAMAGE_TYPES = new ConcurrentHashMap<>();
    private static final String FOLDER_NAME = "elemental_dmg_types";

    public ElementalDamageTypeReloadListener() {
        super(ElementalDamageTypeData.CODEC, FOLDER_NAME);
    }

    @Override
    protected void apply(Map<ResourceLocation, ElementalDamageTypeData> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        CUSTOM_DAMAGE_TYPES.clear();
        
        for (Map.Entry<ResourceLocation, ElementalDamageTypeData> entry : objects.entrySet()) {
            ResourceLocation id = entry.getKey();
            ElementalDamageTypeData data = entry.getValue();
            
            CUSTOM_DAMAGE_TYPES.put(id, data);
            AbloomMod.LOGGER.info("Loaded custom elemental damage type: {} -> {}", id, data.elementItemTooltip());
        }
        
        AbloomMod.LOGGER.info("Loaded {} custom elemental damage types from datapacks", CUSTOM_DAMAGE_TYPES.size());
    }

    @SubscribeEvent
    public static void onRegisterReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ElementalDamageTypeReloadListener());
        AbloomMod.LOGGER.info("Registered ElementalDamageTypeReloadListener");
    }

    public static Map<ResourceLocation, ElementalDamageTypeData> getAllCustomDamageTypes() {
        return Map.copyOf(CUSTOM_DAMAGE_TYPES);
    }

    public static ElementalDamageTypeData getCustomDamageType(ResourceLocation id) {
        return CUSTOM_DAMAGE_TYPES.get(id);
    }

    public static boolean hasCustomDamageType(ResourceLocation id) {
        return CUSTOM_DAMAGE_TYPES.containsKey(id);
    }
}
