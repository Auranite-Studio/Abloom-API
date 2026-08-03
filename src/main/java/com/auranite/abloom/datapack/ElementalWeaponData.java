package com.auranite.abloom.datapack;

import com.auranite.abloom.ElementType;
import com.auranite.abloom.AbloomMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.Optional;

public class ElementalWeaponData {

    private final String item;
    private final String element;
    private final float accumulationMultiplier;
    private final float critChance;
    private final float critDamage;

    public ElementalWeaponData(String item, String element, float accumulationMultiplier, float critChance, float critDamage) {
        this.item = item;
        this.element = element;
        this.accumulationMultiplier = accumulationMultiplier;
        this.critChance = critChance;
        this.critDamage = critDamage;
    }

    public String getItem() {
        return item;
    }

    public String getElement() {
        return element;
    }

    public float getAccumulationMultiplier() {
        return accumulationMultiplier;
    }

    public float getCritChance() {
        return critChance;
    }

    public float getCritDamage() {
        return critDamage;
    }

    public Optional<ResourceLocation> getItemResourceLocation() {
        try {
            return Optional.of(ResourceLocation.parse(item));
        } catch (Exception e) {
            AbloomMod.LOGGER.warn("Invalid item registry name: {}", item, e);
            return Optional.empty();
        }
    }

    public Optional<ElementType> getElementType() {
        ElementType result = ElementType.safeValueOf(element);
        return result != null ? Optional.of(result) : Optional.empty();
    }

    public static ElementalWeaponData fromJson(com.google.gson.JsonObject json) {
        String item = GsonHelper.getAsString(json, "item");
        String element = GsonHelper.getAsString(json, "element");
        float accumulationMultiplier = GsonHelper.getAsFloat(json, "accumulation_multiplier", 1.0f);
        float critChance = GsonHelper.getAsFloat(json, "crit_chance", 0.0f);
        float critDamage = GsonHelper.getAsFloat(json, "crit_damage", 0.0f);

        return new ElementalWeaponData(item, element, accumulationMultiplier, critChance, critDamage);
    }
}
