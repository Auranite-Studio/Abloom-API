package com.auranite.abloom;

import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;


public class AbloomModElementalProjectiles {


    public static void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(AbloomModElementalProjectiles::registerAll);
    }


    public static void registerAll() {



        ElementalProjectileRegistry.registerProjectile(EntityType.FIREBALL, ElementType.FIRE, 0f);
        ElementalProjectileRegistry.registerProjectile(EntityType.SMALL_FIREBALL, ElementType.FIRE, 0f);
        ElementalProjectileRegistry.registerProjectile(EntityType.DRAGON_FIREBALL, ElementType.ENERGY, 0f);


        ElementalProjectileRegistry.registerProjectile(EntityType.FIREWORK_ROCKET, ElementType.PHYSICAL, 0f);
        ElementalProjectileRegistry.registerProjectile(EntityType.WITHER_SKULL, ElementType.EARTH, 0f);
        ElementalProjectileRegistry.registerProjectile(EntityType.SHULKER_BULLET, ElementType.WIND, 0f);
        ElementalProjectileRegistry.registerProjectile(EntityType.LLAMA_SPIT, ElementType.WATER, 0f);
        ElementalProjectileRegistry.registerProjectile(EntityType.BREEZE_WIND_CHARGE, ElementType.WIND, 0f);
        ElementalProjectileRegistry.registerProjectile(EntityType.WIND_CHARGE, ElementType.WIND, 0f);



        registerCustomProjectiles();

        AbloomMod.LOGGER.info("Registered {} elemental projectile types",
                ElementalProjectileRegistry.getRegisteredCount());
    }


    private static void registerCustomProjectiles() {





    }
}