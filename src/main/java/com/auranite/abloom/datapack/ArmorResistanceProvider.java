package com.auranite.abloom.datapack;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.ArmorResistanceRegistry;
import com.auranite.abloom.ElementType;
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
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ArmorResistanceProvider {

    public static final String DATAPACK_PATH = "armor_resistances";

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

            // Ищем файлы в папке data/{modid}/armor_resistances/
            Path resistancesDir = rootPath.resolve("data/" + modId + "/" + DATAPACK_PATH);

            if (!Files.exists(resistancesDir)) {
                continue; // В этом моде нет файлов armor resistances
            }

            AtomicInteger loadedCount = new AtomicInteger();

            try (Stream<Path> paths = Files.walk(resistancesDir)) {
                paths.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".json"))
                        .forEach(path -> {
                            try {
                                String jsonContent = Files.readString(path, StandardCharsets.UTF_8);
                                String sourcePath = rootPath.relativize(path).toString().replace('\\', '/');

                                loadArmorFromJson(sourcePath, jsonContent, loadedCount, modId);
                            } catch (IOException e) {
                                AbloomMod.LOGGER.error("Failed to read armor resistance from {}", path, e);
                            }
                        });
            } catch (IOException e) {
                AbloomMod.LOGGER.error("Failed to scan directory for armor resistances in mod {}", modId, e);
            }

            if (loadedCount.get() > 0) {
                AbloomMod.LOGGER.info("Loaded {} armor resistances from mod '{}'", loadedCount.get(), modId);
                totalLoadedCount.addAndGet(loadedCount.get());
            }
        }

        AbloomMod.LOGGER.info("Total: Loaded {} armor resistances from all mods", totalLoadedCount);
    }

    private static void loadArmorFromJson(String sourcePath, String jsonContent, AtomicInteger loadedCount, String modId) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();

            String item = GsonHelper.getAsString(jsonObject, "item");
            
            ResourceLocation itemLocation = ResourceLocation.parse(item);

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
