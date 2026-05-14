package com.auranite.abloom.data;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.ElementType;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = AbloomMod.MODID)
public class ElementalDamageTypeReloadListener extends SimpleJsonResourceReloadListener {

    private static final Map<ResourceLocation, ElementalDamageTypeData> CUSTOM_DAMAGE_TYPES = new ConcurrentHashMap<>();
    private static final String FOLDER_NAME = "elemental_dmg_types";
    private static final Gson GSON = new Gson();

    public ElementalDamageTypeReloadListener() {
        super(GSON, FOLDER_NAME);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        CUSTOM_DAMAGE_TYPES.clear();
        
        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonElement jsonElement = entry.getValue();
            
            if (!jsonElement.isJsonObject()) {
                AbloomMod.LOGGER.warn("Skipping invalid JSON for {}: not an object", id);
                continue;
            }
            
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            ElementalDamageTypeData data = parseDamageTypeData(id, jsonObject);
            
            if (data != null) {
                CUSTOM_DAMAGE_TYPES.put(id, data);
                // Register the custom type in ElementType
                ElementType.fromDatapackData(data);
                AbloomMod.LOGGER.info("Loaded custom elemental damage type: {} -> {}", id, data.elementItemTooltip());
            }
        }
        
        AbloomMod.LOGGER.info("Loaded {} custom elemental damage types from datapacks", CUSTOM_DAMAGE_TYPES.size());
    }

    private ElementalDamageTypeData parseDamageTypeData(ResourceLocation id, JsonObject json) {
        try {
            ResourceLocation damageSourceId;
            if (json.has("damage_source_id")) {
                damageSourceId = ResourceLocation.parse(json.get("damage_source_id").getAsString());
            } else {
                damageSourceId = ResourceLocation.parse("minecraft:generic");
            }
            
            int color = 0xFFFFFF;
            if (json.has("color")) {
                color = json.get("color").getAsInt();
            }
            
            Optional<ResourceLocation> resonanceEffect = Optional.empty();
            if (json.has("resonance_effect")) {
                resonanceEffect = Optional.of(ResourceLocation.parse(json.get("resonance_effect").getAsString()));
            }
            
            int resonanceEffectDuration = 8;
            if (json.has("resonance_effect_duration")) {
                resonanceEffectDuration = json.get("resonance_effect_duration").getAsInt();
            }
            
            String elementItemTooltip = "elemental.tooltip.unknown";
            if (json.has("element_Item_tooltip")) {
                elementItemTooltip = json.get("element_Item_tooltip").getAsString();
            }
            
            String elementArmorResistanceTooltip = "elemental.resistance.unknown";
            if (json.has("element_armor_resistance_tooltip")) {
                elementArmorResistanceTooltip = json.get("element_armor_resistance_tooltip").getAsString();
            }
            
            Optional<String> resonanceEffectStatusTextDisplay = Optional.empty();
            if (json.has("resonance_effect.status_text_display")) {
                resonanceEffectStatusTextDisplay = Optional.of(json.get("resonance_effect.status_text_display").getAsString());
            }
            
            return new ElementalDamageTypeData(
                    id,
                    damageSourceId,
                    color,
                    resonanceEffect,
                    resonanceEffectDuration,
                    elementItemTooltip,
                    elementArmorResistanceTooltip,
                    resonanceEffectStatusTextDisplay
            );
        } catch (Exception e) {
            AbloomMod.LOGGER.error("Failed to parse elemental damage type {}: {}", id, e.getMessage());
            return null;
        }
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
