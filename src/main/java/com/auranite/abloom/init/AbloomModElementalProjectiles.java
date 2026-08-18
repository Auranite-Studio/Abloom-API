package com.auranite.abloom.init;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.util.ElementType;
import com.auranite.abloom.registries.ElementalProjectileRegistry;
import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public class AbloomModElementalProjectiles {

    public static void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(AbloomModElementalProjectiles::registerAll);
    }

    public static void registerAll() {

        // allowOverride = true → projectile берёт элемент из оружия стрелка (attachment) приоритетно
        ElementalProjectileRegistry.registerProjectile(EntityType.FIREBALL, ElementType.FIRE, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityType.SMALL_FIREBALL, ElementType.FIRE, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityType.DRAGON_FIREBALL, ElementType.QUANTUM, 0f, false);

        ElementalProjectileRegistry.registerProjectile(EntityType.FIREWORK_ROCKET, ElementType.PHYSICAL, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityType.WITHER_SKULL, ElementType.QUANTUM, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityType.SHULKER_BULLET, ElementType.WIND, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityType.LLAMA_SPIT, ElementType.WATER, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityType.BREEZE_WIND_CHARGE, ElementType.WIND, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityType.WIND_CHARGE, ElementType.WIND, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityType.TRIDENT, ElementType.WATER, 12f, true);
        registerCustomProjectiles();

        AbloomMod.LOGGER.info("Registered {} elemental projectile types",
                ElementalProjectileRegistry.getRegisteredCount());
    }

    private static void registerCustomProjectiles() {

    }
}
