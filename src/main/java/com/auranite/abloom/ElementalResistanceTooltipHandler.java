package com.auranite.abloom;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.Map;

/**
 * Обработчик подсказок для отображения сопротивлений элементальному урону на броне.
 */
@EventBusSubscriber(modid = AbloomMod.MODID)
public class ElementalResistanceTooltipHandler {

    private static final String KEY_RESISTANCE_HEADER = "elemental.resistance.header";
    private static final String KEY_RESISTANCE_FIRE = "elemental.resistance.fire";
    private static final String KEY_RESISTANCE_PHYSICAL = "elemental.resistance.physical";
    private static final String KEY_RESISTANCE_WIND = "elemental.resistance.wind";
    private static final String KEY_RESISTANCE_WATER = "elemental.resistance.water";
    private static final String KEY_RESISTANCE_EARTH = "elemental.resistance.earth";
    private static final String KEY_RESISTANCE_ICE = "elemental.resistance.ice";
    private static final String KEY_RESISTANCE_ELECTRIC = "elemental.resistance.electric";
    private static final String KEY_RESISTANCE_ENERGY = "elemental.resistance.energy";
    private static final String KEY_RESISTANCE_NATURAL = "elemental.resistance.natural";
    private static final String KEY_RESISTANCE_QUANTUM = "elemental.resistance.quantum";
    private static final String KEY_RESISTANCE_DEFAULT = "elemental.resistance.element";

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        // Проверяем, есть ли у предмета сопротивления
        if (!ElementalResistanceComponent.hasResistance(stack)) {
            return;
        }

        Map<ElementType, Float> resistances = ElementalResistanceComponent.getAllResistances(stack);

        // Добавляем заголовок
        MutableComponent headerText = Component.translatable(KEY_RESISTANCE_HEADER);
        headerText.setStyle(headerText.getStyle().withColor(0xAAAAAA));
        event.getToolTip().add(headerText);

        // Добавляем каждое сопротивление
        for (Map.Entry<ElementType, Float> entry : resistances.entrySet()) {
            ElementType type = entry.getKey();
            float resistance = entry.getValue();

            if (resistance > 0.0f) {
                MutableComponent resistanceText = getResistanceText(type, resistance);
                event.getToolTip().add(resistanceText);
            }
        }
    }

    private static MutableComponent getResistanceText(ElementType type, float resistance) {
        MutableComponent text = switch (type) {
            case FIRE -> Component.translatable(KEY_RESISTANCE_FIRE);
            case PHYSICAL -> Component.translatable(KEY_RESISTANCE_PHYSICAL);
            case WIND -> Component.translatable(KEY_RESISTANCE_WIND);
            case WATER -> Component.translatable(KEY_RESISTANCE_WATER);
            case EARTH -> Component.translatable(KEY_RESISTANCE_EARTH);
            case ICE -> Component.translatable(KEY_RESISTANCE_ICE);
            case ELECTRIC -> Component.translatable(KEY_RESISTANCE_ELECTRIC);
            case ENERGY -> Component.translatable(KEY_RESISTANCE_ENERGY);
            case NATURAL -> Component.translatable(KEY_RESISTANCE_NATURAL);
            case QUANTUM -> Component.translatable(KEY_RESISTANCE_QUANTUM);
            default -> Component.translatable(KEY_RESISTANCE_DEFAULT, type.getDisplayName());
        };

        // Форматируем процент сопротивления
        int percentage = Math.round(resistance * 100);
        MutableComponent percentageText = Component.literal(" +" + percentage + "%");
        percentageText.setStyle(percentageText.getStyle().withColor(0x00FF00));

        text.setStyle(text.getStyle().withColor(getElementColor(type)));
        return text.append(percentageText);
    }

    private static int getElementColor(ElementType type) {
        return switch (type) {
            case FIRE -> 0xFF5500;
            case PHYSICAL -> 0xC0C0C0;
            case WIND -> 0x00FFFF;
            case WATER -> 0x0080FF;
            case EARTH -> 0x8B4513;
            case ICE -> 0x00BFFF;
            case ELECTRIC -> 0xFF19FF;
            case ENERGY -> 0xFFFF00;
            case NATURAL -> 0x32CD32;
            case QUANTUM -> 0x9400D3;
            default -> 0xFFFFFF;
        };
    }
}
