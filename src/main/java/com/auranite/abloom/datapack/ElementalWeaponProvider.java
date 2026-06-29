package com.auranite.abloom.datapack;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.ElementType;
import com.auranite.abloom.ElementalWeaponRegistry;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.locating.IModFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ElementalWeaponProvider {

    public static final String DATAPACK_PATH = "elemental_weapons";

    public static void loadFromResources() {
        var modList = ModList.get();

        AtomicInteger totalLoadedCount = new AtomicInteger();

        for (var modInfo : modList.getMods()) {
            String modId = modInfo.getModId();
            var modFileInfo = modList.getModFileById(modId);
            if (modFileInfo == null) {
                continue;
            }

            IModFile modFile = modFileInfo.getFile();
            Path rootPath = modFile.getSecureJar().getRootPath();

            // Ищем файлы в папке data/{modid}/elemental_weapons/
            Path weaponsDir = rootPath.resolve("data/" + modId + "/" + DATAPACK_PATH);

            if (!Files.exists(weaponsDir)) {
                continue; // В этом моде нет файлов elemental weapons
            }

            AtomicInteger loadedCount = new AtomicInteger();

            try (Stream<Path> paths = Files.walk(weaponsDir)) {
                paths.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".json"))
                        .forEach(path -> {
                            try {
                                String jsonContent = Files.readString(path, StandardCharsets.UTF_8);
                                String sourcePath = rootPath.relativize(path).toString().replace('\\', '/');

                                loadWeaponFromJson(sourcePath, jsonContent, loadedCount, modId);
                            } catch (IOException e) {
                                AbloomMod.LOGGER.error("Failed to read elemental weapon from {}", path, e);
                            }
                        });
            } catch (IOException e) {
                AbloomMod.LOGGER.error("Failed to scan directory for elemental weapons in mod {}", modId, e);
            }

            if (loadedCount.get() > 0) {
                AbloomMod.LOGGER.info("Loaded {} elemental weapons from mod '{}'", loadedCount.get(), modId);
                totalLoadedCount.addAndGet(loadedCount.get());
            }
        }

        AbloomMod.LOGGER.info("Total: Loaded {} elemental weapons from all mods", totalLoadedCount);
    }

    private static void loadWeaponFromJson(String sourcePath, String jsonContent, AtomicInteger loadedCount, String modId) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();

            String item = GsonHelper.getAsString(jsonObject, "item");
            String element = GsonHelper.getAsString(jsonObject, "element");
            float accumMultiplier = GsonHelper.getAsFloat(jsonObject, "accumulation_multiplier", 1.0f);

            ResourceLocation itemLocation = ResourceLocation.parse(item);

            ElementType elementType = ElementType.safeValueOf(element);
            if (elementType == null) {
                AbloomMod.LOGGER.warn("Invalid element type in {} (from mod {}): {}", sourcePath, modId, element);
                return;
            }

            if (ElementalWeaponRegistry.isBuiltinRegistered(itemLocation)) {
                AbloomMod.LOGGER.warn("Duplicate builtin registration for {} in {} (from mod {}), skipping", 
                        itemLocation, sourcePath, modId);
                return;
            }

            ElementalWeaponRegistry.registerBuiltinWeapon(itemLocation, elementType, accumMultiplier);
            loadedCount.getAndIncrement();
            AbloomMod.LOGGER.debug("Registered elemental weapon: {} -> {} (multiplier: {}) from mod {}",
                    itemLocation, elementType, accumMultiplier, modId);
        } catch (Exception e) {
            AbloomMod.LOGGER.error("Failed to load elemental weapon from {} (from mod {})", sourcePath, modId, e);
        }
    }
}