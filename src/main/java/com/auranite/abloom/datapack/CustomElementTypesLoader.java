package com.auranite.abloom.datapack;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.CustomElementType;
import com.auranite.abloom.CustomElementRegistry;
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
    
    public CustomElementTypesLoader() {
        super(JsonOps.INSTANCE, FOLDER);
    }
    
    @Override
    protected void apply(Map<ResourceLocation, Object> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.push("custom_element_types");
        
        int loadedCount = 0;
        int failedCount = 0;
        
        objects.forEach((id, jsonElement) -> {
            try {
                // Parse the JSON element into a CustomElementType
                CustomElementType.CODEC.parse(JsonOps.INSTANCE, jsonElement)
                    .resultOrPartial(error -> {
                        AbloomMod.LOGGER.error("Failed to parse custom element type {}: {}", id, error);
                        failedCount++;
                    })
                    .ifPresent(elementType -> {
                        if (CustomElementRegistry.register(elementType)) {
                            loadedCount++;
                            AbloomMod.LOGGER.debug("Loaded custom element type: {}", id);
                        } else {
                            failedCount++;
                        }
                    });
            } catch (Exception e) {
                AbloomMod.LOGGER.error("Error loading custom element type {}: {}", id, e.getMessage());
                failedCount++;
            }
        });
        
        profiler.pop();
        AbloomMod.LOGGER.info("Loaded {} custom element types ({} failed)", loadedCount, failedCount);
    }
}
