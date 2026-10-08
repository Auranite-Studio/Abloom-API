package com.auranite.abloom.handler;

import com.auranite.abloom.*;
import com.auranite.abloom.component.ElementalResistanceComponent;
import com.auranite.abloom.handler.ElementDamageHandler;
import com.auranite.abloom.registries.ElementalWeaponRegistry;
import com.auranite.abloom.init.AbloomModAttributes;
import com.auranite.abloom.util.IElementalType;
import com.auranite.abloom.util.ElementalWeaponUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.HashMap;
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
    private static final String KEY_RESONANCE_FREQUENCY = "elemental.tooltip.resonance_frequency";

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

    // Built-in element color map
    private static final Map<String, Integer> BUILTIN_ELEMENT_COLORS;
    static {
        Map<String, Integer> map = new HashMap<>();
        map.put("FIRE", 0xFF5500);
        map.put("PHYSICAL", 0xC0C0C0);
        map.put("WIND", 0x00FFFF);
        map.put("WATER", 0x0080FF);
        map.put("EARTH", 0x8B4513);
        map.put("ICE", 0x00BFFF);
        map.put("ELECTRIC", 0xFF19FF);
        map.put("ENERGY", 0xFFFF00);
        map.put("NATURAL", 0x32CD32);
        map.put("QUANTUM", 0x9400D3);
        map.put("ETHER", 0x24B3A7);
        map.put("LIGHT", 0xFFF1A5);
        map.put("SHADOW", 0x4B0082);
        map.put("PRISMATIC", 0xFFFFFF);
        BUILTIN_ELEMENT_COLORS = Map.copyOf(map);
    }

    private static int getElementColor(IElementalType type) {
        Integer color = BUILTIN_ELEMENT_COLORS.get(type.name());
        return color != null ? color : 0xFFFFFF;
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        handleWeaponTooltip(stack, event);

        handleResistanceTooltip(stack, event);
    }

    private static void handleWeaponTooltip(ItemStack stack, ItemTooltipEvent event) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

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

        IElementalType type = ElementDamageHandler.getEffectiveElementType(stack);
        float accumPoints = ElementalWeaponUtils.getAccumulationMultiplier(stack);
        float weaponCritChance = ElementalWeaponUtils.getCritChance(stack);
        float weaponCritDamage = ElementalWeaponUtils.getCritDamage(stack);
        float resonanceFrequency = ElementalWeaponUtils.getResonanceFrequency(stack);

        if (type != null && (type.name().equals("PRISMATIC") || accumPoints > 1.0f)) {
            MutableComponent elementText = getElementText(type);
            event.getToolTip().add(1, elementText);
        }

        // Get base and modified attribute values from the player/entity
        double entityBonusCritChance = 0.0;
        double entityBonusCritDamage = 0.0;
        var player = event.getEntity();
        if (player != null && player instanceof LivingEntity livingEntity) {
            var critChanceHolder = AbloomModAttributes.CRIT_CHANCE.getKey();
            var critDamageHolder = AbloomModAttributes.CRIT_DMG.getKey();
            var critChanceAttr = livingEntity.getAttribute(BuiltInRegistries.ATTRIBUTE.getHolderOrThrow(critChanceHolder));
            var critDamageAttr = livingEntity.getAttribute(BuiltInRegistries.ATTRIBUTE.getHolderOrThrow(critDamageHolder));
            if (critChanceAttr != null) {
                entityBonusCritChance = critChanceAttr.getValue();
            }
            if (critDamageAttr != null) {
                entityBonusCritDamage = critDamageAttr.getValue();
            }
            var resonanceFreqAttr = livingEntity.getAttribute(BuiltInRegistries.ATTRIBUTE.getHolderOrThrow(AbloomModAttributes.RESONANCE_FREQUENCY_BONUS.getKey()));
            var resonanceAccumAttr = livingEntity.getAttribute(BuiltInRegistries.ATTRIBUTE.getHolderOrThrow(AbloomModAttributes.RESONANCE_ACCUMULATION_BUILDUP.getKey()));
            if (resonanceFreqAttr != null) {
                resonanceFrequency += (float) resonanceFreqAttr.getValue();
            }
            if (resonanceAccumAttr != null) {
                accumPoints *= (1.0f + (float) resonanceAccumAttr.getValue());
            }
        }

        // Sum weapon crit values with entity attribute bonuses
        double totalCritChance = weaponCritChance + entityBonusCritChance;
        double totalCritDamage = weaponCritDamage + entityBonusCritDamage;

        if (totalCritChance > 0.0 || totalCritDamage > 0.0) {
            if (totalCritChance > 0.0) {
                int totalPercent = Math.round((float) Math.min(totalCritChance, 2.0) * 100);
                MutableComponent critChanceText = Component.translatable(
                        KEY_CRIT_CHANCE,
                        totalPercent + "%"
                );
                critChanceText.setStyle(critChanceText.getStyle().withColor(0x00AA00));
                event.getToolTip().add(Component.literal(" ").append(critChanceText));
            }
            if (totalCritDamage > 0.0) {
                int totalPercent = Math.round((float) totalCritDamage * 100);
                MutableComponent critDamageText = Component.translatable(
                        KEY_CRIT_DAMAGE,
                        totalPercent + "%"
                );
                critDamageText.setStyle(critDamageText.getStyle().withColor(0x00AA00));
                event.getToolTip().add(Component.literal(" ").append(critDamageText));
            }
        }

        // Show resonance frequency if present
        if (resonanceFrequency > 0.0f && accumPoints > 1.0f && !type.name().equals("PRISMATIC")) {
            MutableComponent resonanceText = Component.translatable(
                    KEY_RESONANCE_FREQUENCY,
                    Math.round(resonanceFrequency)
            );
            resonanceText.setStyle(resonanceText.getStyle().withColor(0x00AA00));
            event.getToolTip().add(Component.literal(" ").append(resonanceText));
        }

        if (accumPoints > 1.0f && !type.name().equals("PRISMATIC") ) {
            MutableComponent accumText = Component.translatable(
                    KEY_ACCUM_POINTS,
                    String.format("%d", Math.round(accumPoints))
            );
            accumText.setStyle(accumText.getStyle().withColor(0x00AA00));
            event.getToolTip().add(Component.literal(" ").append(accumText));
        }
    }

    private static MutableComponent getElementText(IElementalType type) {
        // Check built-in types first
        String key = switch (type.name()) {
            case "FIRE" -> KEY_ELEMENT_FIRE;
            case "PHYSICAL" -> KEY_ELEMENT_PHYSICAL;
            case "WIND" -> KEY_ELEMENT_WIND;
            case "WATER" -> KEY_ELEMENT_WATER;
            case "EARTH" -> KEY_ELEMENT_EARTH;
            case "ICE" -> KEY_ELEMENT_ICE;
            case "ELECTRIC" -> KEY_ELEMENT_ELECTRIC;
            case "ENERGY" -> KEY_ELEMENT_ENERGY;
            case "NATURAL" -> KEY_ELEMENT_NATURAL;
            case "QUANTUM" -> KEY_ELEMENT_QUANTUM;
            case "ETHER" -> KEY_ELEMENT_ETHER;
            case "LIGHT" -> KEY_ELEMENT_LIGHT;
            case "SHADOW" -> KEY_ELEMENT_SHADOW;
            case "PRISMATIC" -> KEY_ELEMENT_PRISMATIC;
            default -> null;
        };

        MutableComponent text;
        if (key != null) {
            text = Component.translatable(key);
        } else {
            // Custom element type
            text = Component.translatable(KEY_ELEMENT_DEFAULT, type.getDisplayName());
        }
        text.setStyle(text.getStyle().withColor(getElementColor(type)));
        return text;
    }

    private static void handleResistanceTooltip(ItemStack stack, ItemTooltipEvent event) {
        if (!ElementalResistanceComponent.hasResistance(stack)) {
            return;
        }

        Map<IElementalType, Float> resistances = ElementalResistanceComponent.getAllResistances(stack);
        if (resistances.isEmpty()) return;

        MutableComponent headerText = Component.translatable(KEY_RESISTANCE_HEADER);
        headerText.setStyle(headerText.getStyle().withColor(0xAAAAAA));
        event.getToolTip().add(headerText);

        for (Map.Entry<IElementalType, Float> entry : resistances.entrySet()) {
            IElementalType type = entry.getKey();
            float resistance = entry.getValue();

            if (resistance != 0.0f) {
                MutableComponent resistanceText = getResistanceText(type, resistance);
                event.getToolTip().add(resistanceText);
            }
        }
    }

    private static MutableComponent getResistanceText(IElementalType type, float resistance) {
        // Check built-in types first
        String key = switch (type.name()) {
            case "FIRE" -> KEY_RESISTANCE_FIRE;
            case "PHYSICAL" -> KEY_RESISTANCE_PHYSICAL;
            case "WIND" -> KEY_RESISTANCE_WIND;
            case "WATER" -> KEY_RESISTANCE_WATER;
            case "EARTH" -> KEY_RESISTANCE_EARTH;
            case "ICE" -> KEY_RESISTANCE_ICE;
            case "ELECTRIC" -> KEY_RESISTANCE_ELECTRIC;
            case "ENERGY" -> KEY_RESISTANCE_ENERGY;
            case "NATURAL" -> KEY_RESISTANCE_NATURAL;
            case "QUANTUM" -> KEY_RESISTANCE_QUANTUM;
            case "ETHER" -> KEY_RESISTANCE_ETHER;
            case "LIGHT" -> KEY_RESISTANCE_LIGHT;
            case "SHADOW" -> KEY_RESISTANCE_SHADOW;
            case "PRISMATIC" -> KEY_RESISTANCE_PRISMATIC;
            default -> null;
        };

        MutableComponent text;
        if (key != null) {
            text = Component.translatable(key);
        } else {
            // Custom element type
            text = Component.translatable(KEY_RESISTANCE_DEFAULT, type.getDisplayName());
        }

        int percentage = Math.round(resistance * 100);
        String sign = percentage > 0 ? "+" : "";
        MutableComponent percentageText = Component.literal(" " + sign + percentage + "%");
        
        int color = percentage >= 0 ? 0x00FF00 : 0xFF0000;
        percentageText.setStyle(percentageText.getStyle().withColor(color));

        text.setStyle(text.getStyle().withColor(getElementColor(type)));
        return text.append(percentageText);
    }
}
