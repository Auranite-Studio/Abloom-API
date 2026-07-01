package com.auranite.abloom.datapack;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.ArmorResistanceRegistry;
import com.auranite.abloom.ElementType;
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
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ArmorResistanceProvider {

    public static final String DATAPACK_PATH = "armor_resistances";

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
            AbloomMod.LOGGER.debug("Loading armor resistances for mod {} from path: {}", modId, rootPath);

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
                Path resistancesDir;
                if ("abloom".equals(modId)) {
                    // Читаем из src/main/resources (относительно корня проекта)
                    // rootPath = C:\mods\Abloom-API\build\classes\java\main
                    // parent = C:\mods\Abloom-API\build\classes\java
                    // parent.parent = C:\mods\Abloom-API\build\classes
                    // parent.parent.parent = C:\mods\Abloom-API\build
                    // parent.parent.parent.parent = C:\mods\Abloom-API (корень проекта)
                    Path projectRoot = rootPath.getParent().getParent().getParent().getParent();
                    resistancesDir = projectRoot.resolve("src/main/resources/data/" + modId + "/" + DATAPACK_PATH);
                } else {
                    // Для других модов читаем из build/resources
                    Path resourcesRoot = rootPath.getParent().resolve("resources");
                    resistancesDir = resourcesRoot.resolve("data/" + modId + "/" + DATAPACK_PATH);
                }

                AbloomMod.LOGGER.debug("Checking resources directory: {}", resistancesDir);
                AbloomMod.LOGGER.debug("Resources directory exists: {}", Files.exists(resistancesDir));

                if (!Files.exists(resistancesDir)) {
                    AbloomMod.LOGGER.debug("Resources directory does not exist: {}", resistancesDir);
                    continue;
                }

                try (java.util.stream.Stream<Path> paths = Files.walk(resistancesDir)) {
                    paths.filter(Files::isRegularFile)
                            .filter(p -> p.toString().endsWith(".json"))
                            .forEach(path -> {
                                try {
                                    String jsonContent = Files.readString(path, StandardCharsets.UTF_8);
                                    String sourcePath = resistancesDir.relativize(path).toString().replace('\\', '/');

                                    loadArmorFromJson(sourcePath, jsonContent, loadedCount, modId);
                                } catch (IOException e) {
                                    AbloomMod.LOGGER.error("Failed to read armor resistance from {}", path, e);
                                }
                            });
                } catch (IOException e) {
                    AbloomMod.LOGGER.error("Failed to scan directory for armor resistances in mod {}", modId, e);
                }
            }

            if (loadedCount.get() > 0) {
                AbloomMod.LOGGER.info("Loaded {} armor resistances from mod '{}'", loadedCount.get(), modId);
                totalLoadedCount.addAndGet(loadedCount.get());
            }
        }

        AbloomMod.LOGGER.info("Total: Loaded {} armor resistances from all mods", totalLoadedCount);
    }

    private static void loadFromJar(Path jarPath, String resourcesPrefix, AtomicInteger loadedCount, String modId) {
        try (java.util.jar.JarFile jarFile = new java.util.jar.JarFile(jarPath.toFile())) {
            jarFile.stream()
                    .filter(entry -> entry.getName().startsWith(resourcesPrefix) && entry.getName().endsWith(".json"))
                    .forEach(entry -> {
                        try (InputStream inputStream = jarFile.getInputStream(entry)) {
                            String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                            String sourcePath = entry.getName().replace('\\', '/');

                            loadArmorFromJson(sourcePath, jsonContent, loadedCount, modId);
                        } catch (IOException e) {
                            AbloomMod.LOGGER.error("Failed to read armor resistance from {}", entry.getName(), e);
                        }
                    });
        } catch (IOException e) {
            AbloomMod.LOGGER.error("Failed to scan JAR for armor resistances in mod {}", modId, e);
        }
    }

    private static void loadArmorFromJson(String sourcePath, String jsonContent, AtomicInteger loadedCount, String modId) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();

            String item = GsonHelper.getAsString(jsonObject, "item");

            Identifier itemLocation = Identifier.parse(item);

            // Проверяем, не зарегистрирован ли уже этот предмет
            if (ArmorResistanceRegistry.isBuiltinRegistered(itemLocation)) {
                AbloomMod.LOGGER.warn("Duplicate builtin registration for {} in {} (from mod {}), skipping", 
                        itemLocation, sourcePath, modId);
                return;
            }

            // Парсим сопротивления
            Map<ElementType, Float> resistances = new EnumMap<>(ElementType.class);
            if (jsonObject.has("resistances")) {
                JsonObject resistancesObj = GsonHelper.getAsJsonObject(jsonObject, "resistances");
                for (String key : resistancesObj.keySet()) {
                    ElementType elementType = ElementType.safeValueOf(key.toUpperCase());
                    if (elementType != null) {
                        float value = GsonHelper.getAsFloat(resistancesObj, key);
                        resistances.put(elementType, value);
                    } else {
                        AbloomMod.LOGGER.warn("Invalid element type: {} in {} (from mod {})", key, sourcePath, modId);
                    }
                }
            }

            if (resistances.isEmpty()) {
                AbloomMod.LOGGER.warn("No valid resistances found in {} (from mod {})", sourcePath, modId);
                return;
            }

            ArmorResistanceRegistry.registerBuiltinArmor(itemLocation, resistances);
            loadedCount.getAndIncrement();
            AbloomMod.LOGGER.debug("Registered armor resistance: {} -> {} from mod {}",
                    itemLocation, resistances, modId);
        } catch (Exception e) {
            AbloomMod.LOGGER.error("Failed to load armor resistance from {} (from mod {})", sourcePath, modId, e);
        }
    }
}
