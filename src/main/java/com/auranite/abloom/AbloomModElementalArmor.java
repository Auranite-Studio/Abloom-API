package com.auranite.abloom;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber
public class AbloomModElementalArmor {

    private static final Map<Item, Map<ElementType, Float>> VANILLA_ARMOR_RESISTANCES = new HashMap<>();

    static {
        add(Items.NETHERITE_HELMET,     ElementType.FIRE, 0.02f, ElementType.PHYSICAL, 0.02f);
        add(Items.NETHERITE_CHESTPLATE, ElementType.FIRE, 0.03f, ElementType.PHYSICAL, 0.03f);
        add(Items.NETHERITE_LEGGINGS,   ElementType.FIRE, 0.03f, ElementType.PHYSICAL, 0.03f);
        add(Items.NETHERITE_BOOTS,      ElementType.FIRE, 0.02f, ElementType.PHYSICAL, 0.02f);

        add(Items.DIAMOND_HELMET,       ElementType.FIRE, 0.01f, ElementType.PHYSICAL, 0.01f);
        add(Items.DIAMOND_CHESTPLATE,   ElementType.FIRE, 0.02f, ElementType.PHYSICAL, 0.02f);
        add(Items.DIAMOND_LEGGINGS,     ElementType.FIRE, 0.01f, ElementType.PHYSICAL, 0.01f);
        add(Items.DIAMOND_BOOTS,        ElementType.FIRE, 0.01f, ElementType.PHYSICAL, 0.01f);

        add(Items.IRON_HELMET,          ElementType.PHYSICAL, 0.01f);
        add(Items.IRON_CHESTPLATE,      ElementType.PHYSICAL, 0.02f);
        add(Items.IRON_LEGGINGS,        ElementType.PHYSICAL, 0.01f);
        add(Items.IRON_BOOTS,           ElementType.PHYSICAL, 0.01f);

        add(Items.GOLDEN_HELMET,        ElementType.QUANTUM, 0.02f);
        add(Items.GOLDEN_CHESTPLATE,    ElementType.QUANTUM, 0.03f);
        add(Items.GOLDEN_LEGGINGS,      ElementType.QUANTUM, 0.03f);
        add(Items.GOLDEN_BOOTS,         ElementType.QUANTUM, 0.02f);

        add(Items.LEATHER_HELMET,       ElementType.ELECTRIC, 0.02f, ElementType.ICE, 0.1f);
        add(Items.LEATHER_CHESTPLATE,   ElementType.ELECTRIC, 0.03f, ElementType.ICE, 0.2f);
        add(Items.LEATHER_LEGGINGS,     ElementType.ELECTRIC, 0.03f, ElementType.ICE, 0.1f);
        add(Items.LEATHER_BOOTS,        ElementType.ELECTRIC, 0.02f, ElementType.ICE, 0.1f);

        add(Items.CHAINMAIL_HELMET,     ElementType.WIND, 0.03f);
        add(Items.CHAINMAIL_CHESTPLATE, ElementType.WIND, 0.04f);
        add(Items.CHAINMAIL_LEGGINGS,   ElementType.WIND, 0.03f);
        add(Items.CHAINMAIL_BOOTS,      ElementType.WIND, 0.02f);
    }

    private static void add(Item item, ElementType t1, float v1, ElementType t2, float v2) {
        VANILLA_ARMOR_RESISTANCES.put(item, Map.of(t1, v1, t2, v2));
    }
    private static void add(Item item, ElementType t1, float v1, ElementType t2, float v2, ElementType t3, float v3) {
        VANILLA_ARMOR_RESISTANCES.put(item, Map.of(t1, v1, t2, v2, t3, v3));
    }
    private static void add(Item item, ElementType t1, float v1) {
        VANILLA_ARMOR_RESISTANCES.put(item, Map.of(t1, v1));
    }

    @SubscribeEvent
    public static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        VANILLA_ARMOR_RESISTANCES.forEach((item, resistances) -> {
            event.modify(item, builder -> builder.set(
                    DataComponents.CUSTOM_DATA,
                    ElementalResistanceComponent.createDefaultResistanceData(resistances)
            ));
        });

        AbloomMod.LOGGER.info("🛡️ Vanilla armor resistances registered via DataComponents.");
    }
}
