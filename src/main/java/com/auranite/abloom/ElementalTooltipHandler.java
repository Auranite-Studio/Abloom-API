package com.auranite.abloom;

import com.auranite.abloom.datapack.ElementalWeaponData;
import com.auranite.abloom.datapack.ElementalWeaponProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
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
    private static final String KEY_ELEMENT_ETHER = "elemental.tooltip.ether";
    private static final String KEY_ELEMENT_LIGHT = "elemental.tooltip.light";
    private static final String KEY_ELEMENT_SHADOW = "elemental.tooltip.shadow";
    private static final String KEY_ELEMENT_PRISMATIC = "elemental.tooltip.prismatic";
    private static final String KEY_ELEMENT_DEFAULT = "elemental.tooltip.element";
    private static final String KEY_ACCUM_POINTS = "elemental.tooltip.accum_points";
    private static final String KEY_CRIT_CHANCE = "elemental.tooltip.crit_chance";
    private static final String KEY_CRIT_DAMAGE = "elemental.tooltip.crit_damage";

    private static final String KEY_ATTACK_STAGES = "elemental.tooltip.attack_stages";
    private static final String KEY_ATTACK_STAGES_COUNT = "elemental.tooltip.attack_stages_count";
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
    private static final String KEY_RESISTANCE_ETHER = "elemental.resistance.ether";
    private static final String KEY_RESISTANCE_LIGHT = "elemental.resistance.light";
    private static final String KEY_RESISTANCE_SHADOW = "elemental.resistance.shadow";
    private static final String KEY_RESISTANCE_PRISMATIC = "elemental.resistance.prismatic";
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
            case ETHER -> 0x24B3A7;
            case LIGHT -> 0xFFF1A5;
            case SHADOW -> 0x4B0082;
            case PRISMATIC -> ElementDamageDisplayManager.getDamageColor(ElementType.PRISMATIC);
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
        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

        // Show attack stages count if weapon has stages
        if (ElementalWeaponRegistry.hasStages(itemId)) {
            List<ElementalWeaponRegistry.StageData> stages = ElementalWeaponRegistry.getStages(itemId);
            MutableComponent stagesText = Component.translatable(
                    KEY_ATTACK_STAGES_COUNT,
                    stages.size()
            );
            stagesText.setStyle(stagesText.getStyle().withColor(0x00AA00));
            event.getToolTip().add(Component.literal(" ").append(stagesText));

        }

        ElementType type = ElementalWeaponUtils.getElementType(stack);
        float accumPoints = ElementalWeaponUtils.getAccumulationMultiplier(stack);
        float weaponCritChance = ElementalWeaponUtils.getCritChance(stack);
        float weaponCritDamage = ElementalWeaponUtils.getCritDamage(stack);

        if (type == ElementType.PRISMATIC || accumPoints > 1.0f) {
            MutableComponent elementText = getElementText(type);
            event.getToolTip().add(1, elementText);
        }

        // Get base and modified attribute values from the player/entity
        double entityBonusCritChance = 0.0;
        double entityBonusCritDamage = 0.0;
        var player = event.getEntity();
        if (player != null && player instanceof LivingEntity livingEntity) {
            var critChanceHolder = AbloomAttributes.CRIT_CHANCE.getKey();
            var critDamageHolder = AbloomAttributes.CRIT_DMG.getKey();
            var critChanceAttr = livingEntity.getAttribute(BuiltInRegistries.ATTRIBUTE.getOrThrow(critChanceHolder));
            var critDamageAttr = livingEntity.getAttribute(BuiltInRegistries.ATTRIBUTE.getOrThrow(critDamageHolder));
            if (critChanceAttr != null) {
                double base = critChanceAttr.getBaseValue();
                double current = critChanceAttr.getValue();
                entityBonusCritChance = current - base;
            }
            if (critDamageAttr != null) {
                double base = critDamageAttr.getBaseValue();
                double current = critDamageAttr.getValue();
                entityBonusCritDamage = current - base;
            }
        }

        if (weaponCritChance > 0.0f || weaponCritDamage > 0.0f) {
            if (weaponCritChance > 0.0f) {
                int weaponPercent = Math.round(weaponCritChance * 100);
                int entityPercent = (int) Math.round(entityBonusCritChance * 100);
                String entityStr = entityPercent != 0 ? " (" + (entityPercent > 0 ? "+" : "") + entityPercent + "%)" : "";
                MutableComponent critChanceText = Component.translatable(
                        KEY_CRIT_CHANCE,
                        weaponPercent + "%" + entityStr
                );
                critChanceText.setStyle(critChanceText.getStyle().withColor(0x00AA00));
                event.getToolTip().add(Component.literal(" ").append(critChanceText));
            }
            if (weaponCritDamage > 0.0f) {
                int weaponPercent = Math.round(weaponCritDamage * 100);
                int entityPercent = (int) Math.round(entityBonusCritDamage * 100);
                String entityStr = entityPercent != 0 ? " (" + (entityPercent > 0 ? "+" : "") + entityPercent + "%)" : "";
                MutableComponent critDamageText = Component.translatable(
                        KEY_CRIT_DAMAGE,
                        weaponPercent + "%" + entityStr
                );
                critDamageText.setStyle(critDamageText.getStyle().withColor(0x00AA00));
                event.getToolTip().add(Component.literal(" ").append(critDamageText));
            }
        }

        if (accumPoints > 1.0f && type != ElementType.PRISMATIC ) {
            MutableComponent accumText = Component.translatable(
                    KEY_ACCUM_POINTS,
                    String.format("%d", Math.round(accumPoints))
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
            case ETHER -> Component.translatable(KEY_ELEMENT_ETHER);
            case LIGHT -> Component.translatable(KEY_ELEMENT_LIGHT);
            case SHADOW -> Component.translatable(KEY_ELEMENT_SHADOW);
            case PRISMATIC -> Component.translatable(KEY_ELEMENT_PRISMATIC);
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

            if (resistance != 0.0f) {
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
            case ETHER -> Component.translatable(KEY_RESISTANCE_ETHER);
            case LIGHT -> Component.translatable(KEY_RESISTANCE_LIGHT);
            case SHADOW -> Component.translatable(KEY_RESISTANCE_SHADOW);
            case PRISMATIC -> Component.translatable(KEY_RESISTANCE_PRISMATIC);
            default -> Component.translatable(KEY_RESISTANCE_DEFAULT, type.getDisplayName());
        };

        int percentage = Math.round(resistance * 100);
        String sign = percentage > 0 ? "+" : "";
        MutableComponent percentageText = Component.literal(" " + sign + percentage + "%");
        
        int color = percentage >= 0 ? 0x00FF00 : 0xFF0000;
        percentageText.setStyle(percentageText.getStyle().withColor(color));

        text.setStyle(text.getStyle().withColor(getElementColor(type)));
        return text.append(percentageText);
    }
}
