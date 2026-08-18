package com.auranite.abloom.datapack;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.registries.ElementalWeaponRegistry;
import com.auranite.abloom.util.ElementType;
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
import java.util.List;
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

            // Parse using the new ElementalWeaponData class which supports stages
            ElementalWeaponData weaponData = ElementalWeaponData.fromJson(jsonObject);

            var itemLocation = weaponData.getItemResourceLocation();
            if (!itemLocation.isPresent()) {
                AbloomMod.LOGGER.warn("Invalid item location in {} (from mod {})", sourcePath, modId);
                return;
            }

            Identifier location = itemLocation.get();

            if (ElementalWeaponRegistry.isBuiltinRegistered(location)) {
                AbloomMod.LOGGER.warn("Duplicate builtin registration for {} in {} (from mod {}), skipping",
                        location, sourcePath, modId);
                return;
            }

            // Determine the base element for tooltip display
            ElementType baseElement = null;

            if (weaponData.hasStages()) {
                // Multi-stage weapon: use base_element field, or fall back to first stage
                var baseElementOpt = weaponData.getBaseElementType();
                if (baseElementOpt.isPresent()) {
                    baseElement = baseElementOpt.get();
                    ElementalWeaponRegistry.setBaseElement(location, baseElement);
                    AbloomMod.LOGGER.debug("Base element for multi-stage weapon {}: {}", location, baseElement);
                }
            } else {
                // Single-element weapon: use element field
                var elementTypeOpt = weaponData.getElementType();
                if (!elementTypeOpt.isPresent()) {
                    AbloomMod.LOGGER.warn("Invalid element type in {} (from mod {}): {}", sourcePath, modId, weaponData.getElement());
                    return;
                }
                baseElement = elementTypeOpt.get();
                ElementalWeaponRegistry.setBaseElement(location, baseElement);
                AbloomMod.LOGGER.debug("Base element for weapon {}: {}", location, baseElement);
            }

            // Handle multi-stage weapons
            if (weaponData.hasStages()) {
                List<ElementalWeaponData.WeaponStage> stages = weaponData.getStages();

                AbloomMod.LOGGER.info("Loading multi-stage weapon: {} ({} stages) from mod {}",
                        location, stages.size(), modId);

                for (ElementalWeaponData.WeaponStage stage : stages) {
                    ElementType stageElement = stage.getElementType();
                    if (stageElement == null) {
                        AbloomMod.LOGGER.warn("Invalid element type in stage {} of {} (from mod {}): {}, using PHYSICAL",
                                stage.getStageNumber(), location, modId, stage.getElementTypeString());
                        stageElement = ElementType.PHYSICAL;
                    }

                    // Register each stage separately with its element and accumulation
                    ElementalWeaponRegistry.registerBuiltinWeaponWithStage(
                            location,
                            stage.getStageNumber(),
                            stageElement,
                            stage.getAccumulationMultiplier(),
                            weaponData.getCritChance(),
                            weaponData.getCritDamage()
                    );
                }

                loadedCount.getAndIncrement();
                AbloomMod.LOGGER.debug("Registered multi-stage elemental weapon: {} with {} stages from mod {}",
                        location, stages.size(), modId);
            } else {
                // Legacy single-element format
                ElementType elementType = baseElement;

                ElementalWeaponRegistry.registerBuiltinWeapon(
                        location,
                        elementType,
                        weaponData.getAccumulationMultiplier(),
                        weaponData.getCritChance(),
                        weaponData.getCritDamage()
                );

                loadedCount.getAndIncrement();
                AbloomMod.LOGGER.debug("Registered elemental weapon: {} -> {} (multiplier: {}, crit: {:.0f}%/{:.0f}%) from mod {}",
                        location, elementType, weaponData.getAccumulationMultiplier(),
                        weaponData.getCritChance() * 100, weaponData.getCritDamage() * 100, modId);
            }
        } catch (Exception e) {
            AbloomMod.LOGGER.error("Failed to load elemental weapon from {} (from mod {})", sourcePath, modId, e);
        }
    }
}