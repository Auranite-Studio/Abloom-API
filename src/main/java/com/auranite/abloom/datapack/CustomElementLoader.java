package com.auranite.abloom.datapack;

import com.auranite.abloom.Abloom;
import com.auranite.abloom.element.ElementType;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Загрузчик кастомных элементов из датапаков.
 * Читает JSON файлы из data/<modid>/custom_elements/
 */
public class CustomElementLoader extends SimpleJsonResourceReloadListener {
    private static final String FOLDER = "custom_elements";
    
    // Временное хранилище данных перед регистрацией
    private final Map<ResourceLocation, CustomElementData> pendingElements = new HashMap<>();

    public CustomElementLoader() {
        super(JsonParser::parseJsonObject, FOLDER);
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonObject> objects, 
                         @NotNull ResourceManager resourceManager, 
                         @NotNull ProfilerFiller profiler) {
        pendingElements.clear();
        
        objects.forEach((location, json) -> {
            try {
                CustomElementData data = parseElement(location, json);
                if (data != null) {
                    pendingElements.put(location, data);
                    Abloom.LOGGER.info("Загружен кастомный элемент: {}", location);
                }
            } catch (Exception e) {
                Abloom.LOGGER.error("Ошибка при загрузке элемента из {}: {}", location, e.getMessage());
            }
        });
    }

    private CustomElementData parseElement(ResourceLocation location, JsonObject json) {
        String elementId = json.has("element_id") ? json.get("element_id").getAsString() : location.getPath().toUpperCase();
        String damageType = json.has("damage_type") ? json.get("damage_type").getAsString() : location.toString();
        int color = json.has("color") ? json.get("color").getAsInt() : 0xFFFFFF;
        
        ElementType baseElement = ElementType.PHYSICAL;
        if (json.has("based_elemental_dmg")) {
            String baseId = json.get("based_elemental_dmg").getAsString();
            baseElement = ElementType.byId(baseId).orElse(ElementType.PHYSICAL);
        }
        
        String translationKey = json.has("element_translation_key") 
            ? json.get("element_translation_key").getAsString() 
            : "element." + location.getNamespace() + "." + location.getPath();
            
        String resonanceTranslationKey = json.has("resonance_translation_key")
            ? json.get("resonance_translation_key").getAsString()
            : "resonance_text." + location.getNamespace() + "." + location.getPath();
            
        boolean canAccumulate = !json.has("can_resonance_accumulation") || json.get("can_resonance_accumulation").getAsBoolean();
        
        ResonanceEffectData effectData = null;
        if (json.has("effect")) {
            JsonObject effectJson = json.getAsJsonObject("effect");
            String effectType = effectJson.has("type") ? effectJson.get("type").getAsString() : "";
            int duration = 100;
            int amplifier = 0;
            
            if (effectJson.has("config")) {
                JsonObject config = effectJson.getAsJsonObject("config");
                duration = config.has("duration") ? config.get("duration").getAsInt() : 100;
                amplifier = config.has("amplifier") ? config.get("amplifier").getAsInt() : 0;
            }
            
            effectData = new ResonanceEffectData(effectType, duration, amplifier);
        }
        
        return new CustomElementData(
            location.getNamespace(),
            elementId,
            damageType,
            color,
            baseElement,
            translationKey,
            resonanceTranslationKey,
            canAccumulate,
            effectData
        );
    }

    /**
     * Регистрирует все загруженные элементы после завершения загрузки датапаков.
     * Вызывается один раз при синхронизации с клиентом или начале игры.
     */
    public void registerElements() {
        pendingElements.forEach((location, data) -> {
            try {
                ElementType.registerCustom(
                    data.namespace,
                    data.elementId,
                    data.damageType,
                    data.color,
                    data.baseElement,
                    data.translationKey,
                    data.resonanceTranslationKey,
                    data.canAccumulate,
                    data.effect
                );
                Abloom.LOGGER.info("Зарегистрирован кастомный элемент: {}.{}", data.namespace, data.elementId);
            } catch (Exception e) {
                Abloom.LOGGER.error("Не удалось зарегистрировать элемент {}: {}", location, e.getMessage());
            }
        });
        pendingElements.clear();
    }

    public record CustomElementData(
        String namespace,
        String elementId,
        String damageType,
        int color,
        ElementType baseElement,
        String translationKey,
        String resonanceTranslationKey,
        boolean canAccumulate,
        ResonanceEffectData effect
    ) {}

    public record ResonanceEffectData(
        String type,
        int duration,
        int amplifier
    ) {}
}
