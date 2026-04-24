package com.auranite.abloom;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class AbloomModItems {
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(AbloomMod.MODID);

    public static final DeferredItem<Item> FIRE_STICK;
    public static final DeferredItem<Item> PHYSICAL_STICK;
    public static final DeferredItem<Item> WIND_STICK;
    public static final DeferredItem<Item> WATER_STICK;
    public static final DeferredItem<Item> EARTH_STICK;
    public static final DeferredItem<Item> ICE_STICK;
    public static final DeferredItem<Item> ELECTRIC_STICK;
    public static final DeferredItem<Item> ENERGY_STICK;
    public static final DeferredItem<Item> NATURAL_STICK;
    public static final DeferredItem<Item> QUANTUM_STICK;

    static {
        FIRE_STICK     = register("fire_stick", properties -> new Item(properties.stacksTo(1).rarity(Rarity.RARE)));
        PHYSICAL_STICK = register("physical_stick", properties -> new Item(properties.stacksTo(1).rarity(Rarity.RARE)));
        WIND_STICK     = register("wind_stick", properties -> new Item(properties.stacksTo(1).rarity(Rarity.RARE)));
        WATER_STICK    = register("water_stick", properties -> new Item(properties.stacksTo(1).rarity(Rarity.RARE)));
        EARTH_STICK    = register("earth_stick", properties -> new Item(properties.stacksTo(1).rarity(Rarity.RARE)));
        ICE_STICK      = register("ice_stick", properties -> new Item(properties.stacksTo(1).rarity(Rarity.RARE)));
        ELECTRIC_STICK = register("electric_stick", properties -> new Item(properties.stacksTo(1).rarity(Rarity.RARE)));
        ENERGY_STICK   = register("energy_stick", properties -> new Item(properties.stacksTo(1).rarity(Rarity.RARE)));
        NATURAL_STICK  = register("natural_stick", properties -> new Item(properties.stacksTo(1).rarity(Rarity.RARE)));
        QUANTUM_STICK  = register("quantum_stick", properties -> new Item(properties.stacksTo(1).rarity(Rarity.RARE)));
    }

    private static <I extends Item> DeferredItem<I> register(String name, Function<Item.Properties, ? extends I> supplier) {
        return REGISTRY.registerItem(name, supplier, Item.Properties::new);
    }

    private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
        return block(block, new Item.Properties());
    }

    private static DeferredItem<Item> block(DeferredHolder<Block, Block> block, Item.Properties properties) {
        return REGISTRY.registerItem(block.getId().getPath(), prop -> new BlockItem(block.get(), prop));
    }
}