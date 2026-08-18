package com.auranite.abloom.init;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.util.ElementType;
import com.auranite.abloom.registries.ElementalProjectileRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public class AbloomModElementalProjectiles {

    public static void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(AbloomModElementalProjectiles::registerAll);
    }

    public static void registerAll() {

        // allowOverride = true → projectile берёт элемент из оружия стрелка (attachment) приоритетно
        ElementalProjectileRegistry.registerProjectile(EntityTypes.FIREBALL, ElementType.FIRE, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityTypes.SMALL_FIREBALL, ElementType.FIRE, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityTypes.DRAGON_FIREBALL, ElementType.QUANTUM, 0f, false);

        ElementalProjectileRegistry.registerProjectile(EntityTypes.FIREWORK_ROCKET, ElementType.PHYSICAL, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityTypes.WITHER_SKULL, ElementType.QUANTUM, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityTypes.SHULKER_BULLET, ElementType.WIND, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityTypes.LLAMA_SPIT, ElementType.WATER, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityTypes.BREEZE_WIND_CHARGE, ElementType.WIND, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityTypes.WIND_CHARGE, ElementType.WIND, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityTypes.TRIDENT, ElementType.WATER, 12f, true);
        registerCustomProjectiles();

        AbloomMod.LOGGER.info("Registered {} elemental projectile types",
                ElementalProjectileRegistry.getRegisteredCount());
    }

    private static void registerCustomProjectiles() {

    }
}
