package com.auranite.abloom;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkDataEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Record for critical hit result containing modified damage and crit flag. */
record CritResult(float damage, boolean isCrit) {}

/**
 * Handles elemental damage calculations, accumulation tracking, and threshold effects.
 * This class manages the core mechanics of the Abloom API including:
 * <ul>
 *   <li>Resonance accumulation tracking</li>
 *   <li>Threshold-based effect activation</li>
 *   <li>Damage calculation with elemental modifiers</li>
 *   <li>Display number and status text management</li>
 * </ul>
 * 
 * <p>This mod uses a priority-based system for damage event handling to avoid conflicts
 * with other mods that may also modify damage. Use the {@link DamagePriority} annotation
 * on event handlers to control processing order.</p>
 * 
 * <p>For integration with other mods, use {@link DamageModificationManager} to register
 * damage modification callbacks with specific priorities.</p>
 */
@EventBusSubscriber(modid = AbloomMod.MODID)
public class ElementDamageHandler {

    private static float baseAccumulation = 0f;
    private static final int THRESHOLD = 100;
    private static final int RESET_DELAY_TICKS = 300;

    private static final Map<Integer, Long> DAMAGE_COOLDOWNS = new ConcurrentHashMap<>();
    private static final int COOLDOWN_TICKS = 5;

    private static final Map<Integer, Map<ElementType, Long>> LAST_DAMAGE_TIME = new ConcurrentHashMap<>();
    private static final Object LAST_DAMAGE_LOCK = new Object();

    private static MinecraftServer currentServer = null;
    private static int serverTickCounter = 0;
    private static final int CLEANUP_INTERVAL = 20;

    private static ElementDamageDisplayManager displayManager;

    private static final ThreadLocal<Boolean> IS_PROCESSING_DAMAGE = ThreadLocal.withInitial(() -> false);

    private static final int MAX_ACTIVE_DISPLAYS = 500;
    private static int currentDisplayCount = 0;
    private static final Object DISPLAY_COUNT_LOCK = new Object();

    /**
     * Process damage with priority handling to avoid conflicts with other mods.
     * This method checks if damage was already modified by another mod with higher priority
     * and adjusts accumulation accordingly.
     *
     * Uses DamageModificationManager to process damage modifications in priority order.
     *
     * @param target the living entity taking damage
     * @param source the damage source
     * @param baseDamage the initial damage before any modifications
     * @param modifiedDamage the current damage (may have been modified by other mods)
     * @return the final damage after all calculations
     */
    public static float processDamageWithPriority(LivingEntity target, DamageSource source, float baseDamage, float modifiedDamage) {
        if (target == null || source == null) return modifiedDamage;

        // Check if current damage differs from base damage
        // If it does, another mod has already modified it
        if (modifiedDamage != baseDamage) {
            // Another mod modified the damage, we need to:
            // 1. Track accumulation based on the modified damage
            // 2. Call doProcessLivingHurt to process elemental calculations
            // 3. Then apply low-priority modifiers
            ElementType type = getElementTypeFromSource(source);
            if (type != null) {
                // Add accumulation points based on the modified damage (not just 1 point)
                int pointsToAdd = ElementResistanceManager.calculateAccumulationPoints(target, type, 1);
                if (pointsToAdd > 0) {
                    AbloomModAttachments.addPoints(target, type, pointsToAdd);
                    updateLastDamageTime(target, type);
                }
            }
            
            // Process damage through doProcessLivingHurt with the modified damage
            // This ensures elemental calculations and display manager are called
            float processedDamage = processLivingHurtInternal(target, source, baseDamage, modifiedDamage);
            
            // Apply low-priority modifiers AFTER Abloom
            float finalDamage = DamageModificationManager.processLowPriorityDamage(target, source, processedDamage);
            return finalDamage;
        }

        // Use DamageModificationManager to process damage modifications BEFORE Abloom
        // This ensures all registered modifiers are called in priority order
        // Mods with priority > 0 (HIGH priority) will process BEFORE this handler
        float processedDamage = baseDamage;
        
        // Process high-priority modifiers (priority > 0) BEFORE Abloom
        processedDamage = DamageModificationManager.processHighPriorityDamage(target, source, processedDamage);

        // Normal processing flow - no other mod modified damage yet
        float finalDamage = processLivingHurtInternal(target, source, baseDamage, processedDamage);

        // Process low-priority modifiers (priority <= 0) AFTER Abloom
        finalDamage = DamageModificationManager.processLowPriorityDamage(target, source, finalDamage);

        return finalDamage;
    }

    /**
     * Internal damage processing logic that handles elemental calculations.
     * This method should only be called once per damage event.
     *
     * @param target the living entity taking damage
     * @param source the damage source
     * @param baseDamage the initial damage before any modifications
     * @param currentDamage the current damage value (may be modified by other mods)
     * @return the final damage after all calculations
     */
    private static float processLivingHurtInternal(LivingEntity target, DamageSource source, float baseDamage, float currentDamage) {
        IS_PROCESSING_DAMAGE.set(true);
        try {
            return doProcessLivingHurt(target, source, baseDamage, currentDamage);
        } finally {
            IS_PROCESSING_DAMAGE.set(false);
        }
    }

    /**
     * Sets the display manager for rendering damage numbers and status texts.
     * @param manager the display manager instance
     */
    public static void setDisplayManager(ElementDamageDisplayManager manager) {
        displayManager = manager;
    }

    /**
     * Initializes damage colors for all element types.
     * Must be called during mod initialization before any displays are spawned.
     */
    public static void initDamageColors() {
        ElementDamageDisplayManager.registerDamageColor(ElementType.FIRE, 0xFF5500);
        ElementDamageDisplayManager.registerDamageColor(ElementType.PHYSICAL, 0xC0C0C0);
        ElementDamageDisplayManager.registerDamageColor(ElementType.WIND, 0x00FFFF);
        ElementDamageDisplayManager.registerDamageColor(ElementType.WATER, 0x0080FF);
        ElementDamageDisplayManager.registerDamageColor(ElementType.EARTH, 0x8B4513);
        ElementDamageDisplayManager.registerDamageColor(ElementType.ICE, 0x00BFFF);
        ElementDamageDisplayManager.registerDamageColor(ElementType.ELECTRIC, 0xFF19FF);
        ElementDamageDisplayManager.registerDamageColor(ElementType.ENERGY, 0xFFFF00);
        ElementDamageDisplayManager.registerDamageColor(ElementType.NATURAL, 0x32CD32);
        ElementDamageDisplayManager.registerDamageColor(ElementType.QUANTUM, 0x9400D3);
        ElementDamageDisplayManager.registerDamageColor(ElementType.ETHER, 0x24B3A7);
        ElementDamageDisplayManager.registerDamageColor(ElementType.LIGHT, 0xFFFFE0);
        ElementDamageDisplayManager.registerDamageColor(ElementType.SHADOW, 0x4B0082);
    }

    /**
     * Checks if a damage display can be spawned ( respects max display limit ).
     * @return true if display can be spawned, false if limit reached
     */
    public static boolean canSpawnDisplay() {
        synchronized (DISPLAY_COUNT_LOCK) {
            return currentDisplayCount < MAX_ACTIVE_DISPLAYS;
        }
    }

    /**
     * Increments the count of active damage displays.
     * Should only be called when spawning a new display.
     */
    public static void incrementDisplayCount() {
        synchronized (DISPLAY_COUNT_LOCK) {
            currentDisplayCount++;
        }
    }

    /**
     * Decrements the count of active damage displays.
     * Should only be called when removing a display.
     */
    public static void decrementDisplayCount() {
        synchronized (DISPLAY_COUNT_LOCK) {
            currentDisplayCount = Math.max(0, currentDisplayCount - 1);
        }
    }

    /**
     * Checks if damage is currently being processed by the elemental system.
     * @return true if damage is being processed
     */
    public static boolean isProcessingDamage() {
        return IS_PROCESSING_DAMAGE.get();
    }

    /**
     * Gets the current count of active damage displays.
     * @return number of active displays
     */
    public static int getCurrentDisplayCount() {
        synchronized (DISPLAY_COUNT_LOCK) {
            return currentDisplayCount;
        }
    }

    /**
     * Handles server tick events for display cleanup and accumulation reset.
     * Processes pending removals and checks for inactive accumulation points.
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        currentServer = event.getServer();
        if (displayManager != null) displayManager.processPendingRemovals();
        serverTickCounter++;
        if (serverTickCounter >= CLEANUP_INTERVAL) {
            serverTickCounter = 0;
            checkAndResetInactivePoints();
            if (displayManager != null) displayManager.cleanupStaleDisplays();
        }
    }

    /**
     * Handles LivingDamageEvent.Pre with low priority to process elemental calculations.
     * This ensures Abloom processes damage after mods with NORMAL priority (0) and before
     * mods with LOWEST priority (-300).
     * 
     * Modifiers with priority > 0 (HIGH priority) are processed through DamageModificationManager
     * BEFORE this handler, allowing other mods to modify damage first.
     * 
     * Modifiers with priority <= 0 (NORMAL and below) are processed AFTER Abloom's calculations.
     * 
     * Other mods can register damage modifiers via DamageModificationManager to control
     * their processing order relative to Abloom.
     * 
     * For mods that directly modify event.setNewDamage(), Abloom will see the modified damage
     * and add appropriate accumulation points. Abloom will NOT overwrite damage that was modified
     * by other mods.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        if (IS_PROCESSING_DAMAGE.get()) return;
        
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        float baseDamage = event.getOriginalDamage();
        float currentDamage = event.getNewDamage();
        
        // Process damage with priority handling
        float finalDamage = processDamageWithPriority(target, source, baseDamage, currentDamage);
        
        // Only set damage if it was modified by Abloom (not by other mods)
        // If currentDamage != baseDamage, another mod has already modified the damage
        // We should NOT overwrite that modification
        if (finalDamage != currentDamage && currentDamage == baseDamage) {
            // Damage was modified by Abloom and not by other mods
            event.setNewDamage(finalDamage);
        } else if (currentDamage != baseDamage) {
            // Another mod modified the damage - we already added accumulation points
            // but should NOT overwrite their modification
        }
    }

    private static float doProcessLivingHurt(LivingEntity target, DamageSource source, float baseDamage, float currentDamage) {
        LivingEntity attacker = source.getEntity() instanceof LivingEntity e ? e : null;
        boolean erosionActive = target.hasEffect(AbloomModEffects.WINDSWEPT);

        ElementType type = getElementTypeFromSource(source);
        if (type == null) {
            if (canShowDamage(target)) spawnDamageNumber(target, currentDamage, null);
            return currentDamage;
        }

        float damageMultiplier = 1.0f;

        // Modifiers from attacker
        if (attacker != null && attacker.hasEffect(AbloomModEffects.SHOCK)) {
            int amplifier = attacker.getEffect(AbloomModEffects.SHOCK).getAmplifier();
            float reduction = 1.0f - ((amplifier + 1) * 0.20f);
            reduction = Math.max(0.1f, reduction);
            damageMultiplier *= reduction;
        }

        // Modifiers from target
        if (target.hasEffect(AbloomModEffects.OVERLOAD)) {
            int amplifier = target.getEffect(AbloomModEffects.OVERLOAD).getAmplifier();
            damageMultiplier *= 1.0f + (amplifier + 1) * 0.20f;
        }
        if (target.hasEffect(AbloomModEffects.BLOOM)) {
            int amplifier = target.getEffect(AbloomModEffects.BLOOM).getAmplifier();
            damageMultiplier *= 1.0f + (amplifier + 1) * 0.20f;
        }
        if (target.hasEffect(AbloomModEffects.DISPERSION)) {
            float dispersionBonus = getDispersionBonus(type);
            if (AbloomMod.LOGGER.isDebugEnabled()) {
                AbloomMod.LOGGER.debug("Applying dispersion bonus of {} for type {}", dispersionBonus, type);
            }
            damageMultiplier *= 1.0f + dispersionBonus;
        }

        float damage = currentDamage * damageMultiplier;

        float effectiveAccumMultiplier = 1.0f;
        if (source.getDirectEntity() != null) {
            Optional<Float> projectileAccum = ElementalProjectileRegistry.getAccumulationMultiplierForEntity(source.getDirectEntity());
            if (projectileAccum.isPresent()) effectiveAccumMultiplier = projectileAccum.get();
        }
        if (effectiveAccumMultiplier == 1.0f && source.getEntity() instanceof LivingEntity attackerEntity) {
            ItemStack weapon = attackerEntity.getMainHandItem();
            float weaponAccum = ElementalWeaponRegistry.getAccumulationMultiplier(weapon);
            float componentAccum = ElementalWeaponComponent.getAccumMultiplier(weapon);
            if (componentAccum != 1.0f) effectiveAccumMultiplier = componentAccum;
            else if (weaponAccum != 1.0f) effectiveAccumMultiplier = weaponAccum;
        }

        if (target.hasEffect(AbloomModEffects.BLOOM)) {
            int amplifier = target.getEffect(AbloomModEffects.BLOOM).getAmplifier();
            effectiveAccumMultiplier *= 1.20f * (amplifier + 1);
        }
        if (target.hasEffect(AbloomModEffects.WETNESS)) {
            int amplifier = target.getEffect(AbloomModEffects.WETNESS).getAmplifier();
            effectiveAccumMultiplier *= 1.0f + (amplifier + 1) * 1.0f;
        }

        float armorResistanceBonus = getArmorResistanceBonus(target, type);

        int basePoints = (int) baseAccumulation;
        int pointsToAdd = Math.round(basePoints + effectiveAccumMultiplier);
        if (AbloomMod.LOGGER.isDebugEnabled()) {
            AbloomMod.LOGGER.debug("Base accumulation points: {} (base: {}, multiplier: {})", pointsToAdd, basePoints, effectiveAccumMultiplier);
        }
        pointsToAdd = ElementResistanceManager.calculateAccumulationPoints(target, type, pointsToAdd);
        if (AbloomMod.LOGGER.isDebugEnabled()) {
            AbloomMod.LOGGER.debug("Final accumulation points after resistance: {} (entity: {}, type: {})", pointsToAdd, target.getName().getString(), type);
        }

        if (erosionActive && type != ElementType.WIND) {
            spawnStatusText(target, Component.translatable("elemental.tooltip.vortex_convert"), 0x00FFFF);
            pointsToAdd = 100;
            target.removeEffect(AbloomModEffects.WINDSWEPT);
        }

        AbloomModAttachments.addPoints(target, type, pointsToAdd);
        int pointsAfter = AbloomModAttachments.getPoints(target, type);
        boolean thresholdReached = pointsAfter >= THRESHOLD;
        if (AbloomMod.LOGGER.isDebugEnabled()) {
            AbloomMod.LOGGER.debug("Accumulation threshold check: {}/{} points. Threshold reached: {}", pointsAfter, THRESHOLD, thresholdReached);
        }

        float finalDamage = damage;
        finalDamage = ElementResistanceManager.calculateReducedDamage(target, type, finalDamage);
        
        finalDamage = applyArmorResistance(finalDamage, armorResistanceBonus);

        CritResult critResult = applyCriticalHit(attacker, finalDamage);
        finalDamage = critResult.damage();
        boolean isCrit = critResult.isCrit();

        if (thresholdReached) {
            if (AbloomMod.LOGGER.isDebugEnabled()) {
                AbloomMod.LOGGER.debug("Accumulation threshold reached for {} (type: {}). Applying effect.", target.getName().getString(), type);
            }
            finalDamage = applyThresholdEffect(target, type, finalDamage);
            AbloomModAttachments.resetPoints(target, type);
        }

        if (canShowDamage(target)) spawnDamageNumber(target, finalDamage, type, isCrit);
        updateLastDamageTime(target, type);
        return finalDamage;
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        clearActiveDisplays(entity);
        DAMAGE_COOLDOWNS.remove(entity.getId());
        synchronized (LAST_DAMAGE_LOCK) {
            LAST_DAMAGE_TIME.remove(entity.getId());
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity livingEntity) {
            clearActiveDisplays(livingEntity);
            DAMAGE_COOLDOWNS.remove(entity.getId());
            synchronized (LAST_DAMAGE_LOCK) {
                LAST_DAMAGE_TIME.remove(entity.getId());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (displayManager != null) {
            displayManager.clearActiveDisplays(player);
        }
        int playerId = player.getId();
        DAMAGE_COOLDOWNS.remove(playerId);
        synchronized (LAST_DAMAGE_LOCK) {
            LAST_DAMAGE_TIME.remove(playerId);
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkDataEvent.Save event) {
        if (displayManager == null) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        int chunkX = event.getChunk().getPos().x();
        int chunkZ = event.getChunk().getPos().z();
        displayManager.cleanupDisplaysInChunk(level, chunkX, chunkZ);
    }

    private static ElementType getElementTypeFromSource(DamageSource source) {
        Entity directEntity = source.getDirectEntity();
        if (directEntity != null) {
            Optional<ElementType> registryElement = ElementalProjectileRegistry.getElementForEntity(directEntity);
            if (registryElement.isPresent()) return registryElement.get();
            if (AbloomModAttachments.hasProjectileElement(directEntity))
                return AbloomModAttachments.getProjectileElement(directEntity);
        }
        Entity causingEntity = source.getEntity();
        if (causingEntity instanceof LivingEntity attacker) {
            ItemStack weapon = attacker.getMainHandItem();
            Optional<ElementType> componentType = ElementalWeaponComponent.getElement(weapon);
            if (componentType.isPresent()) return componentType.get();
            ElementType registryType = ElementalWeaponRegistry.getElementType(weapon);
            if (registryType != null) return registryType;
        }
        String msgId = source.type().msgId();
        if (msgId != null) {
            for (ElementType type : ElementType.values()) {
                if (type.getDamageTypeId().equals(msgId) || type.getFullDamageTypeId().equals(msgId)) return type;
            }
            ElementType vanillaType = ElementType.fromVanillaDamageType(msgId);
            if (vanillaType != null) return vanillaType;
        }
        return null;
    }

    public static ElementType getElementTypeFromItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        Optional<ElementType> componentType = ElementalWeaponComponent.getElement(stack);
        if (componentType.isPresent()) return componentType.get();
        return ElementalWeaponRegistry.getElementType(stack);
    }

    public static ItemStack createElementalItem(net.minecraft.world.item.Item item, ElementType type, int count) {
        return ElementalWeaponComponent.withElement(new ItemStack(item, count), type);
    }

    public static ItemStack createElementalItemWithAccum(net.minecraft.world.item.Item item, ElementType type, int count, float accumPoints) {
        return ElementalWeaponComponent.withElementAndAccum(new ItemStack(item, count), type, accumPoints);
    }

    private static void updateLastDamageTime(LivingEntity entity, ElementType type) {
        synchronized (LAST_DAMAGE_LOCK) {
            LAST_DAMAGE_TIME.computeIfAbsent(entity.getId(), k -> new EnumMap<>(ElementType.class)).put(type, entity.level().getGameTime());
        }
    }

    private static void checkAndResetInactivePoints() {
        if (currentServer == null) return;
        long currentTime = currentServer.overworld().getGameTime();
        long expiryTime = currentTime - RESET_DELAY_TICKS;
        synchronized (LAST_DAMAGE_LOCK) {
            Iterator<Map.Entry<Integer, Map<ElementType, Long>>> entityIterator = LAST_DAMAGE_TIME.entrySet().iterator();
            while (entityIterator.hasNext()) {
                Map.Entry<Integer, Map<ElementType, Long>> entityEntry = entityIterator.next();
                int entityId = entityEntry.getKey();
                Map<ElementType, Long> typeTimes = entityEntry.getValue();
                
                LivingEntity livingEntity = null;
                for (ServerLevel level : currentServer.getAllLevels()) {
                    Entity entity = level.getEntity(entityId);
                    if (entity instanceof LivingEntity le && le.isAlive()) {
                        livingEntity = le;
                        break;
                    }
                }
                
                if (livingEntity == null) {
                    entityIterator.remove();
                    continue;
                }
                
                Iterator<Map.Entry<ElementType, Long>> typeIterator = typeTimes.entrySet().iterator();
                while (typeIterator.hasNext()) {
                    Map.Entry<ElementType, Long> typeEntry = typeIterator.next();
                    if (typeEntry.getValue() <= expiryTime) {
                        AbloomModAttachments.resetPoints(livingEntity, typeEntry.getKey());
                        typeIterator.remove();
                    }
                }
                
                if (typeTimes.isEmpty()) entityIterator.remove();
            }
        }
    }

    private static boolean canShowDamage(LivingEntity entity) {
        long currentTime = entity.level().getGameTime();
        Long lastTime = DAMAGE_COOLDOWNS.get(entity.getId());
        if (lastTime != null && currentTime - lastTime < COOLDOWN_TICKS) return false;
        DAMAGE_COOLDOWNS.put(entity.getId(), currentTime);
        return true;
    }

    private static void clearActiveDisplays(LivingEntity entity) {
        if (displayManager != null) displayManager.clearActiveDisplays(entity);
    }

    /**
     * Applies critical hit logic to the damage.
     * Combines entity attributes and weapon crit values additively.
     *
     * @param attacker the attacking entity (can be null)
     * @param baseDamage the damage before crit application
     * @return CritResult with modified damage and crit flag
     */
    private static CritResult applyCriticalHit(LivingEntity attacker, float baseDamage) {
        if (attacker == null) {
            return new CritResult(baseDamage, false);
        }

        // Get crit chance from entity attributes
        AttributeInstance critChanceAttr = attacker.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AbloomAttributes.CRIT_CHANCE));
        double entityCritChanceVal = critChanceAttr != null ? critChanceAttr.getValue() : AbloomAttributes.CRIT_CHANCE_BASE;
        // Subtract base value to get only modifiers
        double entityCritChance = Math.max(0.0, entityCritChanceVal - AbloomAttributes.CRIT_CHANCE_BASE);

        // Get crit chance from weapon
        ItemStack weapon = attacker.getMainHandItem();
        double weaponCritChance = ElementalWeaponRegistry.getCritChance(weapon)
                + ElementalWeaponComponent.getCritChance(weapon);

        // Get crit damage from entity attributes
        AttributeInstance critDamageAttr = attacker.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AbloomAttributes.CRIT_DAMAGE));
        double entityCritDamageVal = critDamageAttr != null ? critDamageAttr.getValue() : AbloomAttributes.CRIT_DAMAGE_BASE;
        // Subtract base value to get only modifiers
        double entityCritDamage = Math.max(0.0, entityCritDamageVal - AbloomAttributes.CRIT_DAMAGE_BASE);

        // Get crit damage from weapon
        double weaponCritDamage = ElementalWeaponRegistry.getCritDamage(weapon)
                + ElementalWeaponComponent.getCritDamage(weapon);

        // Sum additively (as per user request)
        double totalCritChance = Math.min(1.0, entityCritChance + weaponCritChance);
        double totalCritDamage = Math.min(10.0, entityCritDamage + weaponCritDamage);

        // Random check
        if (attacker.level().getRandom().nextFloat() < totalCritChance) {
            float critDamage = baseDamage * (1.0f + (float) totalCritDamage);
            return new CritResult(critDamage, true);
        }

        return new CritResult(baseDamage, false);
    }

    private static void spawnDamageNumber(LivingEntity entity, float amount, ElementType type) {
        spawnDamageNumber(entity, amount, type, false);
    }

    private static void spawnDamageNumber(LivingEntity entity, float amount, ElementType type, boolean isCrit) {
        if (!canSpawnDisplay()) return;
        if (displayManager != null) {
            incrementDisplayCount();
            displayManager.spawnDamageNumber(entity, amount, type, isCrit);
        }
    }

    public static void spawnStatusText(LivingEntity entity, Component textComponent, int color) {
        if (!canSpawnDisplay() || displayManager == null) return;
        incrementDisplayCount();
        displayManager.spawnStatusText(entity, textComponent, color);
    }

    public static void spawnStatusText(LivingEntity entity, String text, int color) {
        spawnStatusText(entity, Component.literal(text), color);
    }

    public static int getThreshold() {
        return THRESHOLD;
    }

    public static void setThreshold(int threshold) {
    }

    public static void setDamageColor(ElementType type, int color) {
        ElementDamageDisplayManager.setDamageColor(type, color);
    }

    public static Map<ElementType, Integer> getAllDamageColors() {
        return ElementDamageDisplayManager.getAllDamageColors();
    }

    private static float getArmorResistanceBonus(LivingEntity entity, ElementType type) {
        if (entity == null || type == null) return 0.0f;

        float totalResistance = 0.0f;

        for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR) {
            ItemStack armorStack = entity.getItemBySlot(slot);
            if (!armorStack.isEmpty()) {
                float resistance = ElementalResistanceComponent.getResistance(armorStack, type);
                totalResistance += resistance;
            }
        }
        
        return Math.max(-0.99f, Math.min(totalResistance, 0.99f));
    }

    private static float applyArmorResistance(float damage, float resistanceBonus) {
        float multiplier = 1.0f - resistanceBonus;
        // multiplier > 1.0 when resistanceBonus < 0 (penetration increases damage)
        // multiplier < 1.0 when resistanceBonus > 0 (resistance decreases damage)
        return Math.max(0.0f, damage * multiplier);
    }

    private static float applyThresholdEffect(LivingEntity target, ElementType type, float originalDamage) {
        return switch (type) {
            case LIGHT -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.DISPERSION, 200, 0, false, true));
                spawnStatusText(target, Component.translatable("elemental.tooltip.light_dispersion"), 0xFFFFE0);
                yield originalDamage;
            }
            case FIRE -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.BURN, 200, 0, false, true));
                spawnStatusText(target, Component.translatable("elemental.tooltip.overheating"), 0xFF5500);
                yield originalDamage;
            }
            case PHYSICAL -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.RUPTURE, 120, 0, false, true));
                spawnStatusText(target, Component.translatable("elemental.tooltip.rupture"), 0xC0C0C0);
                yield originalDamage * 2.0f;
            }
            case WIND -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.WINDSWEPT, 160, 0, false, true));
                spawnStatusText(target, Component.translatable("elemental.tooltip.wind_whirlwind"), 0x00FFFF);
                yield originalDamage;
            }
            case WATER -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.WETNESS, 240, 0, false, true));
                spawnStatusText(target, Component.translatable("elemental.tooltip.water_flood"), 0x0080FF);
                yield originalDamage * 1.5f;
            }
            case EARTH -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.STUN, 100, 0, false, true));
                spawnStatusText(target, Component.translatable("elemental.tooltip.earth_petrify"), 0x8B4513);
                yield originalDamage;
            }
            case ICE -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.FREEZE, 240, 0, false, true));
                spawnStatusText(target, Component.translatable("elemental.tooltip.ice_freeze"), 0x00BFFF);
                yield originalDamage;
            }
            case ELECTRIC -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.SHOCK, 200, 0, false, true));
                spawnStatusText(target, Component.translatable("elemental.tooltip.electric_shock"), 0xFF19FF);
                yield originalDamage;
            }
            case ENERGY -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.OVERLOAD, 200, 0, false, true));
                spawnStatusText(target, Component.translatable("elemental.tooltip.energy_overload"), 0xFFFF00);
                yield originalDamage;
            }
            case NATURAL -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.BLOOM, 160, 0, false, true));
                spawnStatusText(target, Component.translatable("elemental.tooltip.natural_bloom"), 0x32CD32);
                yield originalDamage;
            }
            case QUANTUM -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.BREAK, 120, 0, false, true));
                spawnStatusText(target, Component.translatable("elemental.tooltip.quantum_flux"), 0xFF00FF);
                yield originalDamage;
            }
            case ETHER -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.CORRUPTION, 160, 0, false, true));
                spawnStatusText(target, Component.translatable("elemental.tooltip.ether_resonance"), 0x24B3A7);
                yield originalDamage;
            }
            case SHADOW -> {
                target.addEffect(new MobEffectInstance(AbloomModEffects.ECLIPSE, 200, 0, false, true));
                spawnStatusText(target, Component.translatable("elemental.tooltip.shadow_eclipse"), 0x4B0082);
                yield originalDamage;
            }
            default -> originalDamage;
        };
    }

    public static void setBaseAccumulation(float value) {
        baseAccumulation = value;
    }

    private static float getDispersionBonus(ElementType type) {
        return switch (type) {
            case PHYSICAL -> 0.15f;
            case FIRE -> 0.15f;
            case WIND -> 0.10f;
            case WATER -> 0.20f;
            case EARTH -> 0.15f;
            case ICE -> 0.15f;
            case ELECTRIC -> 0.20f;
            case ENERGY -> 0.15f;
            case NATURAL -> 0.10f;
            case QUANTUM -> 0.10f;
            case ETHER -> 0.10f;
            case LIGHT -> 0.30f;
            case SHADOW -> 0.20f;
            default -> 0.00f;
        };
    }

    public static float getBaseAccumulation() {
        return baseAccumulation;
    }

    public static void dealElementDamage(Entity target, ElementType type, float amount) {
        dealElementDamage(target, type, amount, 1.0f, null);
    }

    public static void dealElementDamage(Entity target, ElementType type, float amount, int accumulationPoints) {
        dealElementDamage(target, type, amount, accumulationPoints >= 0 ? accumulationPoints : -accumulationPoints, null);
    }

    public static void dealElementDamage(Entity target, ElementType type, float amount, float accumMultiplier, Entity attacker) {
        if (IS_PROCESSING_DAMAGE.get()) return;
        
        // Mark as processing to prevent infinite recursion
        IS_PROCESSING_DAMAGE.set(true);
        try {
            processDealElementDamage(target, type, amount, accumMultiplier, attacker);
        } finally {
            IS_PROCESSING_DAMAGE.set(false);
        }
    }

    private static void processDealElementDamage(Entity target, ElementType type, float amount, float accumMultiplier, Entity attacker) {
        if (!(target instanceof LivingEntity livingTarget)) return;

        float damageMultiplier = 1.0f;
        float accumBonus = 1.0f;
        
        if (attacker instanceof LivingEntity le && le.hasEffect(AbloomModEffects.SHOCK)) {
            int amplifier = le.getEffect(AbloomModEffects.SHOCK).getAmplifier();
            float reduction = 1.0f - ((amplifier + 1) * 0.20f);
            damageMultiplier *= Math.max(0.1f, reduction);
        }

        if (livingTarget.hasEffect(AbloomModEffects.BLOOM)) {
            int amplifier = livingTarget.getEffect(AbloomModEffects.BLOOM).getAmplifier();
            damageMultiplier *= 1.20f * (amplifier + 1);
            accumBonus *= 1.20f * (amplifier + 1);
        }
        if (livingTarget.hasEffect(AbloomModEffects.OVERLOAD)) {
            int amplifier = livingTarget.getEffect(AbloomModEffects.OVERLOAD).getAmplifier();
            damageMultiplier *= 1.0f + (amplifier + 1) * 0.20f;
        }
        if (livingTarget.hasEffect(AbloomModEffects.WETNESS)) {
            int amplifier = livingTarget.getEffect(AbloomModEffects.WETNESS).getAmplifier();
            accumBonus *= 1.0f + (amplifier + 1) * 1.0f;
        }

        float finalDamage = amount;
        
        // Multiply finalDamage by accumulated damageMultiplier
        finalDamage *= damageMultiplier;
        
        finalDamage = ElementResistanceManager.calculateReducedDamage(livingTarget, type, finalDamage);

        int basePoints = (int) baseAccumulation;
        int pointsToAdd = Math.round((basePoints + accumMultiplier) * accumBonus);
        pointsToAdd = ElementResistanceManager.calculateAccumulationPoints(livingTarget, type, pointsToAdd);

        if (pointsToAdd > 0) {
            AbloomModAttachments.addPoints(livingTarget, type, pointsToAdd);
            boolean thresholdReached = AbloomModAttachments.getPoints(livingTarget, type) >= THRESHOLD;
            if (thresholdReached) {
                finalDamage = applyThresholdEffect(livingTarget, type, finalDamage);
                AbloomModAttachments.resetPoints(livingTarget, type);
            }
            if (canShowDamage(livingTarget)) spawnDamageNumber(livingTarget, finalDamage, type);
        } else {
            if (canShowDamage(livingTarget)) spawnDamageNumber(livingTarget, finalDamage, type);
        }

        if (target.level() instanceof ServerLevel serverLevel) {
            var damageTypeRegistry = serverLevel.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE);
            var rl = Identifier.fromNamespaceAndPath(AbloomMod.MODID, type.getDamageTypeId());
            var damageTypeHolder = damageTypeRegistry.get(rl);
            if (damageTypeHolder.isPresent()) {
                DamageSource source = new DamageSource(damageTypeHolder.get(), attacker, attacker);
                target.hurt(source, finalDamage);
            }
        }
        updateLastDamageTime(livingTarget, type);
    }

    public static void addElementPoints(LivingEntity entity, ElementType type, int points) {
        AbloomModAttachments.addPoints(entity, type, ElementResistanceManager.calculateAccumulationPoints(entity, type, points));
        updateLastDamageTime(entity, type);
    }

    public static int getElementPoints(LivingEntity entity, ElementType type) {
        return AbloomModAttachments.getPoints(entity, type);
    }

    public static void resetElementPoints(LivingEntity entity, ElementType type) {
        AbloomModAttachments.resetPoints(entity, type);
        synchronized (LAST_DAMAGE_LOCK) {
            LAST_DAMAGE_TIME.computeIfPresent(entity.getId(), (id, map) -> {
                map.remove(type);
                return map.isEmpty() ? null : map;
            });
        }
    }

    public static void resetAllElementPoints(LivingEntity entity) {
        for (ElementType type : ElementType.values()) AbloomModAttachments.resetPoints(entity, type);
        synchronized (LAST_DAMAGE_LOCK) {
            LAST_DAMAGE_TIME.remove(entity.getId());
        }
    }

    public static int getAccumulationProgress(LivingEntity entity, ElementType type) {
        return THRESHOLD > 0 ? (AbloomModAttachments.getPoints(entity, type) * 100) / THRESHOLD : 0;
    }

    public static ElementResistanceManager.Resistance getEntityResistance(Entity entity, ElementType type) {
        return ElementResistanceManager.getResistance(entity, type);
    }

    public static void markProjectileAsElemental(Entity projectile, ElementType type) {
        if (projectile != null && !projectile.level().isClientSide()) {
            AbloomModAttachments.setProjectileElement(projectile, type);
        }
    }

    public static void applyElementalDamageInstant(Entity target, Entity source, ElementType elementalType, float baseDamage, float accumPoints) {
        if (IS_PROCESSING_DAMAGE.get()) return;
        IS_PROCESSING_DAMAGE.set(true);
        try {
            processDealElementDamage(target, elementalType, baseDamage, accumPoints, source);
        } finally {
            IS_PROCESSING_DAMAGE.set(false);
        }
    }


}
