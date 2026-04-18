package com.auranite.abloom;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.EnumMap;
import java.util.Map;

/**
 * Компонент данных для добавления сопротивления к элементальным типам урона на броне.
 * Сопротивление хранится как карта ElementType -> float (значение от 0.0 до 1.0)
 */
public class ElementalResistanceComponent {

    public static final String ELEMENT_RESISTANCE_KEY = "elemental_resistance_bonus";
    public static final String RESISTANCE_ENTRY_KEY = "resistance_entry";
    public static final String ELEMENT_TYPE_KEY = "element_type";
    public static final String RESISTANCE_VALUE_KEY = "resistance_value";

    /**
     * Добавляет сопротивление к указанному элементальному типу на предмет брони.
     * @param stack ItemStack брони
     * @param type Тип элемента
     * @param resistance Значение сопротивления (0.0 - 1.0, где 1.0 = 100% сопротивление)
     * @return модифицированный ItemStack
     */
    public static ItemStack withResistance(ItemStack stack, ElementType type, float resistance) {
        if (stack == null || stack.isEmpty() || type == null) return stack;
        
        // Ограничиваем значение сопротивления диапазоном [0.0, 1.0]
        resistance = Math.max(0.0f, Math.min(1.0f, resistance));
        
        final ElementType finalType = type;
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        customData.update(tag -> {
            // Получаем или создаем compound тег для сопротивлений
            var resistanceTag = tag.getCompound(ELEMENT_RESISTANCE_KEY);
            // Сохраняем сопротивление для конкретного типа элемента
            resistanceTag.putFloat(finalType.name(), resistance);
            tag.put(ELEMENT_RESISTANCE_KEY, resistanceTag);
        });
        stack.set(DataComponents.CUSTOM_DATA, customData);
        
        return stack;
    }
    
    /**
     * Добавляет множественные сопротивления к предмету брони.
     * @param stack ItemStack брони
     * @param resistanceMap Карта ElementType -> resistance value
     * @return модифицированный ItemStack
     */
    public static ItemStack withResistances(ItemStack stack, Map<ElementType, Float> resistanceMap) {
        if (stack == null || stack.isEmpty() || resistanceMap == null) return stack;
        
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        customData.update(tag -> {
            var resistanceTag = tag.getCompound(ELEMENT_RESISTANCE_KEY);
            for (Map.Entry<ElementType, Float> entry : resistanceMap.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    float clampedValue = Math.max(0.0f, Math.min(1.0f, entry.getValue()));
                    resistanceTag.putFloat(entry.getKey().name(), clampedValue);
                }
            }
            tag.put(ELEMENT_RESISTANCE_KEY, resistanceTag);
        });
        stack.set(DataComponents.CUSTOM_DATA, customData);
        
        return stack;
    }
    
    /**
     * Получает сопротивление к указанному элементальному типу с предмета брони.
     * @param stack ItemStack брони
     * @param type Тип элемента
     * @return значение сопротивления или 0.0 если не найдено
     */
    public static float getResistance(ItemStack stack, ElementType type) {
        if (stack == null || stack.isEmpty() || type == null) return 0.0f;
        
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return 0.0f;
        
        var tag = customData.copyTag();
        if (!tag.contains(ELEMENT_RESISTANCE_KEY)) return 0.0f;
        
        var resistanceTag = tag.getCompound(ELEMENT_RESISTANCE_KEY);
        return resistanceTag.getFloat(type.name());
    }
    
    /**
     * Получает все сопротивления с предмета брони.
     * @param stack ItemStack брони
     * @return Map ElementType -> resistance value (может быть пустой)
     */
    public static Map<ElementType, Float> getAllResistances(ItemStack stack) {
        Map<ElementType, Float> result = new EnumMap<>(ElementType.class);
        
        if (stack == null || stack.isEmpty()) return result;
        
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return result;
        
        var tag = customData.copyTag();
        if (!tag.contains(ELEMENT_RESISTANCE_KEY)) return result;
        
        var resistanceTag = tag.getCompound(ELEMENT_RESISTANCE_KEY);
        
        for (ElementType type : ElementType.values()) {
            if (resistanceTag.contains(type.name())) {
                result.put(type, resistanceTag.getFloat(type.name()));
            }
        }
        
        return result;
    }
    
    /**
     * Проверяет, имеет ли предмет какие-либо сопротивления.
     * @param stack ItemStack брони
     * @return true если есть хотя бы одно сопротивление > 0
     */
    public static boolean hasResistance(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return false;
        
        var tag = customData.copyTag();
        if (!tag.contains(ELEMENT_RESISTANCE_KEY)) return false;
        
        var resistanceTag = tag.getCompound(ELEMENT_RESISTANCE_KEY);
        
        for (ElementType type : ElementType.values()) {
            if (resistanceTag.contains(type.name()) && resistanceTag.getFloat(type.name()) > 0.0f) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Проверяет, имеет ли предмет сопротивление к конкретному типу элемента.
     * @param stack ItemStack брони
     * @param type Тип элемента
     * @return true если сопротивление > 0
     */
    public static boolean hasResistance(ItemStack stack, ElementType type) {
        return getResistance(stack, type) > 0.0f;
    }
    
    /**
     * Удаляет все сопротивления с предмета.
     * @param stack ItemStack брони
     * @return модифицированный ItemStack
     */
    public static ItemStack removeResistance(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return stack;
        
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            customData.update(tag -> {
                tag.remove(ELEMENT_RESISTANCE_KEY);
            });
            stack.set(DataComponents.CUSTOM_DATA, customData);
        }
        
        return stack;
    }
    
    /**
     * Удаляет сопротивление к конкретному типу элемента.
     * @param stack ItemStack брони
     * @param type Тип элемента
     * @return модифицированный ItemStack
     */
    public static ItemStack removeResistance(ItemStack stack, ElementType type) {
        if (stack == null || stack.isEmpty() || type == null) return stack;
        
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            customData.update(tag -> {
                if (tag.contains(ELEMENT_RESISTANCE_KEY)) {
                    var resistanceTag = tag.getCompound(ELEMENT_RESISTANCE_KEY);
                    resistanceTag.remove(type.name());
                    tag.put(ELEMENT_RESISTANCE_KEY, resistanceTag);
                }
            });
            stack.set(DataComponents.CUSTOM_DATA, customData);
        }
        
        return stack;
    }
}
