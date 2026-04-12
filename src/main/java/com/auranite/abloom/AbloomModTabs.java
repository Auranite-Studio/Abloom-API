package com.auranite.abloom;

import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

public class AbloomModTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AbloomMod.MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> STONES = REGISTRY.register("abloom_api",
            () -> CreativeModeTab.builder().title(Component.translatable("item_group.abloom.abloom_api")).icon(() -> new ItemStack(Items.STICK)).displayItems((parameters, tabData) -> {
                tabData.accept(AbloomModItems.FIRE_STICK.get());
            }).build());
}