package com.auranite.abloom.datapack;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.ElementType;
import com.auranite.abloom.ElementalWeaponRegistry;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class ElementalWeaponProvider {

    public static final String DATAPACK_PATH = "elemental_weapons";
    public static final ResourceLocation DATAPACK_ID = ResourceLocation.fromNamespaceAndPath("abloom", DATAPACK_PATH);
    
    /**
     * Загрузка elemental weapons из resources/data/abloom/elemental_weapons/
     */
    public static void loadFromResources() {
        ClassLoader classLoader = ElementalWeaponProvider.class.getClassLoader();
        // ClassLoader путь: ресурсы лежат напрямую, без префикса data/
        String prefix = "data/abloom/" + DATAPACK_PATH + "/";
        
        List<String> jsonFiles = new ArrayList<>();
        
        // Пытаемся найти ресурсы ��з JAR или directory
        try {
            Enumeration<URL> resources = classLoader.getResources("");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                String urlString = url.toString();
                
                if (urlString.contains(".jar!")) {
                    // JAR файл - нужно извлечь путь до JAR и пробежаться по всем его содержимому
                    String jarPath = urlString.substring(5, urlString.indexOf("!"));
                    AbloomMod.LOGGER.debug("Scanning JAR for elemental weapons: {}", jarPath);
                    scanJar(jarPath, prefix, jsonFiles);
                } else if (urlString.startsWith("file:")) {
                    // Directory - URL уже указывает на директорию, добавляем prefix
                    String baseDir = urlString.substring(5);
                    String dirPath = baseDir + prefix;
                    AbloomMod.LOGGER.debug("Scanning directory for elemental weapons: {}", dirPath);
                    scanDirectory(dirPath, prefix, jsonFiles);
                }
            }
        } catch (IOException e) {
            AbloomMod.LOGGER.error("Error scanning classpath for elemental weapons", e);
            return;
        }
        
        // Если основной способ не нашел файлы, используем fallback
        if (jsonFiles.isEmpty()) {
            AbloomMod.LOGGER.info("Registering elemental weapons (fallback code)...");
            try {
                scanFallbackResources(classLoader, prefix, jsonFiles);
            } catch (IOException e) {
                AbloomMod.LOGGER.error("Error in fallback resource loading", e);
            }
        }
        
        if (jsonFiles.isEmpty()) {
            AbloomMod.LOGGER.warn("No elemental weapons found in resources!");
            return;
        }
        
        AbloomMod.LOGGER.info("Found {} elemental weapon definitions in resources", jsonFiles.size());
        
        AtomicInteger loadedCount = new AtomicInteger();
        jsonFiles.forEach(filePath -> {
            try (InputStream is = classLoader.getResourceAsStream(filePath);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                if (is == null) {
                    AbloomMod.LOGGER.warn("Failed to open resource: {}", filePath);
                    return;
                }
                
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                
                loadWeaponFromJson(filePath, content.toString(), loadedCount);
            } catch (IOException e) {
                AbloomMod.LOGGER.error("Failed to read elemental weapon from {}", filePath, e);
            }
        });
        
        AbloomMod.LOGGER.info("Loaded {} elemental weapons from datapack", loadedCount);
    }
    
    private static void scanJar(String jarPath, String prefix, List<String> jsonFiles) {
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                
                if (name.startsWith(prefix) && name.endsWith(".json") && !entry.isDirectory()) {
                    AbloomMod.LOGGER.debug("Found elemental weapon in JAR: {}", name);
                    jsonFiles.add(name);
                }
            }
        } catch (IOException e) {
            AbloomMod.LOGGER.warn("Failed to scan JAR for elemental weapons: {}", jarPath, e);
        }
    }
    
    private static void scanDirectory(String dirPath, String prefix, List<String> jsonFiles) {
        java.nio.file.Path path = java.nio.file.Paths.get(dirPath);
        if (!java.nio.file.Files.exists(path)) {
            AbloomMod.LOGGER.debug("Directory does not exist: {}", dirPath);
            return;
        }
        
        try (java.util.stream.Stream<java.nio.file.Path> paths = java.nio.file.Files.walk(path)) {
            paths.filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".json"))
                .forEach(jsonPath -> {
                    String relativePath = jsonPath.toString().replace('\\', '/');
                    // Extract relative path from classpath root
                    int prefixIndex = relativePath.indexOf(prefix);
                    if (prefixIndex >= 0) {
                        String filePath = relativePath.substring(prefixIndex);
                        AbloomMod.LOGGER.debug("Found elemental weapon in directory: {}", filePath);
                        jsonFiles.add(filePath);
                    }
                });
        } catch (IOException e) {
            AbloomMod.LOGGER.warn("Failed to scan directory for elemental weapons: {}", dirPath, e);
        }
    }
    
    /**
     * Fallback метод: пробуем найти ресурсы напрямую через classLoader
     */
    private static void scanFallbackResources(ClassLoader classLoader, String prefix, List<String> jsonFiles) throws IOException {
        // Попробуем загрузить файлы напрямую
        String[] knownFiles = {
            "bow.json", "crossbow.json", "diamond_axe.json", "diamond_sword.json",
            "earth_stick.json", "electric_stick.json", "energy_stick.json", "ether_stick.json",
            "fire_stick.json", "golden_axe.json", "golden_sword.json", "ice_stick.json",
            "iron_axe.json", "iron_sword.json", "light_stick.json", "mace.json",
            "natural_stick.json", "netherite_axe.json", "netherite_sword.json", "physical_stick.json",
            "quantum_stick.json", "shadow_stick.json", "stone_axe.json", "stone_sword.json",
            "trident.json", "water_stick.json", "wind_stick.json", "wooden_axe.json", "wooden_sword.json"
        };
        
        for (String fileName : knownFiles) {
            String fullPath = prefix + fileName;
            try (InputStream is = classLoader.getResourceAsStream(fullPath)) {
                if (is != null) {
                    AbloomMod.LOGGER.debug("Found elemental weapon via fallback: {}", fullPath);
                    jsonFiles.add(fullPath);
                }
            }
        }
    }
    
    /**
     * Загрузка одного weapon из JSON контента
     */
    private static void loadWeaponFromJson(String sourcePath, String jsonContent, AtomicInteger loadedCount) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();
            
            // Десериализуем
            String item = GsonHelper.getAsString(jsonObject, "item");
            String element = GsonHelper.getAsString(jsonObject, "element");
            float accumMultiplier = GsonHelper.getAsFloat(jsonObject, "accumulation_multiplier", 1.0f);
            
            // Парсим ResourceLocation
            ResourceLocation itemLocation = ResourceLocation.parse(item);
            
            // Валидируем элемент
            ElementType elementType = ElementType.safeValueOf(element);
            if (elementType == null) {
                AbloomMod.LOGGER.warn("Invalid element type in {}: {}", sourcePath, element);
                return;
            }
            
            // Проверяем дубликаты
            if (ElementalWeaponRegistry.isBuiltinRegistered(itemLocation)) {
                AbloomMod.LOGGER.warn("Duplicate builtin registration for {} in {}, skipping", itemLocation, sourcePath);
                return;
            }
            
            // Регистрируем через datapack метод
            ElementalWeaponRegistry.registerBuiltinWeapon(itemLocation, elementType, accumMultiplier);
            loadedCount.getAndIncrement();
            AbloomMod.LOGGER.debug("Registered elemental weapon: {} -> {} (multiplier: {})", 
                itemLocation, elementType, accumMultiplier);
        } catch (Exception e) {
            AbloomMod.LOGGER.error("Failed to load elemental weapon from {}", sourcePath, e);
        }
    }
}
