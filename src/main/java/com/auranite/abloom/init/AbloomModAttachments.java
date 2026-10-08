package com.auranite.abloom.init;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.util.IElementalType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AbloomModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, AbloomMod.MODID);

    public static final Supplier<AttachmentType<Map<IElementalType, Integer>>> ELEMENT_ACCUMULATOR =
            ATTACHMENT_TYPES.register("element_accumulator", () ->
                    AttachmentType.<Map<IElementalType, Integer>>builder(() -> new HashMap<>()).build()
            );

    public static final Supplier<AttachmentType<IElementalType>> PROJECTILE_ELEMENT =
            ATTACHMENT_TYPES.register("projectile_element", () ->
                    AttachmentType.<IElementalType>builder(() -> null).build()
            );

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }

    public static Map<IElementalType, Integer> getAccumulator(LivingEntity entity) {
        return entity.getData(ELEMENT_ACCUMULATOR.get());
    }

    public static void addPoints(LivingEntity entity, IElementalType type, int amount) {
        Map<IElementalType, Integer> acc = getAccumulator(entity);
        acc.put(type, acc.getOrDefault(type, 0) + amount);
    }

    public static int getPoints(LivingEntity entity, IElementalType type) {
        return getAccumulator(entity).getOrDefault(type, 0);
    }

    public static void setPoints(LivingEntity entity, IElementalType type, int amount) {
        getAccumulator(entity).put(type, amount);
    }

    public static void resetPoints(LivingEntity entity, IElementalType type) {
        getAccumulator(entity).put(type, 0);
    }

    public static boolean hasReachedThreshold(LivingEntity entity, IElementalType type, int threshold) {
        return getPoints(entity, type) >= threshold;
    }

    public static void clearAllPoints(LivingEntity entity) {
        getAccumulator(entity).clear();
    }

    public static void setProjectileElement(Entity entity, IElementalType type) {
        if (entity != null && !entity.level().isClientSide && type != null) {
            entity.setData(PROJECTILE_ELEMENT.get(), type);
        }
    }

    public static IElementalType getProjectileElement(Entity entity) {
        if (entity != null) {
            return entity.getData(PROJECTILE_ELEMENT.get());
        }
        return null;
    }

    public static boolean hasProjectileElement(Entity entity) {
        IElementalType element = getProjectileElement(entity);
        return element != null;
    }

    public static void clearProjectileElement(Entity entity) {
        if (entity != null && !entity.level().isClientSide) {
            entity.setData(PROJECTILE_ELEMENT.get(), null);
        }
    }

    public static void setPrismConversionType(LivingEntity entity, IElementalType type) {
        if (entity != null && !entity.level().isClientSide && type != null) {
            entity.getPersistentData().putString("Abloom_PrismConversionType", type.name());
        }
    }

    public static IElementalType getPrismConversionType(LivingEntity entity) {
        if (entity != null) {
            String typeName = entity.getPersistentData().getString("Abloom_PrismConversionType");
            if (!typeName.isEmpty()) {
                return IElementalType.byName(typeName).orElse(null);
            }
        }
        return null;
    }

    public static void clearPrismConversionType(LivingEntity entity) {
        if (entity != null) {
            entity.getPersistentData().remove("Abloom_PrismConversionType");
        }
    }

    public static void setFluorescenceType(LivingEntity entity, IElementalType type) {
        if (entity != null && !entity.level().isClientSide && type != null) {
            entity.getPersistentData().putString("Abloom_FluorescenceType", type.name());
        }
    }

    public static IElementalType getFluorescenceType(LivingEntity entity) {
        if (entity != null) {
            String typeName = entity.getPersistentData().getString("Abloom_FluorescenceType");
            if (!typeName.isEmpty()) {
                return IElementalType.byName(typeName).orElse(null);
            }
        }
        return null;
    }

    public static void clearFluorescenceType(LivingEntity entity) {
        if (entity != null) {
            entity.getPersistentData().remove("Abloom_FluorescenceType");
        }
    }
}
