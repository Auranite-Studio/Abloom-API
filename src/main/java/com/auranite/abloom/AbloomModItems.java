/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package com.auranite.abloom;

import com.auranite.abloom.items.FireStickItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;


public class AbloomModItems {
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(AbloomMod.MODID);

    public static final DeferredItem<Item> FIRE_STICK;

    static {
        FIRE_STICK = REGISTRY.register("fire_stick", FireStickItem::new);
    }
    private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
        return block(block, new Item.Properties());
    }

    private static DeferredItem<Item> block(DeferredHolder<Block, Block> block, Item.Properties properties) {
        return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
    }
}
