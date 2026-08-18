package com.auranite.abloom.util;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.config.AbloomConfig;
import com.auranite.abloom.init.AbloomModEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.Display.BillboardConstraints;
import net.minecraft.world.entity.Display.TextDisplay;
import net.minecraft.world.phys.AABB;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

public class ElementDamageDisplayManager {

    private static final int DAMAGE_NUMBER_LIFETIME = 30;
    private static final int STATUS_TEXT_LIFETIME = 50;

    // Флаги для TextDisplay: 0x02 = SEE_THROUGH (видимый сквозь блоки)
    private static final byte FLAG_SEE_THROUGH = 0x02;

    public static final String CLEANUP_TAG = "abloom:cleanup_on_load";
    public static final String SELF_DESTRUCT_TAG = "abloom:self_destruct";

    private static final String NBT_MAX_LIFETIME = "abloom:max_lifetime";
    private static final String NBT_AGE = "abloom:age";

    private static final double DAMAGE_GRAVITY = -0.02;
    private static final double DAMAGE_INITIAL_VELOCITY_Y = 0.18;
    private static final double HORIZONTAL_DRIFT = 0.02;

    private static final double STATUS_FLOAT_AMPLITUDE = 0.02;
    private static final double STATUS_FLOAT_SPEED = 0.15;
    private static final int INTERPOLATION_DURATION = 3;

    private static final int SELF_DESTRUCT_BUFFER = 20;

    private static final double BREAK_SHIMMER_SPEED = 0.3;
    private static final float BREAK_SHIMMER_INTENSITY = 0.6f;

    private static final int PHYSICS_UPDATE_INTERVAL = 1;

    private static class DisplayInfo {
        final TextDisplay display;
        final int targetEntityId;
        final boolean isStatus;

        DisplayInfo(TextDisplay display, int targetEntityId, boolean isStatus) {
            this.display = display;
            this.targetEntityId = targetEntityId;
            this.isStatus = isStatus;
        }
    }

    private static final Map<UUID, DisplayInfo> ACTIVE_DAMAGE_DISPLAYS = new ConcurrentHashMap<>();
    private static final Map<UUID, DisplayInfo> ACTIVE_STATUS_DISPLAYS = new ConcurrentHashMap<>();
    private static final Map<ElementType, Integer> DAMAGE_COLORS = new EnumMap<>(ElementType.class);
    private static final Map<UUID, double[]> ACTIVE_PHYSICS = new ConcurrentHashMap<>();

    private static final CopyOnWriteArrayList<TextDisplay> PENDING_REMOVALS = new CopyOnWriteArrayList<>();

    public static void registerDamageColor(ElementType type, int color) {
        DAMAGE_COLORS.put(type, color);
    }

    public static void setDamageColor(ElementType type, int color) {
        DAMAGE_COLORS.put(type, color);
    }

    public static Map<ElementType, Integer> getAllDamageColors() {
        return new EnumMap<>(DAMAGE_COLORS);
    }

    public static int getDamageColor(ElementType type) {
        if (type == null) return 0xFFFFFF;
        // Для призматического урона возвращаем специальное значение -1 для радужного цвета
        if (type == ElementType.PRISMATIC) return -1;
        return DAMAGE_COLORS.getOrDefault(type, 0xFFFFFF);
    }

    public void cleanupStaleDisplays() {
        int cleanedCount = 0;

        // Очистка damage displays
        Iterator<Map.Entry<UUID, DisplayInfo>> damageIterator = ACTIVE_DAMAGE_DISPLAYS.entrySet().iterator();
        while (damageIterator.hasNext()) {
            Map.Entry<UUID, DisplayInfo> entry = damageIterator.next();
            UUID displayUuid = entry.getKey();
            DisplayInfo info = entry.getValue();

            if (info == null || info.display == null || info.display.isRemoved() || info.display.level() == null) {
                damageIterator.remove();
                ACTIVE_PHYSICS.remove(displayUuid);
                cleanedCount++;
                continue;
            }

            Entity target = ((ServerLevel) info.display.level()).getEntity(info.targetEntityId);
            if (target == null || !target.isAlive()) {
                if (!info.display.isRemoved()) safeRemoveDisplaySilent(info.display);
                damageIterator.remove();
                ACTIVE_PHYSICS.remove(displayUuid);
                cleanedCount++;
            }
        }

        // Очистка status displays
        Iterator<Map.Entry<UUID, DisplayInfo>> statusIterator = ACTIVE_STATUS_DISPLAYS.entrySet().iterator();
        while (statusIterator.hasNext()) {
            Map.Entry<UUID, DisplayInfo> entry = statusIterator.next();
            UUID displayUuid = entry.getKey();
            DisplayInfo info = entry.getValue();

            if (info == null || info.display == null || info.display.isRemoved() || info.display.level() == null) {
                statusIterator.remove();
                ACTIVE_PHYSICS.remove(displayUuid);
                cleanedCount++;
                continue;
            }

            Entity target = ((ServerLevel) info.display.level()).getEntity(info.targetEntityId);
            if (target == null || !target.isAlive()) {
                if (!info.display.isRemoved()) safeRemoveDisplaySilent(info.display);
                statusIterator.remove();
                ACTIVE_PHYSICS.remove(displayUuid);
                cleanedCount++;
            }
        }

        if (cleanedCount > 0) {
            AbloomMod.LOGGER.debug("ElementDamageDisplayManager: cleaned {} stale displays", cleanedCount);
        }
    }

    public void cleanupAllDisplays() {
        AbloomMod.LOGGER.info("Force cleaning ALL element damage displays...");

        for (DisplayInfo info : ACTIVE_DAMAGE_DISPLAYS.values()) {
            if (info != null && info.display != null && !info.display.isRemoved()) {
                safeRemoveDisplaySilent(info.display);
            }
        }
        ACTIVE_DAMAGE_DISPLAYS.clear();

        for (DisplayInfo info : ACTIVE_STATUS_DISPLAYS.values()) {
            if (info != null && info.display != null && !info.display.isRemoved()) {
                safeRemoveDisplaySilent(info.display);
            }
        }
        ACTIVE_STATUS_DISPLAYS.clear();
        ACTIVE_PHYSICS.clear();
        PENDING_PHYSICS_TASKS.clear();

        AbloomMod.LOGGER.info("All element damage displays cleared successfully.");
    }

    public void clearActiveDisplays(LivingEntity entity) {
        if (entity == null) return;
        int entityId = entity.getId();

        // Clear damage displays
        Iterator<Map.Entry<UUID, DisplayInfo>> damageIterator = ACTIVE_DAMAGE_DISPLAYS.entrySet().iterator();
        while (damageIterator.hasNext()) {
            Map.Entry<UUID, DisplayInfo> entry = damageIterator.next();
            DisplayInfo info = entry.getValue();
            if (info != null && info.targetEntityId == entityId) {
                if (info.display != null && !info.display.isRemoved()) {
                    safeRemoveDisplaySilent(info.display);
                }
                cleanupDisplayResources(entry.getKey());
                damageIterator.remove();
            }
        }

        // Clear status displays
        Iterator<Map.Entry<UUID, DisplayInfo>> statusIterator = ACTIVE_STATUS_DISPLAYS.entrySet().iterator();
        while (statusIterator.hasNext()) {
            Map.Entry<UUID, DisplayInfo> entry = statusIterator.next();
            DisplayInfo info = entry.getValue();
            if (info != null && info.targetEntityId == entityId) {
                if (info.display != null && !info.display.isRemoved()) {
                    safeRemoveDisplaySilent(info.display);
                }
                cleanupDisplayResources(entry.getKey());
                statusIterator.remove();
            }
        }
    }

    /**
     * Clean up all resources for a display UUID
     */
    private void cleanupDisplayResources(UUID uuid) {
        ACTIVE_DAMAGE_DISPLAYS.remove(uuid);
        ACTIVE_STATUS_DISPLAYS.remove(uuid);
        ACTIVE_PHYSICS.remove(uuid);
        PENDING_PHYSICS_TASKS.remove(uuid);
    }

    public int cleanupDisplaysInChunk(ServerLevel level, int chunkX, int chunkZ) {
        int count = 0;

        Iterator<Map.Entry<UUID, DisplayInfo>> damageIterator = ACTIVE_DAMAGE_DISPLAYS.entrySet().iterator();
        while (damageIterator.hasNext()) {
            Map.Entry<UUID, DisplayInfo> entry = damageIterator.next();
            DisplayInfo info = entry.getValue();

            if (info != null && info.display != null && !info.display.isRemoved()) {
                int dChunkX = (int) Math.floor(info.display.getX() / 16.0);
                int dChunkZ = (int) Math.floor(info.display.getZ() / 16.0);

                if (dChunkX == chunkX && dChunkZ == chunkZ) {
                    PENDING_REMOVALS.add(info.display);
                    damageIterator.remove();
                    ACTIVE_PHYSICS.remove(entry.getKey());
                    count++;
                }
            } else {
                damageIterator.remove();
                ACTIVE_PHYSICS.remove(entry.getKey());
            }
        }

        Iterator<Map.Entry<UUID, DisplayInfo>> statusIterator = ACTIVE_STATUS_DISPLAYS.entrySet().iterator();
        while (statusIterator.hasNext()) {
            Map.Entry<UUID, DisplayInfo> entry = statusIterator.next();
            DisplayInfo info = entry.getValue();

            if (info != null && info.display != null && !info.display.isRemoved()) {
                int dChunkX = (int) Math.floor(info.display.getX() / 16.0);
                int dChunkZ = (int) Math.floor(info.display.getZ() / 16.0);

                if (dChunkX == chunkX && dChunkZ == chunkZ) {
                    PENDING_REMOVALS.add(info.display);
                    statusIterator.remove();
                    ACTIVE_PHYSICS.remove(entry.getKey());
                    count++;
                }
            } else {
                statusIterator.remove();
                ACTIVE_PHYSICS.remove(entry.getKey());
            }
        }

        return count;
    }

    public void processPendingRemovals() {
        if (PENDING_REMOVALS.isEmpty()) return;

        for (TextDisplay display : PENDING_REMOVALS) {
            if (display != null && !display.isRemoved() && display.level() != null) {
                try {
                    safeRemoveDisplaySilent(display);
                } catch (Exception e) {
                    AbloomMod.LOGGER.warn("Failed to discard pending display: {}", e.getMessage());
                }
            }
        }
        PENDING_REMOVALS.clear();
    }

    public static void cleanupOrphanedDisplaysOnWorldLoad(ServerLevel level) {
        if (level == null) return;

        long startTime = System.currentTimeMillis();
        int removedCount = 0;

        Predicate<Entity> hasCleanupTag = e -> e.entityTags().contains(CLEANUP_TAG) && !e.isRemoved();

        for (TextDisplay display : level.getEntities(EntityTypes.TEXT_DISPLAY, hasCleanupTag)) {
            if (display != null && !display.isRemoved()) {
                display.discard();
                removedCount++;
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        if (removedCount > 0) {
            AbloomMod.LOGGER.info("Cleaned {} orphaned TextDisplay entities on world load in {}ms", removedCount, duration);
        }
    }

    public static void tickSelfDestructDisplays(ServerLevel level) {
        if (level == null) return;

        Predicate<Entity> hasSelfDestruct = e -> {
            CompoundTag tag = e.getPersistentData();
            return tag.getBooleanOr(SELF_DESTRUCT_TAG,false) && !e.isRemoved();
        };

        for (TextDisplay display : level.getEntities(EntityTypes.TEXT_DISPLAY, hasSelfDestruct)) {
            if (display == null || display.isRemoved()) continue;

            CompoundTag tag = display.getPersistentData();
            int age = tag.getIntOr(NBT_AGE, 0) + 1;
            int maxLife = tag.getIntOr(NBT_MAX_LIFETIME, 0);

            if (age >= maxLife) {
                display.discard();
            } else {
                tag.putInt(NBT_AGE, age);
            }
        }
    }

    public void spawnDamageNumber(LivingEntity entity, float amount, ElementType type) {
        spawnDamageNumber(entity, amount, type, false);
    }

    public void spawnDamageNumber(LivingEntity entity, float amount, ElementType type, boolean isCrit) {
        if (!AbloomConfig.areDamageNumbersEnabled()) return;
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        double spawnRadiusSq = 16.0 * 16.0;
        boolean playerInRange = serverLevel.players().stream()
                .anyMatch(player -> player.distanceToSqr(entity) <= spawnRadiusSq);
        if (!playerInRange) return;

        int entityId = entity.getId();
        int color = getDamageColor(type);
        boolean hasBreak = entity.hasEffect(AbloomModEffects.BREAK);

        double offsetX = (serverLevel.getRandom().nextFloat() - 0.5f) * 0.5;
        double offsetZ = (serverLevel.getRandom().nextFloat() - 0.5f) * 0.5;

        // Format damage text, add "!!" for critical hits
        String text = String.format("%.1f", amount);
        if (isCrit) {
            text += "!!";
        }

        TextDisplay display = createTextDisplay(
                serverLevel,
                entity.getX() + offsetX,
                entity.getY() + entity.getBbHeight() + 0.5,
                entity.getZ() + offsetZ,
                text,
                color,
                DAMAGE_NUMBER_LIFETIME + SELF_DESTRUCT_BUFFER
        );

        if (display != null) {
            serverLevel.addFreshEntity(display);
            UUID displayUuid = display.getUUID();

            ACTIVE_DAMAGE_DISPLAYS.put(displayUuid, new DisplayInfo(display, entityId, false));

            double randomX = (serverLevel.getRandom().nextFloat() - 0.5f) * HORIZONTAL_DRIFT;
            double randomZ = (serverLevel.getRandom().nextFloat() - 0.5f) * HORIZONTAL_DRIFT;

            ACTIVE_PHYSICS.put(displayUuid, new double[]{
                    randomX,
                    DAMAGE_INITIAL_VELOCITY_Y,
                    randomZ,
                    0,
                    DAMAGE_NUMBER_LIFETIME,
                    color,
                    0,
                    hasBreak ? 1.0 : 0.0
            });

            schedulePhysicsUpdate(serverLevel, displayUuid);

            AbloomMod.queueServerWork(DAMAGE_NUMBER_LIFETIME + 10, () -> {
                if (ACTIVE_PHYSICS.containsKey(displayUuid)) {
                    TextDisplay d = (TextDisplay) serverLevel.getEntity(displayUuid);
                    if (d != null && !d.isRemoved()) safeRemoveDisplaySilent(d);
                    cleanupDisplayResources(displayUuid);
                }
            });
        }
    }

    public void spawnStatusText(LivingEntity entity, Component textComponent, int color) {
        if (!AbloomConfig.areStatusTextsEnabled()) return;
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        double spawnRadiusSq = 16.0 * 16.0;
        boolean playerInRange = serverLevel.players().stream()
                .anyMatch(player -> player.distanceToSqr(entity) <= spawnRadiusSq);
        if (!playerInRange) return;

        int entityId = entity.getId();

        double offsetX = (serverLevel.getRandom().nextFloat() - 0.5f) * 0.3;
        double offsetZ = (serverLevel.getRandom().nextFloat() - 0.5f) * 0.3;

        TextDisplay display = createTextDisplay(
                serverLevel,
                entity.getX() + offsetX,
                entity.getY() + entity.getBbHeight() + 1.2,
                entity.getZ() + offsetZ,
                textComponent,
                color,
                STATUS_TEXT_LIFETIME + SELF_DESTRUCT_BUFFER
        );

        if (display != null) {
            serverLevel.addFreshEntity(display);
            UUID displayUuid = display.getUUID();

            ACTIVE_STATUS_DISPLAYS.put(displayUuid, new DisplayInfo(display, entityId, true));

            double randomPhase = serverLevel.getRandom().nextDouble() * Math.PI * 2;

            ACTIVE_PHYSICS.put(displayUuid, new double[]{
                    0,
                    0,
                    0,
                    0,
                    STATUS_TEXT_LIFETIME,
                    color,
                    randomPhase,
                    0.0
            });

            schedulePhysicsUpdate(serverLevel, displayUuid);

            AbloomMod.queueServerWork(STATUS_TEXT_LIFETIME + 10, () -> {
                if (ACTIVE_PHYSICS.containsKey(displayUuid)) {
                    TextDisplay d = (TextDisplay) serverLevel.getEntity(displayUuid);
                    if (d != null && !d.isRemoved()) safeRemoveDisplaySilent(d);
                    cleanupDisplayResources(displayUuid);
                }
            });
        }
    }

    public void spawnStatusText(LivingEntity entity, String text, int color) {
        if (!AbloomConfig.areStatusTextsEnabled()) return;
        spawnStatusText(entity, Component.literal(text), color);
    }

    private static final Map<UUID, ScheduledPhysicsTask> PENDING_PHYSICS_TASKS = new ConcurrentHashMap<>();

    private static class ScheduledPhysicsTask {
        final UUID displayUuid;
        final ServerLevel level;

        ScheduledPhysicsTask(UUID displayUuid, ServerLevel level) {
            this.displayUuid = displayUuid;
            this.level = level;
        }
    }

    private void schedulePhysicsUpdate(ServerLevel level, UUID displayUuid) {
        // Проверяем, есть ли уже запланированная задача для этого дисплея
        if (PENDING_PHYSICS_TASKS.containsKey(displayUuid)) {
            return; // Задача уже запланирована, не создаем дубликат
        }

        Runnable physicsTask = () -> {
            // Удаляем из pending перед выполнением
            PENDING_PHYSICS_TASKS.remove(displayUuid);

            TextDisplay display = (TextDisplay) level.getEntity(displayUuid);
            double[] physics = ACTIVE_PHYSICS.get(displayUuid);
            DisplayInfo info = ACTIVE_DAMAGE_DISPLAYS.get(displayUuid);
            if (info == null) info = ACTIVE_STATUS_DISPLAYS.get(displayUuid);

            if (display == null || display.isRemoved() || physics == null) {
                cleanupDisplayResources(displayUuid);
                return;
            }

            physics[3]++;
            int ticksAlive = (int) physics[3];
            int maxTicks = (int) physics[4];
            int originalColor = (int) physics[5];
            double floatPhase = physics[6];
            boolean isBreak = physics[7] == 1.0;
            boolean isPrismatic = originalColor == -1; // Призматический урон (радужный цвет)

            if (info != null && info.isStatus) {
                floatPhase += STATUS_FLOAT_SPEED;
                physics[6] = floatPhase;

                double floatOffset = Math.sin(floatPhase) * STATUS_FLOAT_AMPLITUDE;
                display.setPos(display.getX(), display.getY() + floatOffset, display.getZ());

                double pulse = (Math.sin(floatPhase * 2) + 1) / 2;
                int r, g, b;

                // Если призматический урон, используем радужный цвет
                if (isPrismatic) {
                    int rainbowHue = (int) ((ticksAlive * 10 + floatPhase * 10) % 360);
                    int[] rgb = hsbToRgb(rainbowHue, 1.0f, 1.0f);
                    r = rgb[0];
                    g = rgb[1];
                    b = rgb[2];
                } else {
                    r = (originalColor >> 16) & 0xFF;
                    g = (originalColor >> 8) & 0xFF;
                    b = originalColor & 0xFF;
                }

                int shimmerR = (int) (r + (255 - r) * pulse * 0.5);
                int shimmerG = (int) (g + (255 - g) * pulse * 0.5);
                int shimmerB = (int) (b + (255 - b) * pulse * 0.5);
                int shimmerColor = (shimmerR << 16) | (shimmerG << 8) | shimmerB;

                Component currentText = display.getText();
                if (currentText != null) {
                    display.setText(currentText.copy().withStyle(Style.EMPTY.withColor(shimmerColor).withBold(true)));
                }

            } else {
                physics[1] += DAMAGE_GRAVITY;
                display.setPos(display.getX() + physics[0], display.getY() + physics[1], display.getZ() + physics[2]);

                int finalColor;

                if (isBreak) {
                    double pulse = (Math.sin(ticksAlive * BREAK_SHIMMER_SPEED) + 1.0) / 2.0;

                    int r, g, b;

                    // Если призматический урон, используем радужный цвет
                    if (isPrismatic) {
                        int rainbowHue = (int) ((ticksAlive * 20) % 360);
                        int[] rgb = hsbToRgb(rainbowHue, 1.0f, 1.0f);
                        r = rgb[0];
                        g = rgb[1];
                        b = rgb[2];
                    } else {
                        r = (originalColor >> 16) & 0xFF;
                        g = (originalColor >> 8) & 0xFF;
                        b = originalColor & 0xFF;
                    }

                    int shimmerR = (int) (r + (255 - r) * pulse * BREAK_SHIMMER_INTENSITY);
                    int shimmerG = (int) (g + (255 - g) * pulse * BREAK_SHIMMER_INTENSITY);
                    int shimmerB = (int) (b + (255 - b) * pulse * BREAK_SHIMMER_INTENSITY);

                    finalColor = (shimmerR << 16) | (shimmerG << 8) | shimmerB;

                } else {
                    // Если призматический урон, используем радужный цвет
                    if (isPrismatic) {
                        int rainbowHue = (int) ((ticksAlive * 10) % 360);
                        int[] rgb = hsbToRgb(rainbowHue, 1.0f, 1.0f);
                        finalColor = (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
                    } else {
                        finalColor = originalColor;
                    }
                }

                int fadeStartTick = (int) (maxTicks * 0.7);
                if (ticksAlive >= fadeStartTick) {
                    int fadeTicks = maxTicks - fadeStartTick;
                    int currentFadeTick = ticksAlive - fadeStartTick;
                    float alpha = 1.0f - (currentFadeTick / (float) fadeTicks);
                    alpha = Math.max(0.0f, Math.min(1.0f, alpha));

                    int r = (finalColor >> 16) & 0xFF;
                    int g = (finalColor >> 8) & 0xFF;
                    int b = finalColor & 0xFF;

                    int a = (int) (alpha * 255);
                    finalColor = (a << 24) | (r << 16) | (g << 8) | b;
                }

                Component currentText = display.getText();
                if (currentText != null) {
                    display.setText(currentText.copy().withStyle(Style.EMPTY.withColor(finalColor).withBold(true)));
                }
            }

            if (ticksAlive >= maxTicks) {
                safeRemoveDisplay(level, displayUuid, display, info);
                return;
            }

            // Планируем следующий апдейт только если дисплей еще существует
            if (!display.isRemoved()) {
                schedulePhysicsUpdate(level, displayUuid);
            }
        };

        // Добавляем в pending и планируем выполнение
        PENDING_PHYSICS_TASKS.put(displayUuid, new ScheduledPhysicsTask(displayUuid, level));
        AbloomMod.queueServerWork(1, physicsTask);
    }

    /**
     * Converts HSB color to RGB
     */
    private static int[] hsbToRgb(int hue, float saturation, float brightness) {
        int h = hue % 360;
        float s = saturation;
        float b = brightness;

        float c = b * s;
        float x = c * (1 - Math.abs((h / 60f) % 2 - 1));
        float m = b - c;

        float r, g, bVal;

        if (h < 60) {
            r = c;
            g = x;
            bVal = 0;
        } else if (h < 120) {
            r = x;
            g = c;
            bVal = 0;
        } else if (h < 180) {
            r = 0;
            g = c;
            bVal = x;
        } else if (h < 240) {
            r = 0;
            g = x;
            bVal = c;
        } else if (h < 300) {
            r = x;
            g = 0;
            bVal = c;
        } else {
            r = c;
            g = 0;
            bVal = x;
        }

        int red = (int) ((r + m) * 255);
        int green = (int) ((g + m) * 255);
        int blue = (int) ((bVal + m) * 255);

        return new int[]{red, green, blue};
    }

    private void safeRemoveDisplay(ServerLevel level, UUID displayUuid, TextDisplay display, DisplayInfo info) {
        display.removeTag(CLEANUP_TAG);

        if (!display.isRemoved() && level != null) {
            // В 1.21.9 используем broadcastAndSend для отправки пакета удаления
            level.getChunkSource().sendToTrackingPlayersAndSelf(
                    display,
                    new ClientboundRemoveEntitiesPacket(display.getId())
            );
            display.discard();
        }

        cleanupDisplayResources(displayUuid);
    }

    private void safeRemoveDisplaySilent(TextDisplay display) {
        if (display == null || display.isRemoved() || display.level() == null) return;

        display.removeTag(CLEANUP_TAG);

        ServerLevel level = (ServerLevel) display.level();
        // Используем broadcastAndSend для совместимости с 1.21.9
        level.getChunkSource().sendToTrackingPlayersAndSelf(
                display,
                new ClientboundRemoveEntitiesPacket(display.getId())
        );
        display.discard();
    }

    private static void markForCleanup(Entity entity, int maxLifetime) {
        if (entity == null) return;

        entity.addTag(CLEANUP_TAG);

        CompoundTag tag = entity.getPersistentData();
        tag.putBoolean(SELF_DESTRUCT_TAG, true);
        tag.putInt(NBT_MAX_LIFETIME, maxLifetime);
        tag.putInt(NBT_AGE, 0);

        for (Entity passenger : entity.getPassengers()) {
            markForCleanup(passenger, maxLifetime);
        }
    }

    private static TextDisplay createTextDisplay(ServerLevel level, double x, double y, double z, Component textComponent, int color, int maxLifetime) {
        TextDisplay display = EntityTypes.TEXT_DISPLAY.create(level, EntitySpawnReason.EVENT);
        if (display == null) {
            AbloomMod.LOGGER.error("Failed to create TextDisplay entity at ({}, {}, {})", x, y, z);
            return null;
        }

        display.setPos(x, y, z);
        display.setText(textComponent.copy().withStyle(Style.EMPTY.withColor(color).withBold(true)));

        // В 1.21.9: setBackgroundColor принимает цвет как int (ARGB)
        display.setBackgroundColor(0x00000000);

        // Установка флагов через data accessor
        // FLAG_SEE_THROUGH = 0x02 (бит 1)
        display.setFlags(FLAG_SEE_THROUGH);

        display.setLineWidth(200);
        display.setBillboardConstraints(BillboardConstraints.CENTER);
        display.setNoGravity(true);
        display.setInvulnerable(true);
        display.setSilent(true);
        display.setViewRange(16.0f);

        // Интерполяция для плавного движения
        display.setPosRotInterpolationDuration(INTERPOLATION_DURATION);
        display.setTransformationInterpolationDuration(INTERPOLATION_DURATION);
        display.setTransformationInterpolationDelay(0);

        markForCleanup(display, maxLifetime);

        return display;
    }

    private static TextDisplay createTextDisplay(ServerLevel level, double x, double y, double z, String text, int color, int maxLifetime) {
        return createTextDisplay(level, x, y, z, Component.literal(text), color, maxLifetime);
    }
}