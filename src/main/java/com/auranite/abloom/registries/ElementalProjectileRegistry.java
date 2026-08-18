package com.auranite.abloom.registries;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.util.ElementType;
import com.auranite.abloom.handler.ElementDamageHandler;
import com.auranite.abloom.init.AbloomModAttachments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for elemental projectile types and their accumulation multipliers.
 * Allows registration of projectile types, class-based projectile detection,
 * and inherited elemental properties from shooters.
 */
public class ElementalProjectileRegistry {

    private static final Map<EntityType<?>, ElementType> PROJECTILE_ELEMENT_MAP = new ConcurrentHashMap<>();

    private static final Map<EntityType<?>, Float> PROJECTILE_ACCUM_MAP = new ConcurrentHashMap<>();

    private static final Map<Class<? extends Entity>, ElementType> PROJECTILE_CLASS_MAP = new ConcurrentHashMap<>();

    private static final Map<Class<? extends Entity>, Float> PROJECTILE_CLASS_ACCUM_MAP = new ConcurrentHashMap<>();

    private static boolean inheritElementFromShooter = true;

    /**
     * Initializes the projectile registry.
     * @param modEventBus the mod event bus
     */
    public static void register(IEventBus modEventBus) {
        AbloomMod.LOGGER.info("ElementalProjectileRegistry initialized");
    }

    /**
     * Registers a projectile type with its elemental type and accumulation multiplier.
     * @param entityType the projectile entity type
     * @param element the elemental type
     * @param accumulationMultiplier the accumulation multiplier
     */
    public static void registerProjectile(EntityType<?> entityType, ElementType element, float accumulationMultiplier) {
        if (entityType == null || element == null) {
            AbloomMod.LOGGER.warn("Cannot register null projectile type or element");
            return;
        }
        PROJECTILE_ELEMENT_MAP.put(entityType, element);
        PROJECTILE_ACCUM_MAP.put(entityType, accumulationMultiplier);
        AbloomMod.LOGGER.debug("Registered projectile {} → {} (accum: x{})", entityType, element, accumulationMultiplier);
    }

    /**
     * Registers a projectile class with its elemental type and accumulation multiplier.
     * Useful for custom projectile classes that extend base classes.
     * @param entityClass the projectile class
     * @param element the elemental type
     * @param accumulationMultiplier the accumulation multiplier
     */
    public static void registerProjectileByClass(Class<? extends Entity> entityClass, ElementType element, float accumulationMultiplier) {
        if (entityClass == null || element == null) {
            AbloomMod.LOGGER.warn("Cannot register null projectile class or element");
            return;
        }
        PROJECTILE_CLASS_MAP.put(entityClass, element);
        PROJECTILE_CLASS_ACCUM_MAP.put(entityClass, accumulationMultiplier);
        AbloomMod.LOGGER.debug("Registered projectile class {} → {} (accum: x{})", entityClass.getSimpleName(), element, accumulationMultiplier);
    }

    /**
     * Gets the elemental type for a projectile entity type.
     * @param entityType the projectile entity type
     * @return optional containing the elemental type, or empty if not registered
     */
    public static Optional<ElementType> getElementForType(EntityType<?> entityType) {
        return Optional.ofNullable(PROJECTILE_ELEMENT_MAP.get(entityType));
    }

    /**
     * Gets the elemental type for a projectile entity.
     * Checks entity type, class hierarchy, and attachment data.
     * @param entity the projectile entity
     * @return optional containing the elemental type, or empty if not elemental
     */
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

    /**
     * Gets the accumulation multiplier for a projectile entity.
     * @param entity the projectile entity
     * @return optional containing the accumulation multiplier, or empty if not found
     */
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

    /**
     * Checks if a projectile is elemental.
     * @param entity the projectile entity
     * @return true if the projectile has an elemental type
     */
    public static boolean isElementalProjectile(Entity entity) {
        return getElementForEntity(entity).isPresent();
    }

    /**
     * Gets the count of registered projectile types.
     * @return number of registered projectile types
     */
    public static int getRegisteredCount() {
        return PROJECTILE_ELEMENT_MAP.size();
    }

    /**
     * Applies elemental property to a projectile.
     * First checks registered element, then falls back to shooter's weapon element.
     * @param projectile the projectile entity
     * @param shooter the shooter entity
     * @return true if element was applied, false otherwise
     */
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

    /**
     * Sets whether projectiles should inherit element from shooter's weapon.
     * @param value true to enable inheritance, false to disable
     */
    public static void setInheritElementFromShooter(boolean value) {
        inheritElementFromShooter = value;
    }

    /**
     * Checks if projectiles inherit element from shooter.
     * @return true if inheritance is enabled
     */
    public static boolean getInheritElementFromShooter() {
        return inheritElementFromShooter;
    }

    /**
     * Creates and launches a elemental projectile with element from registry or shooter.
     * @param level the server level
     * @param shooter the shooter
     * @param projectileType the projectile entity type
     * @param velocity the launch velocity
     * @param inaccuracy the launch inaccuracy
     * @return the created projectile, or null if creation failed
     */
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

    /**
     * Creates a elemental projectile with a forced elemental type override.
     * @param level the server level
     * @param shooter the shooter
     * @param projectileType the projectile entity type
     * @param forcedElement the forced elemental type
     * @param velocity the launch velocity
     * @param inaccuracy the launch inaccuracy
     * @return the created projectile, or null if creation failed
     */
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
