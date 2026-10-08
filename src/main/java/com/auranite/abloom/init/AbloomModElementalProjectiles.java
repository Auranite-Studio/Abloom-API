package com.auranite.abloom.init;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.util.IElementalType;
import com.auranite.abloom.registries.ElementalProjectileRegistry;
import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public class AbloomModElementalProjectiles {

    public static void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(AbloomModElementalProjectiles::registerAll);
    }

    public static void registerAll() {

        // allowOverride = true → projectile берёт элемент из оружия стрелка (attachment) приоритетно
        ElementalProjectileRegistry.registerProjectile(EntityType.FIREBALL, com.auranite.abloom.util.ElementType.FIRE, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityType.SMALL_FIREBALL, com.auranite.abloom.util.ElementType.FIRE, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityType.DRAGON_FIREBALL, com.auranite.abloom.util.ElementType.QUANTUM, 0f, false);

        ElementalProjectileRegistry.registerProjectile(EntityType.FIREWORK_ROCKET, com.auranite.abloom.util.ElementType.PHYSICAL, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityType.WITHER_SKULL, com.auranite.abloom.util.ElementType.QUANTUM, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityType.SHULKER_BULLET, com.auranite.abloom.util.ElementType.WIND, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityType.LLAMA_SPIT, com.auranite.abloom.util.ElementType.WATER, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityType.BREEZE_WIND_CHARGE, com.auranite.abloom.util.ElementType.WIND, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityType.WIND_CHARGE, com.auranite.abloom.util.ElementType.WIND, 0f, false);
        ElementalProjectileRegistry.registerProjectile(EntityType.TRIDENT, com.auranite.abloom.util.ElementType.WATER, 8f, true);
        ElementalProjectileRegistry.registerProjectile(EntityType.SNOWBALL, com.auranite.abloom.util.ElementType.ICE, 0f, true);
        registerCustomProjectiles();

        AbloomMod.LOGGER.info("Registered {} elemental projectile types",
                ElementalProjectileRegistry.getRegisteredCount());
    }

    private static void registerCustomProjectiles() {

    }
}
