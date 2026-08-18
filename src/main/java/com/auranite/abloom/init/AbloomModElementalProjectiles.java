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

        ElementalProjectileRegistry.registerProjectile(EntityTypes.FIREBALL, ElementType.FIRE, 0f);
        ElementalProjectileRegistry.registerProjectile(EntityTypes.SMALL_FIREBALL, ElementType.FIRE, 0f);
        ElementalProjectileRegistry.registerProjectile(EntityTypes.DRAGON_FIREBALL, ElementType.ENERGY, 0f);

        ElementalProjectileRegistry.registerProjectile(EntityTypes.FIREWORK_ROCKET, ElementType.PHYSICAL, 0f);
        ElementalProjectileRegistry.registerProjectile(EntityTypes.WITHER_SKULL, ElementType.EARTH, 0f);
        ElementalProjectileRegistry.registerProjectile(EntityTypes.SHULKER_BULLET, ElementType.WIND, 0f);
        ElementalProjectileRegistry.registerProjectile(EntityTypes.LLAMA_SPIT, ElementType.WATER, 0f);
        ElementalProjectileRegistry.registerProjectile(EntityTypes.BREEZE_WIND_CHARGE, ElementType.WIND, 0f);
        ElementalProjectileRegistry.registerProjectile(EntityTypes.WIND_CHARGE, ElementType.WIND, 0f);

        registerCustomProjectiles();

        AbloomMod.LOGGER.info("Registered {} elemental projectile types",
                ElementalProjectileRegistry.getRegisteredCount());
    }

    private static void registerCustomProjectiles() {

    }
}
