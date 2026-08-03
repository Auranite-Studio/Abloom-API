package com.auranite.abloom;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class AbloomAttributes {
    public static final DeferredRegister<Attribute> REGISTRY = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, AbloomMod.MODID);

    /** Base value for crit chance: 5% (0.05). */
    public static final double CRIT_CHANCE_BASE = 0.05;

    /** Base value for crit damage: 50% (0.5), total multiplier 1.5x. */
    public static final double CRIT_DAMAGE_BASE = 0.5;

    /**
     * Critical hit chance attribute. Default base: 0.05 (5%).
     * Direct Attribute reference for use with LivingEntity.getAttribute().
     */
    public static final Attribute CRIT_CHANCE =
            new RangedAttribute("abloom.crit_chance", CRIT_CHANCE_BASE, 0.0, 100.0)
                    .setSyncable(true);

    /**
     * Critical hit damage multiplier attribute. Default base: 0.5 (50% extra).
     */
    public static final Attribute CRIT_DAMAGE =
            new RangedAttribute("abloom.crit_damage", CRIT_DAMAGE_BASE, 0.0, 10.0)
                    .setSyncable(true);

    /**
     * Register attributes with the deferred register for discovery.
     */
    public static void registerAttributes(IEventBus modEventBus) {
        REGISTRY.register(modEventBus);
    }

    @SubscribeEvent
    public static void modifyAttributes(EntityAttributeModificationEvent event) {
        // Attributes are added via modifiers on items/effects, not added by default
    }
}
