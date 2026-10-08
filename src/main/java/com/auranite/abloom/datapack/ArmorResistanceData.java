package com.auranite.abloom.datapack;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.util.IElementalType;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ArmorResistanceData {

    private final String item;
    private final Map<IElementalType, Float> resistances;

    public ArmorResistanceData(String item, Map<IElementalType, Float> resistances) {
        this.item = item;
        this.resistances = resistances != null ? resistances : new HashMap<>();
    }

    public String getItem() {
        return item;
    }

    public Map<IElementalType, Float> getResistances() {
        return resistances;
    }

    public Optional<ResourceLocation> getItemResourceLocation() {
        try {
            return Optional.of(ResourceLocation.parse(item));
        } catch (Exception e) {
            AbloomMod.LOGGER.warn("Invalid item registry name: {}", item, e);
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public static ArmorResistanceData fromJson(JsonObject json) {
        String item = GsonHelper.getAsString(json, "item");

        Map<IElementalType, Float> resistances = new HashMap<>();
        if (json.has("resistances")) {
            JsonObject resistancesObj = GsonHelper.getAsJsonObject(json, "resistances");
            for (String key : resistancesObj.keySet()) {
                Optional<? extends IElementalType> elementType = IElementalType.byName(key.toUpperCase());
                if (elementType.isPresent()) {
                    float value = GsonHelper.getAsFloat(resistancesObj, key);
                    resistances.put((IElementalType) elementType.get(), value);
                } else {
                    AbloomMod.LOGGER.warn("Invalid element type: {} in resistances", key);
                }
            }
        }

        return new ArmorResistanceData(item, resistances);
    }
}
