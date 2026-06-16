package com.auranite.abloom;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class MobResistanceRegistry {

    private MobResistanceRegistry() {}

    public static void init() {
        AbloomMod.LOGGER.info("Initializing Mob Resistance Registry...");

        // Регистрация иммунитетов
        registerEarthResistance(
                EntityTypes.ENDERMITE,
                EntityTypes.SILVERFISH,
                EntityTypes.SHULKER
        );

        registerFireResistance(
                EntityTypes.BLAZE,
                EntityTypes.MAGMA_CUBE,
                EntityTypes.WITHER,
                EntityTypes.ENDER_DRAGON,
                EntityTypes.STRIDER,
                EntityTypes.ZOMBIFIED_PIGLIN,
                EntityTypes.WITHER_SKELETON
        );

        registerWindResistance(
                EntityTypes.PHANTOM,
                EntityTypes.BREEZE
        );

        registerWaterResistance(
                EntityTypes.SQUID,
                EntityTypes.GLOW_SQUID,
                EntityTypes.NAUTILUS,
                EntityTypes.ZOMBIE_NAUTILUS,
                EntityTypes.DROWNED,
                EntityTypes.GUARDIAN,
                EntityTypes.ELDER_GUARDIAN,
                EntityTypes.AXOLOTL,
                EntityTypes.TADPOLE,
                EntityTypes.FROG,
                EntityTypes.TURTLE,
                EntityTypes.COD,
                EntityTypes.SALMON,
                EntityTypes.PUFFERFISH,
                EntityTypes.TROPICAL_FISH,
                EntityTypes.DOLPHIN,
                EntityTypes.WITCH
        );

        registerIceResistance(
                EntityTypes.SNOW_GOLEM,
                EntityTypes.STRAY,
                EntityTypes.POLAR_BEAR,
                EntityTypes.GOAT
        );

        registerQuantumResistance(
                EntityTypes.ENDERMAN,
                EntityTypes.ENDERMITE,
                EntityTypes.ENDER_DRAGON,
                EntityTypes.SHULKER
        );

        registerEtherResistance(
                EntityTypes.ENDER_DRAGON,
                EntityTypes.WITHER
        );

        registerFireResistance(
                EntityTypes.GHAST,
                EntityTypes.WARDEN,
                EntityTypes.HOGLIN,
                EntityTypes.PIGLIN,
                EntityTypes.PIGLIN_BRUTE,
                EntityTypes.ZOGLIN,
                EntityTypes.HUSK,
                EntityTypes.CAMEL,
                EntityTypes.CAMEL_HUSK,
                EntityTypes.ENDER_DRAGON,
                EntityTypes.WITHER
        );

        registerWindResistance(
                EntityTypes.ENDER_DRAGON,
                EntityTypes.GHAST,
                EntityTypes.HAPPY_GHAST,
                EntityTypes.VEX,
                EntityTypes.ALLAY,
                EntityTypes.PARROT,
                EntityTypes.CHICKEN,
                EntityTypes.OCELOT,
                EntityTypes.CAT,
                EntityTypes.FOX,
                EntityTypes.WOLF
        );


        registerElectricResistance(
                EntityTypes.CREEPER,
                EntityTypes.ENDERMAN,
                EntityTypes.PHANTOM,
                EntityTypes.ALLAY,
                EntityTypes.BREEZE
        );

        registerEnergyResistance(
                EntityTypes.ENDERMAN,
                EntityTypes.SHULKER,
                EntityTypes.WARDEN,
                EntityTypes.ENDER_DRAGON,
                EntityTypes.WITHER,
                EntityTypes.ELDER_GUARDIAN,
                EntityTypes.EVOKER,
                EntityTypes.WITCH
        );

        registerEnergyWeakness(
                EntityTypes.CREEPER,
                EntityTypes.GHAST,
                EntityTypes.HAPPY_GHAST
        );

        registerNaturalResistance(
                EntityTypes.BOGGED,
                EntityTypes.WITHER_SKELETON,
                EntityTypes.WITHER,
                EntityTypes.SLIME,
                EntityTypes.MAGMA_CUBE,
                EntityTypes.BEE,
                EntityTypes.WOLF,
                EntityTypes.OCELOT,
                EntityTypes.CAT,
                EntityTypes.PANDA,
                EntityTypes.FOX,
                EntityTypes.RABBIT
        );

        registerPhysicalResistance(
                EntityTypes.TURTLE,
                EntityTypes.ARMADILLO,
                EntityTypes.IRON_GOLEM,
//                EntityType.COPPER_GOLEM,
                EntityTypes.SHULKER,
                EntityTypes.WARDEN,
                EntityTypes.ENDER_DRAGON
        );

        registerPhysicalWeakness(
                EntityTypes.SLIME,
                EntityTypes.MAGMA_CUBE,
                EntityTypes.PHANTOM,
                EntityTypes.VEX,
                EntityTypes.ALLAY,
                EntityTypes.GLOW_SQUID,
                EntityTypes.SQUID
        );

        registerQuantumResistance(
                EntityTypes.WITHER,
                EntityTypes.WARDEN
        );

        registerEarthResistance(
                EntityTypes.IRON_GOLEM,
                EntityTypes.COPPER_GOLEM,
                EntityTypes.WARDEN,
                EntityTypes.GIANT,
                EntityTypes.RAVAGER,
                EntityTypes.ARMADILLO,
                EntityTypes.SNIFFER
        );

        // Регистрация уязвимостей
        registerFireWeakness(
                EntityTypes.SNOW_GOLEM,
                EntityTypes.DOLPHIN,
                EntityTypes.ZOMBIE,
                EntityTypes.ZOMBIE_VILLAGER,
                EntityTypes.DROWNED,
                EntityTypes.STRAY,
                EntityTypes.BOGGED
        );

        registerWindWeakness(
                EntityTypes.TURTLE,
                EntityTypes.SNIFFER,
                EntityTypes.ARMADILLO,
                EntityTypes.CAMEL,
                EntityTypes.CAMEL_HUSK,
                EntityTypes.RAVAGER,
                EntityTypes.HOGLIN,
                EntityTypes.POLAR_BEAR
        );

        registerEarthWeakness(
                EntityTypes.GHAST,
                EntityTypes.HAPPY_GHAST,
                EntityTypes.PHANTOM,
                EntityTypes.VEX,
                EntityTypes.ALLAY,
                EntityTypes.BREEZE
        );

        registerWaterWeakness(
                EntityTypes.BLAZE,
                EntityTypes.SNOW_GOLEM,
                EntityTypes.STRIDER,
                EntityTypes.BREEZE,
                EntityTypes.PARCHED
        );

        registerIceWeakness(
                EntityTypes.BLAZE,
                EntityTypes.MAGMA_CUBE,
                EntityTypes.STRIDER,
                EntityTypes.BREEZE,
                EntityTypes.PARCHED
        );

        registerElectricWeakness(
                EntityTypes.DROWNED,
                EntityTypes.TURTLE,
                EntityTypes.AXOLOTL,
                EntityTypes.FROG,
                EntityTypes.TADPOLE,
                EntityTypes.COD,
                EntityTypes.SALMON,
                EntityTypes.PUFFERFISH,
                EntityTypes.TROPICAL_FISH,
                EntityTypes.DOLPHIN,
                EntityTypes.SQUID,
                EntityTypes.GLOW_SQUID,
                EntityTypes.NAUTILUS,
                EntityTypes.ZOMBIE_NAUTILUS,
                EntityTypes.GUARDIAN,
                EntityTypes.ELDER_GUARDIAN
        );

        registerNaturalWeakness(
                EntityTypes.VILLAGER,
                EntityTypes.WANDERING_TRADER,
                EntityTypes.IRON_GOLEM,
                EntityTypes.COPPER_GOLEM,
                EntityTypes.SNOW_GOLEM,
                EntityTypes.ALLAY,
                EntityTypes.ZOGLIN,
                EntityTypes.STRAY,
                EntityTypes.ZOMBIFIED_PIGLIN,
                EntityTypes.ZOMBIE,
                EntityTypes.ZOMBIE_VILLAGER,
                EntityTypes.ZOMBIE_NAUTILUS,
                EntityTypes.SKELETON,
                EntityTypes.AXOLOTL
        );

        registerQuantumWeakness(
                EntityTypes.VILLAGER,
                EntityTypes.WANDERING_TRADER,
                EntityTypes.BAT,
                EntityTypes.ALLAY
        );

        registerEtherWeakness(
                EntityTypes.ENDERMAN,
                EntityTypes.ENDERMITE,
                EntityTypes.SHULKER,
                EntityTypes.WARDEN
        );


        AbloomMod.LOGGER.info("Mob Resistance Registry initialized!");
    }

    // Утилитарные методы для регистрации
    private static void registerFireResistance(EntityType<?>... types) {
        if (types == null) return;
        ElementResistanceRegistry.registerUniform(ElementType.FIRE, ElementResistanceManager.Resistance.HALF_RESIST.resistance(), types);
    }

    private static void registerWaterResistance(EntityType<?>... types) {
        if (types == null) return;
        ElementResistanceRegistry.registerUniform(ElementType.WATER, ElementResistanceManager.Resistance.HALF_RESIST.resistance(), types);
    }

    private static void registerWindResistance(EntityType<?>... types) {
        if (types == null) return;
        ElementResistanceRegistry.registerUniform(ElementType.WIND, ElementResistanceManager.Resistance.HALF_RESIST.resistance(), types);
    }

    private static void registerIceResistance(EntityType<?>... types) {
        if (types == null) return;
        ElementResistanceRegistry.registerUniform(ElementType.ICE, ElementResistanceManager.Resistance.HALF_RESIST.resistance(), types);
    }

    private static void registerQuantumResistance(EntityType<?>... types) {
        if (types == null) return;
        ElementResistanceRegistry.registerUniform(ElementType.QUANTUM, ElementResistanceManager.Resistance.HALF_RESIST.resistance(), types);
    }

    private static void registerEarthResistance(EntityType<?>... types) {
        if (types == null) return;
        ElementResistanceRegistry.registerUniform(ElementType.EARTH, ElementResistanceManager.Resistance.HALF_RESIST.resistance(), types);
    }

    private static void registerEtherResistance(EntityType<?>... types) {
        if (types == null) return;
        ElementResistanceRegistry.registerUniform(ElementType.ETHER, ElementResistanceManager.Resistance.HALF_RESIST.resistance(), types);
    }

    private static void registerElectricResistance(EntityType<?>... types) {
        if (types == null) return;
        ElementResistanceRegistry.registerUniform(ElementType.ELECTRIC, ElementResistanceManager.Resistance.HALF_RESIST.resistance(), types);
    }

    private static void registerEnergyResistance(EntityType<?>... types) {
        if (types == null) return;
        ElementResistanceRegistry.registerUniform(ElementType.ENERGY, ElementResistanceManager.Resistance.HALF_RESIST.resistance(), types);
    }

    private static void registerEnergyWeakness(EntityType<?>... types) {
        if (types == null) return;
        ElementResistanceRegistry.registerUniform(ElementType.ENERGY, ElementResistanceManager.Resistance.WEAKNESS.resistance(), types);
    }

    private static void registerNaturalResistance(EntityType<?>... types) {
        if (types == null) return;
        ElementResistanceRegistry.registerUniform(ElementType.NATURAL, ElementResistanceManager.Resistance.HALF_RESIST.resistance(), types);
    }

    private static void registerPhysicalResistance(EntityType<?>... types) {
        if (types == null) return;
        ElementResistanceRegistry.registerUniform(ElementType.PHYSICAL, ElementResistanceManager.Resistance.HALF_RESIST.resistance(), types);
    }

    private static void registerPhysicalWeakness(EntityType<?>... types) {
        if (types == null) return;
        ElementResistanceRegistry.registerUniform(ElementType.PHYSICAL, ElementResistanceManager.Resistance.WEAKNESS.resistance(), types);
    }

    private static void registerFireWeakness(EntityType<?>... types) {
        if (types == null) return;
        ElementResistanceRegistry.registerUniform(ElementType.FIRE, ElementResistanceManager.Resistance.WEAKNESS.resistance(), types);
    }

    private static void registerWindWeakness(EntityType<?>... types) {
        if (types == null) return;
        ElementResistanceRegistry.registerUniform(ElementType.WIND, ElementResistanceManager.Resistance.WEAKNESS.resistance(), types);
    }

    private static void registerEarthWeakness(EntityType<?>... types) {
        if (types == null) return;
        ElementResistanceRegistry.registerUniform(ElementType.EARTH, ElementResistanceManager.Resistance.WEAKNESS.resistance(), types);
    }

    private static void registerWaterWeakness(EntityType<?>... types) {
        if (types == null) return;
        ElementResistanceRegistry.registerUniform(ElementType.WATER, ElementResistanceManager.Resistance.WEAKNESS.resistance(), types);
    }

    private static void registerIceWeakness(EntityType<?>... types) {
        if (types == null) return;
        ElementResistanceRegistry.registerUniform(ElementType.ICE, ElementResistanceManager.Resistance.WEAKNESS.resistance(), types);
    }

    private static void registerElectricWeakness(EntityType<?>... types) {
        if (types == null) return;
        ElementResistanceRegistry.registerUniform(ElementType.ELECTRIC, ElementResistanceManager.Resistance.WEAKNESS.resistance(), types);
    }

    private static void registerNaturalWeakness(EntityType<?>... types) {
        if (types == null) return;
        ElementResistanceRegistry.registerUniform(ElementType.NATURAL, ElementResistanceManager.Resistance.WEAKNESS.resistance(), types);
    }

    private static void registerQuantumWeakness(EntityType<?>... types) {
        if (types == null) return;
        ElementResistanceRegistry.registerUniform(ElementType.QUANTUM, ElementResistanceManager.Resistance.WEAKNESS.resistance(), types);
    }

    private static void registerEtherWeakness(EntityType<?>... types) {
        if (types == null) return;
        ElementResistanceRegistry.registerUniform(ElementType.ETHER, ElementResistanceManager.Resistance.WEAKNESS.resistance(), types);
    }

    public static void registerCustomResistance(EntityType<?> entityType, ElementType element, float resistance) {
        ElementResistanceRegistry.registerSingle(entityType, element, resistance);
    }
}
