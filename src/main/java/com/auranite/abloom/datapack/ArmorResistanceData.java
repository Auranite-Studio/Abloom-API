package com.auranite.abloom.datapack;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.ElementType;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class ArmorResistanceData {

    private final String item;
    private final Map<ElementType, Float> resistances;

    public ArmorResistanceData(String item, Map<ElementType, Float> resistances) {
        this.item = item;
        this.resistances = resistances != null ? resistances : new EnumMap<>(ElementType.class);
    }

    public String getItem() {
        return item;
    }

    public Map<ElementType, Float> getResistances() {
        return resistances;
    }

    public Optional<Identifier> getItemResourceLocation() {
        try {
            return Optional.of(Identifier.parse(item));
        } catch (Exception e) {
            AbloomMod.LOGGER.warn("Invalid item registry name: {}", item, e);
            return Optional.empty();
        }
    }

    public static ArmorResistanceData fromJson(JsonObject json) {
        String item = GsonHelper.getAsString(json, "item");
        
        Map<ElementType, Float> resistances = new EnumMap<>(ElementType.class);
        if (json.has("resistances")) {
            JsonObject resistancesObj = GsonHelper.getAsJsonObject(json, "resistances");
            for (String key : resistancesObj.keySet()) {
                ElementType elementType = ElementType.safeValueOf(key.toUpperCase());
                if (elementType != null) {
                    float value = GsonHelper.getAsFloat(resistancesObj, key);
                    resistances.put(elementType, value);
                } else {
                    AbloomMod.LOGGER.warn("Invalid element type: {} in resistances", key);
                }
            }
        }

        return new ArmorResistanceData(item, resistances);
    }
}
