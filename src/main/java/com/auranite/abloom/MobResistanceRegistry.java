package com.auranite.abloom;

import net.minecraft.world.entity.EntityType;

public class MobResistanceRegistry {

    private MobResistanceRegistry() {}

    public static void init() {
        AbloomMod.LOGGER.info("Initializing Mob Resistance Registry...");

        // Регистрация иммунитетов
        registerEarthImmune(
                EntityType.ENDERMITE,
                EntityType.SILVERFISH,
                EntityType.SHULKER
        );

        registerFireImmune(
                EntityType.BLAZE,
                EntityType.MAGMA_CUBE,
                EntityType.WITHER,
                EntityType.ENDER_DRAGON,
                EntityType.STRIDER,
                EntityType.ZOMBIFIED_PIGLIN,
                EntityType.WITHER_SKELETON
        );

        registerWindImmune(
                EntityType.PHANTOM,
                EntityType.BREEZE
        );

        registerWaterImmune(
                EntityType.SQUID,
                EntityType.GLOW_SQUID,
//                EntityType.NAUTILUS,
//                EntityType.ZOMBIE_NAUTILUS,
                EntityType.DROWNED,
                EntityType.GUARDIAN,
                EntityType.ELDER_GUARDIAN,
                EntityType.AXOLOTL,
                EntityType.TADPOLE,
                EntityType.FROG,
                EntityType.TURTLE,
                EntityType.COD,
                EntityType.SALMON,
                EntityType.PUFFERFISH,
                EntityType.TROPICAL_FISH,
                EntityType.DOLPHIN
        );

        registerWaterResistance(
                EntityType.WITCH
        );

        registerIceImmune(
                EntityType.SNOW_GOLEM,
                EntityType.STRAY,
                EntityType.POLAR_BEAR,
                EntityType.GOAT
        );

        registerQuantumImmune(
                EntityType.ENDERMAN,
                EntityType.ENDERMITE,
                EntityType.ENDER_DRAGON,
                EntityType.SHULKER
        );

        registerEtherImmune(
                EntityType.ENDER_DRAGON,
                EntityType.WITHER
        );

        registerFireResistance(
                EntityType.GHAST,
                EntityType.WARDEN,
                EntityType.HOGLIN,
                EntityType.PIGLIN,
                EntityType.PIGLIN_BRUTE,
                EntityType.ZOGLIN,
                EntityType.HUSK,
                EntityType.CAMEL
//                EntityType.CAMEL_HUSK
        );

        registerWindResistance(
                EntityType.ENDER_DRAGON,
                EntityType.GHAST,
//                EntityType.HAPPY_GHAST,
                EntityType.VEX,
                EntityType.ALLAY,
                EntityType.PARROT,
                EntityType.CHICKEN,
                EntityType.OCELOT,
                EntityType.CAT,
                EntityType.FOX,
                EntityType.WOLF
        );


        registerElectricImmune(
                EntityType.CREEPER
        );

        registerElectricResistance(
                EntityType.ENDERMAN,
                EntityType.PHANTOM,
                EntityType.ALLAY,
                EntityType.BREEZE
        );

        registerEnergyImmune(
                EntityType.ENDERMAN,
                EntityType.SHULKER,
                EntityType.WARDEN
        );

        registerEnergyResistance(
                EntityType.ENDER_DRAGON,
                EntityType.WITHER,
                EntityType.ELDER_GUARDIAN,
                EntityType.EVOKER,
                EntityType.WITCH
        );

        registerEnergyWeakness(
                EntityType.CREEPER,
                EntityType.GHAST
//                EntityType.HAPPY_GHAST
        );

        registerNaturalImmune(
                EntityType.BOGGED,
                EntityType.WITHER_SKELETON,
                EntityType.WITHER,
                EntityType.SLIME,
                EntityType.MAGMA_CUBE,
                EntityType.BEE
        );

        registerNaturalResistance(
                EntityType.WOLF,
                EntityType.OCELOT,
                EntityType.CAT,
                EntityType.PANDA,
                EntityType.FOX,
                EntityType.RABBIT
        );

        registerPhysicalResistance(
                EntityType.TURTLE,
                EntityType.ARMADILLO,
                EntityType.IRON_GOLEM,
//                EntityType.COPPER_GOLEM,
                EntityType.SHULKER,
                EntityType.WARDEN,
                EntityType.ENDER_DRAGON
        );

        registerPhysicalWeakness(
                EntityType.SLIME,
                EntityType.MAGMA_CUBE,
                EntityType.PHANTOM,
                EntityType.VEX,
                EntityType.ALLAY,
                EntityType.GLOW_SQUID,
                EntityType.SQUID
        );

        registerQuantumResistance(
                EntityType.WITHER,
                EntityType.WARDEN
        );

        registerEarthResistance(
                EntityType.IRON_GOLEM,
//                EntityType.COPPER_GOLEM,
                EntityType.WARDEN,
                EntityType.GIANT,
                EntityType.RAVAGER,
                EntityType.ARMADILLO,
                EntityType.SNIFFER
        );

        // Регистрация уязвимостей
        registerFireWeakness(
                EntityType.SNOW_GOLEM,
                EntityType.DOLPHIN,
                EntityType.ZOMBIE,
                EntityType.ZOMBIE_VILLAGER,
                EntityType.DROWNED,
                EntityType.STRAY,
                EntityType.BOGGED
        );

        registerWindWeakness(
                EntityType.TURTLE,
                EntityType.SNIFFER,
                EntityType.ARMADILLO,
                EntityType.CAMEL,
//                EntityType.CAMEL_HUSK,
                EntityType.RAVAGER,
                EntityType.HOGLIN,
                EntityType.POLAR_BEAR
        );

        registerEarthWeakness(
                EntityType.GHAST,
//                EntityType.HAPPY_GHAST,
                EntityType.PHANTOM,
                EntityType.VEX,
                EntityType.ALLAY,
                EntityType.BREEZE
        );

        registerWaterWeakness(
                EntityType.BLAZE,
                EntityType.SNOW_GOLEM,
                EntityType.STRIDER,
                EntityType.BREEZE
//                EntityType.PARCHED
        );

        registerIceWeakness(
                EntityType.BLAZE,
                EntityType.MAGMA_CUBE,
                EntityType.STRIDER,
                EntityType.BREEZE
//                EntityType.PARCHED
        );

        registerElectricWeakness(
                EntityType.DROWNED,
                EntityType.TURTLE,
                EntityType.AXOLOTL,
                EntityType.FROG,
                EntityType.TADPOLE,
                EntityType.COD,
                EntityType.SALMON,
                EntityType.PUFFERFISH,
                EntityType.TROPICAL_FISH,
                EntityType.DOLPHIN,
                EntityType.SQUID,
                EntityType.GLOW_SQUID,
//                EntityType.NAUTILUS,
//                EntityType.ZOMBIE_NAUTILUS,
                EntityType.GUARDIAN,
                EntityType.ELDER_GUARDIAN
        );

        registerNaturalWeakness(
                EntityType.VILLAGER,
                EntityType.WANDERING_TRADER,
                EntityType.IRON_GOLEM,
//                EntityType.COPPER_GOLEM,
                EntityType.SNOW_GOLEM,
                EntityType.ALLAY,
                EntityType.ZOGLIN,
                EntityType.STRAY,
                EntityType.ZOMBIFIED_PIGLIN,
                EntityType.ZOMBIE,
                EntityType.ZOMBIE_VILLAGER,
//                EntityType.ZOMBIE_NAUTILUS,
                EntityType.SKELETON,
                EntityType.AXOLOTL
        );

        registerQuantumWeakness(
                EntityType.VILLAGER,
                EntityType.WANDERING_TRADER,
                EntityType.BAT,
                EntityType.ALLAY
        );

        registerEtherWeakness(
                EntityType.ENDERMAN,
                EntityType.ENDERMITE,
                EntityType.SHULKER,
                EntityType.WARDEN
        );


        AbloomMod.LOGGER.info("Mob Resistance Registry initialized!");
    }

    // Утилитарные методы для регистрации
    private static void registerFireImmune(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.FIRE, ElementResistanceManager.Resistance.IMMUNE.accumulationResistance(), ElementResistanceManager.Resistance.IMMUNE.damageResistance(), types);
    }

    private static void registerWaterImmune(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.WATER, ElementResistanceManager.Resistance.IMMUNE.accumulationResistance(), ElementResistanceManager.Resistance.IMMUNE.damageResistance(), types);
    }

    private static void registerWindImmune(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.WIND, ElementResistanceManager.Resistance.IMMUNE.accumulationResistance(), ElementResistanceManager.Resistance.IMMUNE.damageResistance(), types);
    }

    private static void registerIceImmune(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.ICE, ElementResistanceManager.Resistance.IMMUNE.accumulationResistance(), ElementResistanceManager.Resistance.IMMUNE.damageResistance(), types);
    }

    private static void registerQuantumImmune(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.QUANTUM, ElementResistanceManager.Resistance.IMMUNE.accumulationResistance(), ElementResistanceManager.Resistance.IMMUNE.damageResistance(), types);
    }

    private static void registerEarthImmune(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.EARTH, ElementResistanceManager.Resistance.IMMUNE.accumulationResistance(), ElementResistanceManager.Resistance.IMMUNE.damageResistance(), types);
    }

    private static void registerEtherImmune(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.ETHER, ElementResistanceManager.Resistance.IMMUNE.accumulationResistance(), ElementResistanceManager.Resistance.IMMUNE.damageResistance(), types);
    }

    private static void registerFireResistance(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.FIRE, ElementResistanceManager.Resistance.HALF_RESIST.accumulationResistance(), ElementResistanceManager.Resistance.HALF_RESIST.damageResistance(), types);
    }

    private static void registerWindResistance(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.WIND, ElementResistanceManager.Resistance.HALF_RESIST.accumulationResistance(), ElementResistanceManager.Resistance.HALF_RESIST.damageResistance(), types);
    }

    private static void registerElectricImmune(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.ELECTRIC, ElementResistanceManager.Resistance.IMMUNE.accumulationResistance(), ElementResistanceManager.Resistance.IMMUNE.damageResistance(), types);
    }

    private static void registerElectricResistance(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.ELECTRIC, ElementResistanceManager.Resistance.HALF_RESIST.accumulationResistance(), ElementResistanceManager.Resistance.HALF_RESIST.damageResistance(), types);
    }

    private static void registerWaterResistance(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.WATER, ElementResistanceManager.Resistance.HALF_RESIST.accumulationResistance(), ElementResistanceManager.Resistance.HALF_RESIST.damageResistance(), types);
    }

    private static void registerEnergyImmune(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.ENERGY, ElementResistanceManager.Resistance.IMMUNE.accumulationResistance(), ElementResistanceManager.Resistance.IMMUNE.damageResistance(), types);
    }

    private static void registerEnergyResistance(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.ENERGY, ElementResistanceManager.Resistance.HALF_RESIST.accumulationResistance(), ElementResistanceManager.Resistance.HALF_RESIST.damageResistance(), types);
    }

    private static void registerEnergyWeakness(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.ENERGY, ElementResistanceManager.Resistance.WEAKNESS.accumulationResistance(), ElementResistanceManager.Resistance.WEAKNESS.damageResistance(), types);
    }

    private static void registerNaturalImmune(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.NATURAL, ElementResistanceManager.Resistance.IMMUNE.accumulationResistance(), ElementResistanceManager.Resistance.IMMUNE.damageResistance(), types);
    }

    private static void registerNaturalResistance(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.NATURAL, ElementResistanceManager.Resistance.HALF_RESIST.accumulationResistance(), ElementResistanceManager.Resistance.HALF_RESIST.damageResistance(), types);
    }

    private static void registerQuantumResistance(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.QUANTUM, ElementResistanceManager.Resistance.HALF_RESIST.accumulationResistance(), ElementResistanceManager.Resistance.HALF_RESIST.damageResistance(), types);
    }

    private static void registerEarthResistance(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.EARTH, ElementResistanceManager.Resistance.HALF_RESIST.accumulationResistance(), ElementResistanceManager.Resistance.HALF_RESIST.damageResistance(), types);
    }

    private static void registerPhysicalResistance(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.PHYSICAL, ElementResistanceManager.Resistance.HALF_RESIST.accumulationResistance(), ElementResistanceManager.Resistance.HALF_RESIST.damageResistance(), types);
    }

    private static void registerPhysicalWeakness(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.PHYSICAL, ElementResistanceManager.Resistance.WEAKNESS.accumulationResistance(), ElementResistanceManager.Resistance.WEAKNESS.damageResistance(), types);
    }

    private static void registerFireWeakness(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.FIRE, ElementResistanceManager.Resistance.WEAKNESS.accumulationResistance(), ElementResistanceManager.Resistance.WEAKNESS.damageResistance(), types);
    }

    private static void registerWindWeakness(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.WIND, ElementResistanceManager.Resistance.WEAKNESS.accumulationResistance(), ElementResistanceManager.Resistance.WEAKNESS.damageResistance(), types);
    }

    private static void registerEarthWeakness(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.EARTH, ElementResistanceManager.Resistance.WEAKNESS.accumulationResistance(), ElementResistanceManager.Resistance.WEAKNESS.damageResistance(), types);
    }

    private static void registerWaterWeakness(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.WATER, ElementResistanceManager.Resistance.WEAKNESS.accumulationResistance(), ElementResistanceManager.Resistance.WEAKNESS.damageResistance(), types);
    }

    private static void registerIceWeakness(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.ICE, ElementResistanceManager.Resistance.WEAKNESS.accumulationResistance(), ElementResistanceManager.Resistance.WEAKNESS.damageResistance(), types);
    }

    private static void registerElectricWeakness(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.ELECTRIC, ElementResistanceManager.Resistance.WEAKNESS.accumulationResistance(), ElementResistanceManager.Resistance.WEAKNESS.damageResistance(), types);
    }

    private static void registerNaturalWeakness(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.NATURAL, ElementResistanceManager.Resistance.WEAKNESS.accumulationResistance(), ElementResistanceManager.Resistance.WEAKNESS.damageResistance(), types);
    }

    private static void registerQuantumWeakness(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.QUANTUM, ElementResistanceManager.Resistance.WEAKNESS.accumulationResistance(), ElementResistanceManager.Resistance.WEAKNESS.damageResistance(), types);
    }

    private static void registerEtherWeakness(EntityType<?>... types) {
        ElementResistanceRegistry.registerUniform(ElementType.ETHER, ElementResistanceManager.Resistance.WEAKNESS.accumulationResistance(), ElementResistanceManager.Resistance.WEAKNESS.damageResistance(), types);
    }

    public static void registerCustomResistance(EntityType<?> entityType, ElementType element, float accumResistance, float damageResistance) {
        ElementResistanceRegistry.registerSingle(entityType, element, accumResistance, damageResistance);
    }
}
