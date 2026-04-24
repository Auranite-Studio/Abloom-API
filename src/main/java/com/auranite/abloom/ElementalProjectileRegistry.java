package com.auranite.abloom;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ElementalProjectileRegistry {

    private static final Map<EntityType<?>, ElementType> PROJECTILE_ELEMENT_MAP = new ConcurrentHashMap<>();

    private static final Map<EntityType<?>, Float> PROJECTILE_ACCUM_MAP = new ConcurrentHashMap<>();

    private static final Map<Class<? extends Entity>, ElementType> PROJECTILE_CLASS_MAP = new ConcurrentHashMap<>();

    private static final Map<Class<? extends Entity>, Float> PROJECTILE_CLASS_ACCUM_MAP = new ConcurrentHashMap<>();

    private static boolean inheritElementFromShooter = true;

    public static void register(IEventBus modEventBus) {
        AbloomMod.LOGGER.info("ElementalProjectileRegistry initialized");
    }

    public static void registerProjectile(EntityType<?> entityType, ElementType element, float accumulationMultiplier) {
        if (entityType == null || element == null) {
            AbloomMod.LOGGER.warn("Cannot register null projectile type or element");
            return;
        }
        PROJECTILE_ELEMENT_MAP.put(entityType, element);
        PROJECTILE_ACCUM_MAP.put(entityType, accumulationMultiplier);
        AbloomMod.LOGGER.debug("Registered projectile {} → {} (accum: x{})", entityType, element, accumulationMultiplier);
    }

    public static void registerProjectileByClass(Class<? extends Entity> entityClass, ElementType element, float accumulationMultiplier) {
        if (entityClass == null || element == null) {
            AbloomMod.LOGGER.warn("Cannot register null projectile class or element");
            return;
        }
        PROJECTILE_CLASS_MAP.put(entityClass, element);
        PROJECTILE_CLASS_ACCUM_MAP.put(entityClass, accumulationMultiplier);
        AbloomMod.LOGGER.debug("Registered projectile class {} → {} (accum: x{})", entityClass.getSimpleName(), element, accumulationMultiplier);
    }

    public static Optional<ElementType> getElementForType(EntityType<?> entityType) {
        return Optional.ofNullable(PROJECTILE_ELEMENT_MAP.get(entityType));
    }

    public static Optional<ElementType> getElementForEntity(Entity entity) {
        if (entity == null) return Optional.empty();

        ElementType byType = PROJECTILE_ELEMENT_MAP.get(entity.getType());
        if (byType != null) return Optional.of(byType);

        for (Map.Entry<Class<? extends Entity>, ElementType> entry : PROJECTILE_CLASS_MAP.entrySet()) {
            if (entry.getKey().isInstance(entity)) {
                return Optional.of(entry.getValue());
            }
        }

        if (AbloomModAttachments.hasProjectileElement(entity)) {
            return Optional.ofNullable(AbloomModAttachments.getProjectileElement(entity));
        }

        return Optional.empty();
    }

    public static Optional<Float> getAccumulationMultiplierForEntity(Entity entity) {
        if (entity == null) return Optional.empty();

        Float byType = PROJECTILE_ACCUM_MAP.get(entity.getType());
        if (byType != null) return Optional.of(byType);

        for (Map.Entry<Class<? extends Entity>, Float> entry : PROJECTILE_CLASS_ACCUM_MAP.entrySet()) {
            if (entry.getKey().isInstance(entity)) {
                return Optional.of(entry.getValue());
            }
        }

        return Optional.empty();
    }

    public static boolean isElementalProjectile(Entity entity) {
        return getElementForEntity(entity).isPresent();
    }

    public static int getRegisteredCount() {
        return PROJECTILE_ELEMENT_MAP.size();
    }

    public static boolean applyElementToProjectile(Entity projectile, LivingEntity shooter) {
        if (projectile == null || projectile.level().isClientSide()) return false;

        Optional<ElementType> registeredElement = getElementForEntity(projectile);
        ElementType elementToApply = null;

        if (registeredElement.isPresent()) {
            elementToApply = registeredElement.get();
        }
        else if (inheritElementFromShooter && shooter != null) {
            net.minecraft.world.item.ItemStack weapon = shooter.getMainHandItem();
            elementToApply = ElementDamageHandler.getElementTypeFromItem(weapon);
        }

        if (elementToApply != null) {
            AbloomModAttachments.setProjectileElement(projectile, elementToApply);
            return true;
        }

        return false;
    }

    public static void setInheritElementFromShooter(boolean value) {
        inheritElementFromShooter = value;
    }

    public static boolean getInheritElementFromShooter() {
        return inheritElementFromShooter;
    }

    public static <T extends Entity> T createAndLaunchElementalProjectile(
            net.minecraft.server.level.ServerLevel level,
            LivingEntity shooter,
            EntityType<T> projectileType,
            float velocity,
            float inaccuracy
    ) {
        T projectile = projectileType.create(level, EntitySpawnReason.NATURAL);
        if (projectile == null) return null;

        projectile.snapTo(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ(),
                shooter.getYHeadRot(), shooter.getXRot());

        applyElementToProjectile(projectile, shooter);

        if (projectile instanceof net.minecraft.world.entity.projectile.Projectile proj) {
            proj.shootFromRotation(shooter, shooter.getXRot(), shooter.getYHeadRot(), 0.0F, velocity, inaccuracy);
            proj.setOwner(shooter);
        }

        level.addFreshEntity(projectile);
        return projectile;
    }

    public static <T extends Entity> T createElementalProjectileWithOverride(
            net.minecraft.server.level.ServerLevel level,
            LivingEntity shooter,
            EntityType<T> projectileType,
            ElementType forcedElement,
            float velocity,
            float inaccuracy
    ) {
        T projectile = projectileType.create(level, EntitySpawnReason.NATURAL);
        if (projectile == null) return null;

        projectile.snapTo(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ(),
                shooter.getYHeadRot(), shooter.getXRot());

        if (forcedElement != null && !level.isClientSide()) {
            AbloomModAttachments.setProjectileElement(projectile, forcedElement);
        }

        if (projectile instanceof net.minecraft.world.entity.projectile.Projectile proj) {
            proj.shootFromRotation(shooter, shooter.getXRot(), shooter.getYHeadRot(), 0.0F, velocity, inaccuracy);
            proj.setOwner(shooter);
        }

        level.addFreshEntity(projectile);
        return projectile;
    }
}
