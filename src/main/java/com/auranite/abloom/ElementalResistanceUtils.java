package com.auranite.abloom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * Утилитный класс для регистрации сопротивлений элементальному урону на броне.
 * Аналогично ElementalWeaponUtils для оружия.
 */
public class ElementalResistanceUtils {

    private ElementalResistanceUtils() {}

    /**
     * Регистрирует сопротивления элементальному урону для предмета брони.
     * @param item Предмет брони
     * @param resistanceMap Карта ElementType -> значение сопротивления (0.0 - 1.0)
     */
    public static void registerArmor(Item item, Map<ElementType, Float> resistanceMap) {
        if (item == null || resistanceMap == null || resistanceMap.isEmpty()) return;
        
        ItemStack stack = new ItemStack(item);
        ElementalResistanceComponent.withResistances(stack, resistanceMap);
        
        AbloomMod.LOGGER.info("🛡️ Registered armor {} with elemental resistances: {}",
                item.getName(stack).getString(), resistanceMap);
    }

    /**
     * Регистрирует одно сопротивление для предмета брони.
     * @param item Предмет брони
     * @param type Тип элемента
     * @param resistance Значение сопротивления (0.0 - 1.0)
     */
    public static void registerArmor(Item item, ElementType type, float resistance) {
        if (item == null || type == null) return;
        
        ItemStack stack = new ItemStack(item);
        ElementalResistanceComponent.withResistance(stack, type, resistance);
        
        AbloomMod.LOGGER.info("🛡️ Registered armor {} with {} resistance: {}",
                item.getName(stack).getString(), type, resistance);
    }

    /**
     * Создаёт ItemStack брони с указанными сопротивлениями.
     * @param item Предмет брони
     * @param resistanceMap Карта ElementType -> значение сопротивления
     * @param count Количество предметов в стаке
     * @return ItemStack с применёнными сопротивлениями
     */
    public static ItemStack createResistantArmor(Item item, Map<ElementType, Float> resistanceMap, int count) {
        if (item == null || resistanceMap == null) return new ItemStack(item, count);
        
        ItemStack stack = new ItemStack(item, count);
        return ElementalResistanceComponent.withResistances(stack, resistanceMap);
    }

    /**
     * Создаёт ItemStack брони с одним сопротивлением.
     * @param item Предмет брони
     * @param type Тип элемента
     * @param resistance Значение сопротивления
     * @param count Количество предметов в стаке
     * @return ItemStack с применённым сопротивлением
     */
    public static ItemStack createResistantArmor(Item item, ElementType type, float resistance, int count) {
        if (item == null || type == null) return new ItemStack(item, count);
        
        ItemStack stack = new ItemStack(item, count);
        return ElementalResistanceComponent.withResistance(stack, type, resistance);
    }
}
