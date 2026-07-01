package com.auranite.abloom;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

import java.util.Map;

@EventBusSubscriber(modid = AbloomMod.MODID)
public class ArmorResistanceApplier {

    @SubscribeEvent
    public static void applyDatapackResistances(ModifyDefaultComponentsEvent event) {
        // Apply datapack armor resistances to items
        var allResistances = ArmorResistanceRegistry.getAllRegisteredResistances();
        
        for (var entry : allResistances.entrySet()) {
            var itemLocation = entry.getKey();
            var resistances = entry.getValue();
            
            var optionalItem = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(itemLocation);
            if (optionalItem.isPresent()) {
                var item = optionalItem.get();
                
                // Build the custom data with resistances
                CustomData resistancesData = CustomData.EMPTY.update(tag -> {
                    var resistanceTag = tag.getCompoundOrEmpty("elemental_resistance_bonus");
                    for (Map.Entry<ElementType, Float> resistanceEntry : resistances.entrySet()) {
                        ElementType type = resistanceEntry.getKey();
                        Float value = resistanceEntry.getValue();
                        if (type != null && value != null) {
                            resistanceTag.putFloat(type.name(), value);
                        }
                    }
                    tag.put("elemental_resistance_bonus", resistanceTag);
                });
                
                // Apply to the item using the builder
                event.modify(item, builder -> builder.set(DataComponents.CUSTOM_DATA, resistancesData));
            }
        }
    }
}
