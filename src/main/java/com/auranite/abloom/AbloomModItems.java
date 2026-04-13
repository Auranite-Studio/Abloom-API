
package com.auranite.abloom;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AbloomModItems {
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(AbloomMod.MODID);

    public static final DeferredItem<Item> FIRE_STICK;
    public static final DeferredItem<Item> PHYSICAL_STICK;
    public static final DeferredItem<Item> WIND_STICK;
    public static final DeferredItem<Item> WATER_STICK;
    public static final DeferredItem<Item> EARTH_STICK;
    public static final DeferredItem<Item> ICE_STICK;
    public static final DeferredItem<Item> ELECTRIC_STICK;
    public static final DeferredItem<Item> SOURCE_STICK;
    public static final DeferredItem<Item> NATURAL_STICK;
    public static final DeferredItem<Item> QUANTUM_STICK;

    
    private static final Item.Properties STICK_PROPS = new Item.Properties()
            .stacksTo(1)
            .rarity(Rarity.RARE);

    static {
        FIRE_STICK     = REGISTRY.register("fire_stick", () -> new Item(STICK_PROPS));
        PHYSICAL_STICK = REGISTRY.register("physical_stick", () -> new Item(STICK_PROPS));
        WIND_STICK     = REGISTRY.register("wind_stick", () -> new Item(STICK_PROPS));
        WATER_STICK    = REGISTRY.register("water_stick", () -> new Item(STICK_PROPS));
        EARTH_STICK    = REGISTRY.register("earth_stick", () -> new Item(STICK_PROPS));
        ICE_STICK      = REGISTRY.register("ice_stick", () -> new Item(STICK_PROPS));
        ELECTRIC_STICK = REGISTRY.register("electric_stick", () -> new Item(STICK_PROPS));
        SOURCE_STICK   = REGISTRY.register("source_stick", () -> new Item(STICK_PROPS));
        NATURAL_STICK  = REGISTRY.register("natural_stick", () -> new Item(STICK_PROPS));
        QUANTUM_STICK  = REGISTRY.register("quantum_stick", () -> new Item(STICK_PROPS));
    }

    private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
        return block(block, new Item.Properties());
    }

    private static DeferredItem<Item> block(DeferredHolder<Block, Block> block, Item.Properties properties) {
        return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
    }
}