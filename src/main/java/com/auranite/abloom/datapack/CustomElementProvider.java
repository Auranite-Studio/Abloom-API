package com.auranite.abloom.datapack;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.util.ElementType;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Loads custom elemental type definitions from datapack JSON files.
 * 
 * <p>Searches for files in {@code data/{modid}/elemental_types/} directory
 * within each mod's resources. Each JSON file defines one custom element type.
 * 
 * <p>Example JSON format:
 * <pre>{@code
 * {
 *   "element_id": "yourmod:custom_element",
 *   "override_damage_types": ["yourmod:custom_element_damage_source", "minecraft:generic"],
 *   "color": 2891628,
 *   "element_translation_key": "element.yourmod.custom",
 *   "resonance_translation_key": "resonance_text.yourmod.custom",
 *   "can_resonance_accumulation": true,
 *   "resonance_dmg_multiplier": 2.0,
 *   "resonance_effect": {
 *     "effect": "yourmod:custom_resonance_effect",
 *     "config": {
 *       "duration": 100,
 *       "amplifier": 0
 *     }
 *   }
 * }
 * }</pre>
 */
public class CustomElementProvider {

    public static final String DATAPACK_PATH = "elemental_types";

    /**
     * Loads all custom elemental types from all mod resources.
     * Should be called during mod initialization.
     */
    public static void loadFromResources() {
        var modList = ModList.get();
        AtomicInteger totalLoadedCount = new AtomicInteger();

        for (var modInfo : modList.getMods()) {
            String modId = modInfo.getModId();
            var modFileInfo = modList.getModFileById(modId);
            if (modFileInfo == null) {
                continue;
            }

            var modFile = modFileInfo.getFile();
            Path rootPath = modFile.getSecureJar().getRootPath();

            // Look for files in data/{modid}/elemental_types/
            Path typesDir = rootPath.resolve("data/" + modId + "/" + DATAPACK_PATH);

            if (!Files.exists(typesDir)) {
                continue;
            }

            AtomicInteger loadedCount = new AtomicInteger();

            try (Stream<Path> paths = Files.walk(typesDir)) {
                paths.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".json"))
                        .forEach(path -> {
                            try {
                                String jsonContent = Files.readString(path, StandardCharsets.UTF_8);
                                String sourcePath = rootPath.relativize(path).toString().replace('\\', '/');

                                loadElementFromJson(sourcePath, jsonContent, loadedCount, modId);
                            } catch (IOException e) {
                                AbloomMod.LOGGER.error("Failed to read custom element from {}", path, e);
                            }
                        });
            } catch (IOException e) {
                AbloomMod.LOGGER.error("Failed to scan directory for custom elements in mod {}", modId, e);
            }

            if (loadedCount.get() > 0) {
                AbloomMod.LOGGER.info("Loaded {} custom element types from mod '{}'", loadedCount.get(), modId);
                totalLoadedCount.addAndGet(loadedCount.get());
            }
        }

        AbloomMod.LOGGER.info("Total: Loaded {} custom element types from all mods", totalLoadedCount.get());
    }

    private static void loadElementFromJson(String sourcePath, String jsonContent, AtomicInteger loadedCount, String modId) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();
            CustomElementData elementData = CustomElementData.fromJson(jsonObject);

            // Validate element_id
            ResourceLocation elementId = elementData.elementId();
            if (elementId == null) {
                AbloomMod.LOGGER.warn("Missing element_id in {} (from mod {})", sourcePath, modId);
                return;
            }

            // Generate enum-style name and damage type ID
            String elementName = elementData.getElementName();
            String damageTypeId = elementData.getDamageTypeId();

            // Register the custom element type
            boolean registered = ElementType.registerCustomElementType(
                    elementName,
                    damageTypeId,
                    elementData.getDisplayName()
            );

            if (!registered) {
                AbloomMod.LOGGER.warn("Duplicate custom element type '{}' in {} (from mod {}), skipping",
                        elementName, sourcePath, modId);
                return;
            }

            // Register override damage types if present
            ResourceLocation[] overrideDamageTypes = elementData.overrideDamageTypes();
            if (overrideDamageTypes != null && overrideDamageTypes.length > 0) {
                AbloomMod.LOGGER.debug("Registered {} override damage types for element '{}' from mod {}",
                        overrideDamageTypes.length, elementName, modId);
                for (ResourceLocation damageType : overrideDamageTypes) {
                    AbloomMod.LOGGER.debug("  - {}", damageType);
                }
            }

            // Log element configuration
            AbloomMod.LOGGER.info("Loaded custom element '{}' (color: 0x{:06X}, resonance: {}, multiplier: {:.1f}) from mod {}",
                    elementName,
                    elementData.color() & 0xFFFFFF,
                    elementData.canResonanceAccumulation(),
                    elementData.resonanceDmgMultiplier(),
                    modId);

            // Log resonance effect if present
            if (elementData.resonanceEffect() != null) {
                CustomElementData.ResonanceEffectConfig resonanceEffect = elementData.resonanceEffect();
                AbloomMod.LOGGER.info("  Resonance effect: {} (duration: {} ticks, amplifier: {})",
                        resonanceEffect.effect(),
                        resonanceEffect.duration(),
                        resonanceEffect.amplifier());
            }

            loadedCount.getAndIncrement();

        } catch (Exception e) {
            AbloomMod.LOGGER.error("Failed to load custom element from {} (from mod {})", sourcePath, modId, e);
        }
    }
}