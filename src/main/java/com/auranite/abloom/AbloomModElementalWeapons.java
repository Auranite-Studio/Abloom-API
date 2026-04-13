package com.auranite.abloom;

import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * 🔹 ОБРАБОТЧИК РЕГИСТРАЦИИ ЭЛЕМЕНТАЛЬНОГО ОРУЖИЯ 🔹
 *
 * Вызывается при инициализации мода для регистрации существующих предметов.
 */
@EventBusSubscriber
public class AbloomModElementalWeapons {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            AbloomMod.LOGGER.info("⚔️ Registering elemental weapons...");

            registerFireWeapons();
            registerPhysicalWeapons();
            registerWindWeapons();
            registerWaterWeapons();
            registerEarthWeapons();
            registerIceWeapons();
            registerElectricWeapons();
            registerSourceWeapons();
            registerNaturalWeapons();
            registerQuantumWeapons();

            AbloomMod.LOGGER.info("✅ Elemental weapon registration complete! Total: {}",
                    ElementalWeaponRegistry.getRegisteredCount());
        });
    }

    private static void registerFireWeapons() {
        ElementalWeaponUtils.registerItem(AbloomModItems.FIRE_STICK.get(), ElementType.FIRE, 50f);
    }

    private static void registerPhysicalWeapons() {
        ElementalWeaponUtils.registerItem(Items.NETHERITE_SWORD, ElementType.PHYSICAL, 5f);
        ElementalWeaponUtils.registerItem(Items.DIAMOND_SWORD, ElementType.PHYSICAL, 5f);
        ElementalWeaponUtils.registerItem(Items.GOLDEN_SWORD, ElementType.PHYSICAL, 4f);
        ElementalWeaponUtils.registerItem(Items.IRON_SWORD, ElementType.PHYSICAL, 3f);
        ElementalWeaponUtils.registerItem(Items.STONE_SWORD, ElementType.PHYSICAL, 2f);
        ElementalWeaponUtils.registerItem(Items.WOODEN_SWORD, ElementType.PHYSICAL, 2f);

        ElementalWeaponUtils.registerItem(Items.NETHERITE_AXE, ElementType.PHYSICAL, 7f);
        ElementalWeaponUtils.registerItem(Items.DIAMOND_AXE, ElementType.PHYSICAL, 7f);
        ElementalWeaponUtils.registerItem(Items.GOLDEN_AXE, ElementType.PHYSICAL, 6f);
        ElementalWeaponUtils.registerItem(Items.IRON_AXE, ElementType.PHYSICAL, 5f);
        ElementalWeaponUtils.registerItem(Items.STONE_AXE, ElementType.PHYSICAL, 4f);
        ElementalWeaponUtils.registerItem(Items.WOODEN_AXE, ElementType.PHYSICAL, 2f);

        ElementalWeaponUtils.registerItem(Items.CROSSBOW, ElementType.PHYSICAL, 7f);
        ElementalWeaponUtils.registerItem(Items.TRIDENT, ElementType.PHYSICAL, 4f);
        ElementalWeaponUtils.registerItem(Items.MACE, ElementType.PHYSICAL, 25f);
        ElementalWeaponUtils.registerItem(Items.BOW, ElementType.PHYSICAL, 2f);

        ElementalWeaponUtils.registerItem(AbloomModItems.PHYSICAL_STICK.get(), ElementType.PHYSICAL, 50f);
    }

    private static void registerWindWeapons() {
        ElementalWeaponUtils.registerItem(AbloomModItems.WIND_STICK.get(), ElementType.WIND, 50f);
    }

    private static void registerWaterWeapons() {
        ElementalWeaponUtils.registerItem(AbloomModItems.WATER_STICK.get(), ElementType.WATER, 50f);
    }

    private static void registerEarthWeapons() {
        ElementalWeaponUtils.registerItem(AbloomModItems.EARTH_STICK.get(), ElementType.EARTH, 50f);
    }
    private static void registerIceWeapons() {
        ElementalWeaponUtils.registerItem(AbloomModItems.ICE_STICK.get(), ElementType.ICE, 50f);
    }
    private static void registerElectricWeapons() {
        ElementalWeaponUtils.registerItem(AbloomModItems.ELECTRIC_STICK.get(), ElementType.ELECTRIC, 50f);
    }
    private static void registerSourceWeapons() {
        ElementalWeaponUtils.registerItem(AbloomModItems.SOURCE_STICK.get(), ElementType.SOURCE, 50f);
    }
    private static void registerNaturalWeapons() {
        ElementalWeaponUtils.registerItem(AbloomModItems.NATURAL_STICK.get(), ElementType.NATURAL, 50f);
    }
    private static void registerQuantumWeapons() {
        ElementalWeaponUtils.registerItem(AbloomModItems.QUANTUM_STICK.get(), ElementType.QUANTUM, 50f);
    }
}