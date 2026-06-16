package com.auranite.abloom;

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
import net.minecraft.world.item.ItemStack;
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

@EventBusSubscriber(modid = AbloomMod.MODID)
public class ElementDamageHandler {

    private static float baseAccumulation = 1.0f;
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

    public static void setDisplayManager(ElementDamageDisplayManager manager) {
        displayManager = manager;
    }

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

    public static boolean canSpawnDisplay() {
        synchronized (DISPLAY_COUNT_LOCK) {
            return currentDisplayCount < MAX_ACTIVE_DISPLAYS;
        }
    }

    public static void incrementDisplayCount() {
        synchronized (DISPLAY_COUNT_LOCK) {
            currentDisplayCount++;
        }
    }

    public static void decrementDisplayCount() {
        synchronized (DISPLAY_COUNT_LOCK) {
            currentDisplayCount = Math.max(0, currentDisplayCount - 1);
        }
    }

    public static int getCurrentDisplayCount() {
        synchronized (DISPLAY_COUNT_LOCK) {
            return currentDisplayCount;
        }
    }

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

    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        if (IS_PROCESSING_DAMAGE.get()) return;
        IS_PROCESSING_DAMAGE.set(true);
        try {
            processLivingHurt(event);
        } finally {
            IS_PROCESSING_DAMAGE.set(false);
        }
    }

    private static void processLivingHurt(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity e ? e : null;
        boolean erosionActive = target.hasEffect(AbloomModEffects.WINDSWEPT);

        DamageSource source = event.getSource();
        ElementType type = getElementTypeFromSource(source);
        if (type == null) {
            if (canShowDamage(target)) spawnDamageNumber(target, event.getNewDamage(), null);
            return;
        }

        float damageMultiplier = 1.0f;

        damageMultiplier = calculateOutgoingDamageMultiplier(type, damageMultiplier);

        // Модификаторы от атакующего
        if (attacker != null && attacker.hasEffect(AbloomModEffects.SHOCK)) {
            int amplifier = attacker.getEffect(AbloomModEffects.SHOCK).getAmplifier();
            float reduction = 1.0f - ((amplifier + 1) * 0.20f);
            reduction = Math.max(0.1f, reduction);
            damageMultiplier *= reduction;
        }

        // Модификаторы от цели
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

        float damage = event.getNewDamage() * damageMultiplier;

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
            effectiveAccumMultiplier *= 1.0f + (amplifier + 1) * 0.5f;
        }

        float armorResistanceBonus = getArmorResistanceBonus(target, type);

        int basePoints = (int) baseAccumulation;
        int pointsToAdd = Math.round(basePoints * effectiveAccumMultiplier);
        if (AbloomMod.LOGGER.isDebugEnabled()) {
            AbloomMod.LOGGER.debug("Base accumulation points: {} (base: {}, multiplier: {})", pointsToAdd, basePoints, effectiveAccumMultiplier);
        }
        pointsToAdd = Math.round(ElementDamageHandler.calculateResonanceBuildupMultiplier(type, pointsToAdd));
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
        
        finalDamage = calculateIncomingDamageMultiplier(type, finalDamage);

        finalDamage = applyArmorResistance(finalDamage, armorResistanceBonus);
        if (thresholdReached) {
            if (AbloomMod.LOGGER.isDebugEnabled()) {
                AbloomMod.LOGGER.debug("Accumulation threshold reached for {} (type: {}). Applying effect.", target.getName().getString(), type);
            }
            finalDamage = applyThresholdEffect(target, type, finalDamage);
            AbloomModAttachments.resetPoints(target, type);
        }
        event.setNewDamage(finalDamage);
        if (canShowDamage(target)) spawnDamageNumber(target, finalDamage, type);
        updateLastDamageTime(target, type);
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
        if (displayManager != null) displayManager.clearActiveDisplays(player);
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

    private static void spawnDamageNumber(LivingEntity entity, float amount, ElementType type) {
        if (!canSpawnDisplay()) return;
        if (displayManager != null) {
            incrementDisplayCount();
            displayManager.spawnDamageNumber(entity, amount, type);
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

        // Применяем модификаторы сопротивления от других модов
        // Модификаторы могут уменьшить сопротивление ниже 0 (для увеличения урона)
        totalResistance = calculateResistance(type, totalResistance);
        
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
                yield originalDamage;
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
        
        // Применяем модификаторы исходящего урона от других модов
        damageMultiplier = calculateOutgoingDamageMultiplier(type, damageMultiplier);
        
        if (attacker instanceof LivingEntity le && le.hasEffect(AbloomModEffects.SHOCK)) {
            int amplifier = le.getEffect(AbloomModEffects.SHOCK).getAmplifier();
            float reduction = 1.0f - ((amplifier + 1) * 0.20f);
            damageMultiplier *= Math.max(0.1f, reduction);
        }

        float accumBonus = 1.0f;
        if (livingTarget.hasEffect(AbloomModEffects.BLOOM)) {
            damageMultiplier *= 1.20f;
            accumBonus *= 1.20f;
        }
        if (livingTarget.hasEffect(AbloomModEffects.OVERLOAD)) {
            int amplifier = livingTarget.getEffect(AbloomModEffects.OVERLOAD).getAmplifier();
            damageMultiplier *= 1.0f + (amplifier + 1) * 0.20f;
        }
        if (livingTarget.hasEffect(AbloomModEffects.WETNESS)) {
            int amplifier = livingTarget.getEffect(AbloomModEffects.WETNESS).getAmplifier();
            accumBonus *= 1.0f + (amplifier + 1) * 0.5f;
        }

        float finalDamage = amount;
        
        // Применяем модификаторы исходящего урона от других модов (к значению damageMultiplier он уже применен, но finalDamage еще не умножен)
        // Умножаем finalDamage на накопленный damageMultiplier
        finalDamage *= damageMultiplier;
        
        finalDamage = ElementResistanceManager.calculateReducedDamage(livingTarget, type, finalDamage);
        
        // Применяем модификаторы входящего урона от других модов
        finalDamage = calculateIncomingDamageMultiplier(type, finalDamage);

        int basePoints = (int) baseAccumulation;
        int pointsToAdd = ElementResistanceManager.calculateAccumulationPoints(livingTarget, type, basePoints);
        pointsToAdd = Math.round(pointsToAdd * accumMultiplier * accumBonus);
        
        // Применяем модификаторы накопления резонанса от других модов
        pointsToAdd = Math.round(calculateResonanceBuildupMultiplier(type, pointsToAdd));

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
        // Применяем модификаторы накопления резонанса от других модов
        points = Math.round(ElementDamageHandler.calculateResonanceBuildupMultiplier(type, points));
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

    // ============================================
    // ПУБЛИЧНЫЙ API ДЛЯ ДРУГИХ МОДОВ
    // ============================================

    /**
     * Базовый класс для модификаторов урона
     */
    public static class DamageModifier {
        private final String name;
        private final float value;
        private final ModifierType type;

        public DamageModifier(String name, float value, ModifierType type) {
            this.name = name;
            this.value = value;
            this.type = type;
        }

        public String getName() { return name; }
        public float getValue() { return value; }
        public ModifierType getType() { return type; }
    }

    public enum ModifierType {
        OUTGOING_DAMAGE,        // Бонус к исходящему урону (положительное = +%, отрицательное = -%)
        INCOMING_DAMAGE,        // Бонус к входящему урону (положительное = +% к получаемому урону, отрицательное = -%)
        RESISTANCE,             // Модификатор сопротивления (положительное = +%, отрицательное = -%)
        DEFENCE,                // Модификатор защиты (положительное = +защита, отрицательное = игнорирование)
        RESONANCE_BUILDUP       // Модификатор накопления резонанса (положительное = +% к накоплению, отрицательное = -%)
    }

    private static final Map<String, DamageModifier> DAMAGE_MODIFIERS = new ConcurrentHashMap<>();

    /**
     * Регистрация модификатора урона
     * @param id Уникальный ID модификатора (например, "mymod:fire_bonus")
     * @param modifier Модификатор
     */
    public static void registerDamageModifier(String id, DamageModifier modifier) {
        DAMAGE_MODIFIERS.put(id, modifier);
    }

    /**
     * Удаление модификатора урона
     * @param id ID модификатора
     */
    public static void removeDamageModifier(String id) {
        DAMAGE_MODIFIERS.remove(id);
    }

    /**
     * Получение всех модификаторов по типу
     * @param type Тип модификатора
     * @return Список модификаторов
     */
    public static List<DamageModifier> getModifiersByType(ModifierType type) {
        return DAMAGE_MODIFIERS.values().stream()
            .filter(m -> m.getType() == type)
            .toList();
    }

    /**
     * Расчет множителя исходящего урона
     * @param type Тип урона
     * @param originalMultiplier Базовый множитель
     * @return Итоговый множитель
     */
    public static float calculateOutgoingDamageMultiplier(ElementType type, float originalMultiplier) {
        float multiplier = originalMultiplier;
        for (DamageModifier modifier : getModifiersByType(ModifierType.OUTGOING_DAMAGE)) {
            // Знаковое значение: +0.20 = +20%, -0.20 = -20%
            multiplier += modifier.getValue();
        }
        // Множитель урона не может быть отрицательным или нулевым
        return Math.max(0.001f, multiplier);
    }

    /**
     * Расчет множителя входящего урона
     * @param type Тип урона
     * @param originalMultiplier Базовый множитель
     * @return Итоговый множитель
     */
    public static float calculateIncomingDamageMultiplier(ElementType type, float originalMultiplier) {
        float multiplier = originalMultiplier;
        for (DamageModifier modifier : getModifiersByType(ModifierType.INCOMING_DAMAGE)) {
            // Знаковое значение: +0.20 = +% к получаемому урону, -0.20 = -% к получаемому урону
            multiplier += modifier.getValue();
        }
        // Множитель урона не может быть отрицательным или нулевым
        return Math.max(0.001f, multiplier);
    }

    /**
     * Модификатор сопротивления (в процентах от 0.0 до 1.0)
     * @param type Тип урона
     * @param originalResistance Базовое сопротивление
     * @return Итоговое сопротивление
     */
    public static float calculateResistance(ElementType type, float originalResistance) {
        float adjustment = 0.0f;
        for (DamageModifier modifier : getModifiersByType(ModifierType.RESISTANCE)) {
            // Знаковое значение: +0.20 = +20% к сопротивлению, -0.20 = -20% к сопротивлению
            adjustment += modifier.getValue();
        }
        // Модификаторы могут уменьшить сопротивление ниже 0 (для увеличения урона)
        // Но броня не может дать более чем 99% сопротивления
        return Math.max(-0.99f, originalResistance + adjustment);
    }

    /**
     * Снижение сопротивления (для эффектов проникновения)
     * @param type Тип урона
     * @param originalResistance Базовое сопротивление
     * @return Сниженное сопротивление
     */
    public static float reduceResistance(ElementType type, float originalResistance) {
        return calculateResistance(type, originalResistance);
    }

    /**
     * Бонус к сопротивлению (для эффектов защиты)
     * @param type Тип урона
     * @param originalResistance Базовое сопротивление
     * @return Увеличенное сопротивление
     */
    public static float bonusResistance(ElementType type, float originalResistance) {
        return calculateResistance(type, originalResistance);
    }

    /**
     * Расчет итогового значения защиты
     * @param originalArmorValue Исходное значение брони
     * @param target Целевое существо
     * @return Итоговое значение брони после применения модификаторов
     */
    public static int calculateArmorValue(int originalArmorValue, LivingEntity target) {
        int adjustment = 0;
        
        // Применяем модификаторы защиты
        for (DamageModifier modifier : getModifiersByType(ModifierType.DEFENCE)) {
            // Знаковое значение: +10 = +10 к броне, -10 = игнорирование 10 единиц брони
            adjustment += (int) modifier.getValue();
        }
        
        // Защита не может быть отрицательной
        return Math.max(0, originalArmorValue + adjustment);
    }

    /**
     * Получение модификатора по ID
     * @param id ID модификатора
     * @return Модификатор или null
     */
    public static DamageModifier getDamageModifier(String id) {
        return DAMAGE_MODIFIERS.get(id);
    }

    /**
     * Получение всех зарегистрированных модификаторов
     * @return Карта всех модификаторов
     */
    public static Map<String, DamageModifier> getAllDamageModifiers() {
        return new ConcurrentHashMap<>(DAMAGE_MODIFIERS);
    }

    /**
     * Расчет множителя накопления резонанса
     * @param type Тип урона
     * @param originalMultiplier Базовый множитель
     * @return Итоговый множитель
     */
    public static float calculateResonanceBuildupMultiplier(ElementType type, float originalMultiplier) {
        float adjustment = 0.0f;
        for (DamageModifier modifier : getModifiersByType(ModifierType.RESONANCE_BUILDUP)) {
            // Знаковое значение: +0.50 = +50% к накоплению, -0.50 = -50% к накоплению
            adjustment += modifier.getValue();
        }
        // Множитель накопления не может быть отрицательным или нулевым
        return Math.max(0.001f, originalMultiplier + adjustment);
    }
}
