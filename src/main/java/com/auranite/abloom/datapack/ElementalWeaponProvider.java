package com.auranite.abloom.datapack;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.ElementType;
import com.auranite.abloom.ElementalWeaponRegistry;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
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
        var modFileInfo = ModList.get().getModFileById("abloom");
        if (modFileInfo == null) {
            AbloomMod.LOGGER.error("Mod 'abloom' not found in ModList!");
            return;
        }

        IModFile modFile = modFileInfo.getFile();
        Path rootPath = modFile.getSecureJar().getRootPath(); // Корень мода (JAR или папка resources в dev)
        Path weaponsDir = rootPath.resolve("data/abloom/" + DATAPACK_PATH);

        if (!Files.exists(weaponsDir)) {
            AbloomMod.LOGGER.warn("Elemental weapons directory not found: {}", weaponsDir);
            return;
        }

        AtomicInteger loadedCount = new AtomicInteger();

        try (Stream<Path> paths = Files.walk(weaponsDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            // Читаем содержимое файла напрямую через NIO
                            String jsonContent = Files.readString(path, StandardCharsets.UTF_8);
                            // Получаем относительный путь для красивого логирования
                            String sourcePath = rootPath.relativize(path).toString().replace('\\', '/');

                            loadWeaponFromJson(sourcePath, jsonContent, loadedCount);
                        } catch (IOException e) {
                            AbloomMod.LOGGER.error("Failed to read elemental weapon from {}", path, e);
                        }
                    });
        } catch (IOException e) {
            AbloomMod.LOGGER.error("Failed to scan directory for elemental weapons", e);
        }

        AbloomMod.LOGGER.info("Loaded {} elemental weapons from mod resources", loadedCount);
    }

    private static void loadWeaponFromJson(String sourcePath, String jsonContent, AtomicInteger loadedCount) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();

            String item = GsonHelper.getAsString(jsonObject, "item");
            String element = GsonHelper.getAsString(jsonObject, "element");
            float accumMultiplier = GsonHelper.getAsFloat(jsonObject, "accumulation_multiplier", 1.0f);

            Identifier itemLocation = Identifier.parse(item);

            ElementType elementType = ElementType.safeValueOf(element);
            if (elementType == null) {
                AbloomMod.LOGGER.warn("Invalid element type in {}: {}", sourcePath, element);
                return;
            }

            if (ElementalWeaponRegistry.isBuiltinRegistered(itemLocation)) {
                AbloomMod.LOGGER.warn("Duplicate builtin registration for {} in {}, skipping", itemLocation, sourcePath);
                return;
            }

            ElementalWeaponRegistry.registerBuiltinWeapon(itemLocation, elementType, accumMultiplier);
            loadedCount.getAndIncrement();
            AbloomMod.LOGGER.debug("Registered elemental weapon: {} -> {} (multiplier: {})",
                    itemLocation, elementType, accumMultiplier);
        } catch (Exception e) {
            AbloomMod.LOGGER.error("Failed to load elemental weapon from {}", sourcePath, e);
        }
    }
}