package com.auranite.abloom;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.Map;

@EventBusSubscriber(modid = AbloomMod.MODID)
public class ElementalTooltipHandler {

    private static final String KEY_ELEMENT_FIRE = "elemental.tooltip.fire";
    private static final String KEY_ELEMENT_PHYSICAL = "elemental.tooltip.physical";
    private static final String KEY_ELEMENT_WIND = "elemental.tooltip.wind";
    private static final String KEY_ELEMENT_WATER = "elemental.tooltip.water";
    private static final String KEY_ELEMENT_EARTH = "elemental.tooltip.earth";
    private static final String KEY_ELEMENT_ICE = "elemental.tooltip.ice";
    private static final String KEY_ELEMENT_ELECTRIC = "elemental.tooltip.electric";
    private static final String KEY_ELEMENT_ENERGY = "elemental.tooltip.energy";
    private static final String KEY_ELEMENT_NATURAL = "elemental.tooltip.natural";
    private static final String KEY_ELEMENT_QUANTUM = "elemental.tooltip.quantum";
    private static final String KEY_ELEMENT_DEFAULT = "elemental.tooltip.element";
    private static final String KEY_ACCUM_MULTIPLIER = "elemental.tooltip.accum_multiplier";

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

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        handleWeaponTooltip(stack, event);

        handleResistanceTooltip(stack, event);
    }

    private static void handleWeaponTooltip(ItemStack stack, ItemTooltipEvent event) {
        ElementType type = ElementalWeaponUtils.getElementType(stack);
        float accumMultiplier = ElementalWeaponUtils.getAccumulationMultiplier(stack);

        if (type != null && accumMultiplier != 0.0f && accumMultiplier != 1.0f) {

            MutableComponent elementText = getElementText(type);
            event.getToolTip().add(1, elementText);

            MutableComponent accumText = Component.translatable(
                    KEY_ACCUM_MULTIPLIER,
                    String.format("%.1f", accumMultiplier)
            );
            accumText.setStyle(accumText.getStyle().withColor(0x00AA00));
            event.getToolTip().add(Component.literal(" ").append(accumText));
        }
    }

    private static MutableComponent getElementText(ElementType type) {
        MutableComponent text = switch (type) {
            case FIRE -> Component.translatable(KEY_ELEMENT_FIRE);
            case PHYSICAL -> Component.translatable(KEY_ELEMENT_PHYSICAL);
            case WIND -> Component.translatable(KEY_ELEMENT_WIND);
            case WATER -> Component.translatable(KEY_ELEMENT_WATER);
            case EARTH -> Component.translatable(KEY_ELEMENT_EARTH);
            case ICE -> Component.translatable(KEY_ELEMENT_ICE);
            case ELECTRIC -> Component.translatable(KEY_ELEMENT_ELECTRIC);
            case ENERGY -> Component.translatable(KEY_ELEMENT_ENERGY);
            case NATURAL -> Component.translatable(KEY_ELEMENT_NATURAL);
            case QUANTUM -> Component.translatable(KEY_ELEMENT_QUANTUM);
            default -> Component.translatable(KEY_ELEMENT_DEFAULT, type.name());
        };
        text.setStyle(text.getStyle().withColor(getElementColor(type)));
        return text;
    }

    private static void handleResistanceTooltip(ItemStack stack, ItemTooltipEvent event) {
        if (!ElementalResistanceComponent.hasResistance(stack)) {
            return;
        }

        Map<ElementType, Float> resistances = ElementalResistanceComponent.getAllResistances(stack);
        if (resistances.isEmpty()) return;

        MutableComponent headerText = Component.translatable(KEY_RESISTANCE_HEADER);
        headerText.setStyle(headerText.getStyle().withColor(0xAAAAAA));
        event.getToolTip().add(headerText);

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

        int percentage = Math.round(resistance * 100);
        MutableComponent percentageText = Component.literal(" +" + percentage + "%");
        percentageText.setStyle(percentageText.getStyle().withColor(0x00FF00));

        text.setStyle(text.getStyle().withColor(getElementColor(type)));
        return text.append(percentageText);
    }
}
