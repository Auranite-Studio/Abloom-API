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

    private static final String KEY_ACCUM_POINTS = "elemental.tooltip.accum_points";
    private static final String KEY_RESISTANCE_HEADER = "elemental.resistance.header";

    private static int getElementColor(ElementType type) {
        if (type == null) return 0xFFFFFF;
        return type.getColor();
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        handleWeaponTooltip(stack, event);

        handleResistanceTooltip(stack, event);
    }

    private static void handleWeaponTooltip(ItemStack stack, ItemTooltipEvent event) {
        ElementType type = ElementalWeaponUtils.getElementType(stack);
        float accumPoints = ElementalWeaponUtils.getAccumulationMultiplier(stack);

        if (type != null && accumPoints != 0.0f && accumPoints != 1.0f) {

            MutableComponent elementText = getElementText(type);
            event.getToolTip().add(1, elementText);

            MutableComponent accumText = Component.translatable(
                    KEY_ACCUM_POINTS,
                    String.format("%.1f", accumPoints)
            );
            accumText.setStyle(accumText.getStyle().withColor(0x00AA00));
            event.getToolTip().add(Component.literal(" ").append(accumText));
        }
    }

    private static MutableComponent getElementText(ElementType type) {
        if (type == null) {
            return Component.translatable("elemental.tooltip.element");
        }
        MutableComponent text = Component.translatable(type.getElementItemTooltip());
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
        if (type == null) {
            return Component.translatable("elemental.resistance.element");
        }
        MutableComponent text = Component.translatable(type.getElementArmorResistanceTooltip());

        int percentage = Math.round(resistance * 100);
        MutableComponent percentageText = Component.literal(" +" + percentage + "%");
        percentageText.setStyle(percentageText.getStyle().withColor(0x00FF00));

        text.setStyle(text.getStyle().withColor(getElementColor(type)));
        return text.append(percentageText);
    }
}
