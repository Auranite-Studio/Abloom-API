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
    public static final DeferredHolder<Attribute, Attribute> CRIT_CHANCE = REGISTRY.register("crit_chance", () -> new RangedAttribute("attribute.power.crit_chance", 0, -2, 2).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> RESONANCE_ACCUMULATION_BUILDUP = REGISTRY.register("resonance_accumulation_buildup", () -> new RangedAttribute("attribute.power.resonance_accumulation_buildup", 0, -1, 1).setSyncable(true));

    // Elemental damage bonus attributes: range -1 to 1 (±100% damage modifier)
    public static final DeferredHolder<Attribute, Attribute> FIRE_DMG_BONUS = REGISTRY.register("fire_dmg_bonus", () -> new RangedAttribute("attribute.power.fire_dmg_bonus", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> PHYSICAL_DMG_BONUS = REGISTRY.register("physical_dmg_bonus", () -> new RangedAttribute("attribute.power.physical_dmg_bonus", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> WIND_DMG_BONUS = REGISTRY.register("wind_dmg_bonus", () -> new RangedAttribute("attribute.power.wind_dmg_bonus", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> EARTH_DMG_BONUS = REGISTRY.register("earth_dmg_bonus", () -> new RangedAttribute("attribute.power.earth_dmg_bonus", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> WATER_DMG_BONUS = REGISTRY.register("water_dmg_bonus", () -> new RangedAttribute("attribute.power.water_dmg_bonus", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> ICE_DMG_BONUS = REGISTRY.register("ice_dmg_bonus", () -> new RangedAttribute("attribute.power.ice_dmg_bonus", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> ELECTRIC_DMG_BONUS = REGISTRY.register("electric_dmg_bonus", () -> new RangedAttribute("attribute.power.electric_dmg_bonus", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> ENERGY_DMG_BONUS = REGISTRY.register("energy_dmg_bonus", () -> new RangedAttribute("attribute.power.energy_dmg_bonus", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> NATURAL_DMG_BONUS = REGISTRY.register("natural_dmg_bonus", () -> new RangedAttribute("attribute.power.natural_dmg_bonus", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> QUANTUM_DMG_BONUS = REGISTRY.register("quantum_dmg_bonus", () -> new RangedAttribute("attribute.power.quantum_dmg_bonus", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> ETHER_DMG_BONUS = REGISTRY.register("ether_dmg_bonus", () -> new RangedAttribute("attribute.power.ether_dmg_bonus", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> LIGHT_DMG_BONUS = REGISTRY.register("light_dmg_bonus", () -> new RangedAttribute("attribute.power.light_dmg_bonus", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> SHADOW_DMG_BONUS = REGISTRY.register("shadow_dmg_bonus", () -> new RangedAttribute("attribute.power.shadow_dmg_bonus", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> PRISMATIC_DMG_BONUS = REGISTRY.register("prismatic_dmg_bonus", () -> new RangedAttribute("attribute.power.prismatic_dmg_bonus", 0, -1, 1).setSyncable(true));

    // Elemental resistance modifier attributes: range -1 to 1 (±100% resistance modifier)
    // Positive = more resistance (less damage), Negative = less resistance (more damage)
    public static final DeferredHolder<Attribute, Attribute> FIRE_RESIST_MOD = REGISTRY.register("fire_resist_mod", () -> new RangedAttribute("attribute.power.fire_resist_mod", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> PHYSICAL_RESIST_MOD = REGISTRY.register("physical_resist_mod", () -> new RangedAttribute("attribute.power.physical_resist_mod", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> WIND_RESIST_MOD = REGISTRY.register("wind_resist_mod", () -> new RangedAttribute("attribute.power.wind_resist_mod", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> EARTH_RESIST_MOD = REGISTRY.register("earth_resist_mod", () -> new RangedAttribute("attribute.power.earth_resist_mod", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> WATER_RESIST_MOD = REGISTRY.register("water_resist_mod", () -> new RangedAttribute("attribute.power.water_resist_mod", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> ICE_RESIST_MOD = REGISTRY.register("ice_resist_mod", () -> new RangedAttribute("attribute.power.ice_resist_mod", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> ELECTRIC_RESIST_MOD = REGISTRY.register("electric_resist_mod", () -> new RangedAttribute("attribute.power.electric_resist_mod", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> ENERGY_RESIST_MOD = REGISTRY.register("energy_resist_mod", () -> new RangedAttribute("attribute.power.energy_resist_mod", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> NATURAL_RESIST_MOD = REGISTRY.register("natural_resist_mod", () -> new RangedAttribute("attribute.power.natural_resist_mod", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> QUANTUM_RESIST_MOD = REGISTRY.register("quantum_resist_mod", () -> new RangedAttribute("attribute.power.quantum_resist_mod", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> ETHER_RESIST_MOD = REGISTRY.register("ether_resist_mod", () -> new RangedAttribute("attribute.power.ether_resist_mod", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> LIGHT_RESIST_MOD = REGISTRY.register("light_resist_mod", () -> new RangedAttribute("attribute.power.light_resist_mod", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> SHADOW_RESIST_MOD = REGISTRY.register("shadow_resist_mod", () -> new RangedAttribute("attribute.power.shadow_resist_mod", 0, -1, 1).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> PRISMATIC_RESIST_MOD = REGISTRY.register("prismatic_resist_mod", () -> new RangedAttribute("attribute.power.prismatic_resist_mod", 0, -1, 1).setSyncable(true));

    @SubscribeEvent
    public static void addAttributes(EntityAttributeModificationEvent event) {
        event.getTypes().forEach(entity -> event.add(entity, CRIT_DMG));
        event.getTypes().forEach(entity -> event.add(entity, CRIT_CHANCE));
        event.getTypes().forEach(entity -> event.add(entity, RESONANCE_ACCUMULATION_BUILDUP));
        event.getTypes().forEach(entity -> event.add(entity, FIRE_DMG_BONUS));
        event.getTypes().forEach(entity -> event.add(entity, PHYSICAL_DMG_BONUS));
        event.getTypes().forEach(entity -> event.add(entity, WIND_DMG_BONUS));
        event.getTypes().forEach(entity -> event.add(entity, EARTH_DMG_BONUS));
        event.getTypes().forEach(entity -> event.add(entity, WATER_DMG_BONUS));
        event.getTypes().forEach(entity -> event.add(entity, ICE_DMG_BONUS));
        event.getTypes().forEach(entity -> event.add(entity, ELECTRIC_DMG_BONUS));
        event.getTypes().forEach(entity -> event.add(entity, ENERGY_DMG_BONUS));
        event.getTypes().forEach(entity -> event.add(entity, NATURAL_DMG_BONUS));
        event.getTypes().forEach(entity -> event.add(entity, QUANTUM_DMG_BONUS));
        event.getTypes().forEach(entity -> event.add(entity, ETHER_DMG_BONUS));
        event.getTypes().forEach(entity -> event.add(entity, LIGHT_DMG_BONUS));
        event.getTypes().forEach(entity -> event.add(entity, SHADOW_DMG_BONUS));
        event.getTypes().forEach(entity -> event.add(entity, PRISMATIC_DMG_BONUS));
        event.getTypes().forEach(entity -> event.add(entity, FIRE_RESIST_MOD));
        event.getTypes().forEach(entity -> event.add(entity, PHYSICAL_RESIST_MOD));
        event.getTypes().forEach(entity -> event.add(entity, WIND_RESIST_MOD));
        event.getTypes().forEach(entity -> event.add(entity, EARTH_RESIST_MOD));
        event.getTypes().forEach(entity -> event.add(entity, WATER_RESIST_MOD));
        event.getTypes().forEach(entity -> event.add(entity, ICE_RESIST_MOD));
        event.getTypes().forEach(entity -> event.add(entity, ELECTRIC_RESIST_MOD));
        event.getTypes().forEach(entity -> event.add(entity, ENERGY_RESIST_MOD));
        event.getTypes().forEach(entity -> event.add(entity, NATURAL_RESIST_MOD));
        event.getTypes().forEach(entity -> event.add(entity, QUANTUM_RESIST_MOD));
        event.getTypes().forEach(entity -> event.add(entity, ETHER_RESIST_MOD));
        event.getTypes().forEach(entity -> event.add(entity, LIGHT_RESIST_MOD));
        event.getTypes().forEach(entity -> event.add(entity, SHADOW_RESIST_MOD));
        event.getTypes().forEach(entity -> event.add(entity, PRISMATIC_RESIST_MOD));
    }
}