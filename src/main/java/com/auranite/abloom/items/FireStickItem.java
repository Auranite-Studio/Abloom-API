package com.auranite.abloom.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class FireStickItem extends Item {
    public FireStickItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    }
}