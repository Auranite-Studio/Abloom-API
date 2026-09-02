/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package com.auranite.abloom.init;

import com.auranite.abloom.AbloomMod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.core.registries.BuiltInRegistries;

@EventBusSubscriber
public class AbloomModAttributes {
    public static final DeferredRegister<Attribute> REGISTRY = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, AbloomMod.MODID);
    public static final DeferredHolder<Attribute, Attribute> CRIT_DMG = REGISTRY.register("crit_dmg", () -> new RangedAttribute("attribute.power.crit_dmg", 0, -10, 10).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> CRIT_CHANCE = REGISTRY.register("crit_chance", () -> new RangedAttribute("attribute.power.crit_chance", 0, -1, 2).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> MULTI_CRIT_DMG = REGISTRY.register("multi_crit_dmg", () -> new RangedAttribute("attribute.power.multi_crit_dmg", 0, -10, 10).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> MULTI_CRIT_CHANCE = REGISTRY.register("multi_crit_chance", () -> new RangedAttribute("attribute.power.multi_crit_chance", 0, -1, 2).setSyncable(true));

    @SubscribeEvent
    public static void addAttributes(EntityAttributeModificationEvent event) {
        event.getTypes().forEach(entity -> event.add(entity, CRIT_DMG));
        event.getTypes().forEach(entity -> event.add(entity, CRIT_CHANCE));
    }
}