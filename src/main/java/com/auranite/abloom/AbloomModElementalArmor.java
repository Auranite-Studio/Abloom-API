package com.auranite.abloom;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber
public class AbloomModElementalArmor {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            AbloomMod.LOGGER.info("🛡️ Registering elemental armor resistances...");

            registerFireResistArmor();
            registerPhysicalResistArmor();
            registerWindResistArmor();
            registerWaterResistArmor();
            registerEarthResistArmor();
            registerIceResistArmor();
            registerElectricResistArmor();
            registerEnergyResistArmor();
            registerNaturalResistArmor();
            registerQuantumResistArmor();

            AbloomMod.LOGGER.info("✅ Elemental armor registration complete!");
        });
    }

    private static void registerFireResistArmor() {
        // Пример: броня с сопротивлением к огню
        Map<ElementType, Float> fireResist = new HashMap<>();
        fireResist.put(ElementType.FIRE, 0.15f);
        ElementalResistanceUtils.registerArmor(AbloomModItems.FIRE_STICK.get(), fireResist);
    }

    private static void registerPhysicalResistArmor() {
        // Пример: броня с сопротивлением к физическому урону
        Map<ElementType, Float> physicalResist = new HashMap<>();
        physicalResist.put(ElementType.PHYSICAL, 0.10f);
        ElementalResistanceUtils.registerArmor(AbloomModItems.PHYSICAL_STICK.get(), physicalResist);
    }

    private static void registerWindResistArmor() {
        Map<ElementType, Float> windResist = new HashMap<>();
        windResist.put(ElementType.WIND, 0.15f);
        ElementalResistanceUtils.registerArmor(AbloomModItems.WIND_STICK.get(), windResist);
    }

    private static void registerWaterResistArmor() {
        Map<ElementType, Float> waterResist = new HashMap<>();
        waterResist.put(ElementType.WATER, 0.15f);
        ElementalResistanceUtils.registerArmor(AbloomModItems.WATER_STICK.get(), waterResist);
    }

    private static void registerEarthResistArmor() {
        Map<ElementType, Float> earthResist = new HashMap<>();
        earthResist.put(ElementType.EARTH, 0.15f);
        ElementalResistanceUtils.registerArmor(AbloomModItems.EARTH_STICK.get(), earthResist);
    }

    private static void registerIceResistArmor() {
        Map<ElementType, Float> iceResist = new HashMap<>();
        iceResist.put(ElementType.ICE, 0.15f);
        ElementalResistanceUtils.registerArmor(AbloomModItems.ICE_STICK.get(), iceResist);
    }

    private static void registerElectricResistArmor() {
        Map<ElementType, Float> electricResist = new HashMap<>();
        electricResist.put(ElementType.ELECTRIC, 0.15f);
        ElementalResistanceUtils.registerArmor(AbloomModItems.ELECTRIC_STICK.get(), electricResist);
    }

    private static void registerEnergyResistArmor() {
        Map<ElementType, Float> energyResist = new HashMap<>();
        energyResist.put(ElementType.ENERGY, 0.15f);
        ElementalResistanceUtils.registerArmor(AbloomModItems.ENERGY_STICK.get(), energyResist);
    }

    private static void registerNaturalResistArmor() {
        Map<ElementType, Float> naturalResist = new HashMap<>();
        naturalResist.put(ElementType.NATURAL, 0.15f);
        ElementalResistanceUtils.registerArmor(AbloomModItems.NATURAL_STICK.get(), naturalResist);
    }

    private static void registerQuantumResistArmor() {
        Map<ElementType, Float> quantumResist = new HashMap<>();
        quantumResist.put(ElementType.QUANTUM, 0.15f);
        ElementalResistanceUtils.registerArmor(AbloomModItems.QUANTUM_STICK.get(), quantumResist);
    }
}
