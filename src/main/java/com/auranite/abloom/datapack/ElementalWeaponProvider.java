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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

public class ElementalWeaponProvider {

    public static final String DATAPACK_PATH = "elemental_weapons";

    public static void loadFromResources() {
        var modList = ModList.get();

        AtomicInteger totalLoadedCount = new AtomicInteger();

        for (var modInfo : modList.getMods()) {
            String modId = modInfo.getModId();

            // Получаем файл мода по ID
            var modFileInfo = modList.getModFileById(modId);
            if (modFileInfo == null) {
                continue;
            }

            IModFile modFile = modFileInfo.getFile();
            if (modFile == null) {
                continue;
            }

            // Получаем путь к корню мода
            Path rootPath = modFile.getFilePath();
            AbloomMod.LOGGER.debug("Loading elemental weapons for mod {} from path: {}", modId, rootPath);

            // Путь к папке с ресурсами внутри JAR
            String resourcesPrefix = "data/" + modId + "/" + DATAPACK_PATH + "/";
            AbloomMod.LOGGER.debug("Resources prefix for mod {}: {}", modId, resourcesPrefix);

            AtomicInteger loadedCount = new AtomicInteger();

            // Проверяем, является ли это JAR-файлом
            if (rootPath.toString().endsWith(".jar")) {
                loadFromJar(rootPath, resourcesPrefix, loadedCount, modId);
            } else {
                // Это папка с исходниками (например, при разработке)
                // Для нашего мода (abloom) читаем из src/main/resources
                Path weaponsDir;
                if ("abloom".equals(modId)) {
                    // Читаем из src/main/resources (относительно корня проекта)
                    // rootPath = C:\mods\Abloom-API\build\classes\java\main
                    // parent = C:\mods\Abloom-API\build\classes\java
                    // parent.parent = C:\mods\Abloom-API\build\classes
                    // parent.parent.parent = C:\mods\Abloom-API\build
                    // parent.parent.parent.parent = C:\mods\Abloom-API (корень проекта)
                    Path projectRoot = rootPath.getParent().getParent().getParent().getParent();
                    weaponsDir = projectRoot.resolve("src/main/resources/data/" + modId + "/" + DATAPACK_PATH);
                } else {
                    // Для других модов читаем из build/resources
                    Path resourcesRoot = rootPath.getParent().resolve("resources");
                    weaponsDir = resourcesRoot.resolve("data/" + modId + "/" + DATAPACK_PATH);
                }

                AbloomMod.LOGGER.debug("Checking resources directory: {}", weaponsDir);
                AbloomMod.LOGGER.debug("Resources directory exists: {}", Files.exists(weaponsDir));

                if (!Files.exists(weaponsDir)) {
                    AbloomMod.LOGGER.debug("Resources directory does not exist: {}", weaponsDir);
                    continue;
                }

                try (java.util.stream.Stream<Path> paths = Files.walk(weaponsDir)) {
                    paths.filter(Files::isRegularFile)
                            .filter(p -> p.toString().endsWith(".json"))
                            .forEach(path -> {
                                try {
                                    String jsonContent = Files.readString(path, StandardCharsets.UTF_8);
                                    String sourcePath = weaponsDir.relativize(path).toString().replace('\\', '/');

                                    loadWeaponFromJson(sourcePath, jsonContent, loadedCount, modId);
                                } catch (IOException e) {
                                    AbloomMod.LOGGER.error("Failed to read elemental weapon from {}", path, e);
                                }
                            });
                } catch (IOException e) {
                    AbloomMod.LOGGER.error("Failed to scan directory for elemental weapons in mod {}", modId, e);
                }
            }

            if (loadedCount.get() > 0) {
                AbloomMod.LOGGER.info("Loaded {} elemental weapons from mod '{}'", loadedCount.get(), modId);
                totalLoadedCount.addAndGet(loadedCount.get());
            }
        }

        AbloomMod.LOGGER.info("Total: Loaded {} elemental weapons from all mods", totalLoadedCount);
    }

    private static void loadFromJar(Path jarPath, String resourcesPrefix, AtomicInteger loadedCount, String modId) {
        try (java.util.jar.JarFile jarFile = new java.util.jar.JarFile(jarPath.toFile())) {
            jarFile.stream()
                    .filter(entry -> entry.getName().startsWith(resourcesPrefix) && entry.getName().endsWith(".json"))
                    .forEach(entry -> {
                        try (InputStream inputStream = jarFile.getInputStream(entry)) {
                            String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                            String sourcePath = entry.getName().replace('\\', '/');

                            loadWeaponFromJson(sourcePath, jsonContent, loadedCount, modId);
                        } catch (IOException e) {
                            AbloomMod.LOGGER.error("Failed to read elemental weapon from {}", entry.getName(), e);
                        }
                    });
        } catch (IOException e) {
            AbloomMod.LOGGER.error("Failed to scan JAR for elemental weapons in mod {}", modId, e);
        }
    }

    private static void loadWeaponFromJson(String sourcePath, String jsonContent, AtomicInteger loadedCount, String modId) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();

            String item = GsonHelper.getAsString(jsonObject, "item");
            String element = GsonHelper.getAsString(jsonObject, "element");
            float accumMultiplier = GsonHelper.getAsFloat(jsonObject, "accumulation_multiplier", 1.0f);
            float critChance = GsonHelper.getAsFloat(jsonObject, "crit_chance", 0.0f);
            float critDamage = GsonHelper.getAsFloat(jsonObject, "crit_damage", 0.0f);

            Identifier itemLocation = Identifier.parse(item);

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

            ElementalWeaponRegistry.registerBuiltinWeapon(itemLocation, elementType, accumMultiplier, critChance, critDamage);
            loadedCount.getAndIncrement();
            AbloomMod.LOGGER.debug("Registered elemental weapon: {} -> {} (multiplier: {}, crit: {:.0f}%/{:.0f}%) from mod {}",
                    itemLocation, elementType, accumMultiplier, critChance * 100, critDamage * 100, modId);
        } catch (Exception e) {
            AbloomMod.LOGGER.error("Failed to load elemental weapon from {} (from mod {})", sourcePath, modId, e);
        }
    }
}
