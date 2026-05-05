package com.auranite.abloom.datapack;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.CustomElementType;
import com.auranite.abloom.CustomElementRegistry;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

/**
 * Datapack loader for custom elemental damage types.
 * Reads JSON files from data/<namespace>/abloom/element_types/*.json
 */
public class CustomElementTypesLoader extends SimpleJsonResourceReloadListener {
    
    private static final String FOLDER = "abloom/element_types";
    private static final Gson GSON = new Gson();
    
    public CustomElementTypesLoader() {
        super(GSON, FOLDER);
    }
    
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.push("custom_element_types");
        
        int[] loadedCount = {0};
        int[] failedCount = {0};
        
        objects.forEach((id, jsonElement) -> {
            try {
                // Parse the JSON element into a CustomElementType
                CustomElementType.CODEC.parse(JsonOps.INSTANCE, jsonElement)
                    .resultOrPartial(error -> {
                        AbloomMod.LOGGER.error("Failed to parse custom element type {}: {}", id, error);
                        failedCount[0]++;
                    })
                    .ifPresent(elementType -> {
                        if (CustomElementRegistry.register(elementType)) {
                            loadedCount[0]++;
                            AbloomMod.LOGGER.debug("Loaded custom element type: {}", id);
                        } else {
                            failedCount[0]++;
                        }
                    });
            } catch (Exception e) {
                AbloomMod.LOGGER.error("Error loading custom element type {}: {}", id, e.getMessage());
                failedCount[0]++;
            }
        });
        
        profiler.pop();
        AbloomMod.LOGGER.info("Loaded {} custom element types ({} failed)", loadedCount[0], failedCount[0]);
    }
}
