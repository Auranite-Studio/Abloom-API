package com.auranite.abloom.config;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.Set;

public class AbloomConfig {

    public static class Client {
        // Effect Display Settings
        public final ModConfigSpec.DoubleValue RENDER_SCALE;
        public final ModConfigSpec.DoubleValue VERTICAL_OFFSET;
        public final ModConfigSpec.DoubleValue HORIZONTAL_OFFSET;
        public final ModConfigSpec.BooleanValue SHOW_SELF_POTION;
        public final ModConfigSpec.BooleanValue SHOW_OTHER_POTION;
        public final ModConfigSpec.BooleanValue BLINK_ON_LOW_DURATION;
        public final ModConfigSpec.DoubleValue MAX_DISTANCE;
        public final ModConfigSpec.DoubleValue MAX_DISTANCE_WITHOUT_LINE_OF_SIGHT;

        // Damage Display Settings
        public final ModConfigSpec.BooleanValue enableDamageNumbers;
        public final ModConfigSpec.BooleanValue enableStatusTexts;
        public final ModConfigSpec.IntValue damageNumberSpawnRadius;

        public Client(ModConfigSpec.Builder builder) {
            // Effect Display Settings
            builder.push("Effect Display Settings");
            RENDER_SCALE = builder.defineInRange("render_scale", 1.0, 0.1, 2.0);
            VERTICAL_OFFSET = builder.defineInRange("vertical_offset", 0.0, -100.0, 100.0);
            HORIZONTAL_OFFSET = builder.defineInRange("horizontal_offset", 0.0, -5.0, 5.0);
            SHOW_SELF_POTION = builder.define("show_self_potion", false);
            SHOW_OTHER_POTION = builder.define("show_other_potion", true);
            BLINK_ON_LOW_DURATION = builder.define("blink_on_low_duration", true);
            MAX_DISTANCE = builder.defineInRange("max_distance", 64.0, 8.0, 128.0);
            MAX_DISTANCE_WITHOUT_LINE_OF_SIGHT = builder.defineInRange("max_distance_without_line_of_sight", 0.0, 0.0, 64.0);
            builder.pop();

            // Damage Display Settings
            builder.push("Damage Display Settings");
            this.enableDamageNumbers = builder
                    .translation("abloom.config.enableDamageNumbers")
                    .define("enableDamageNumbers", true);
            this.enableStatusTexts = builder
                    .translation("abloom.config.enableStatusTexts")
                    .define("enableStatusTexts", true);
            this.damageNumberSpawnRadius = builder
                    .translation("abloom.config.damageNumberSpawnRadius")
                    .defineInRange("damageNumberSpawnRadius", 48, 1, 128);
            builder.pop();
        }

        public double getRenderScale() {
            return (Double) RENDER_SCALE.get();
        }

        public double getVerticalOffset() {
            return (Double) VERTICAL_OFFSET.get();
        }

        public double getHorizontalOffset() {
            return (Double) HORIZONTAL_OFFSET.get();
        }
    }

    public static class Server {
        public Server(ModConfigSpec.Builder builder) {
            // Server settings can be added here if needed
        }
    }

    public static final ModConfigSpec CLIENT_SPEC;
    public static final AbloomConfig.Client CLIENT_CONFIG;
    public static final ModConfigSpec SERVER_SPEC;
    public static final AbloomConfig.Server SERVER_CONFIG;
    public static final Set<String> DISPLAY_EFFECTS;

    static {
        DISPLAY_EFFECTS = new HashSet<>();
        DISPLAY_EFFECTS.add("burn");
        DISPLAY_EFFECTS.add("wetness");
        DISPLAY_EFFECTS.add("stun");
        DISPLAY_EFFECTS.add("freeze");
        DISPLAY_EFFECTS.add("shock");
        DISPLAY_EFFECTS.add("break");
        DISPLAY_EFFECTS.add("rupture");
        DISPLAY_EFFECTS.add("bloom");
        DISPLAY_EFFECTS.add("windswept");
        DISPLAY_EFFECTS.add("corruption");
        DISPLAY_EFFECTS.add("overload");
        DISPLAY_EFFECTS.add("dispersion");
        DISPLAY_EFFECTS.add("eclipse");
        DISPLAY_EFFECTS.add("prism");
    }

    private static volatile boolean cachedDamageNumbers = true;
    private static volatile boolean cachedStatusTexts = true;
    private static volatile int cachedDamageNumberSpawnRadius = 48;

    static {
        final Pair<Client, ModConfigSpec> clientSpec = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = clientSpec.getRight();
        CLIENT_CONFIG = clientSpec.getLeft();
        final Pair<Server, ModConfigSpec> serverSpec = new ModConfigSpec.Builder().configure(Server::new);
        SERVER_SPEC = serverSpec.getRight();
        SERVER_CONFIG = serverSpec.getLeft();
    }

    public static boolean areDamageNumbersEnabled() {
        return cachedDamageNumbers;
    }

    public static boolean areStatusTextsEnabled() {
        return cachedStatusTexts;
    }

    public static int getDamageNumberSpawnRadius() {
        return cachedDamageNumberSpawnRadius;
    }

    public static int getDamageNumberSpawnRadiusSq() {
        return cachedDamageNumberSpawnRadius * cachedDamageNumberSpawnRadius;
    }

    @SubscribeEvent
    public static void onConfigLoad(final ModConfigEvent.Loading event) {
        syncConfigValues(event.getConfig());
    }

    @SubscribeEvent
    public static void onConfigReload(final ModConfigEvent.Reloading event) {
        syncConfigValues(event.getConfig());
    }

    private static void syncConfigValues(net.neoforged.fml.config.ModConfig config) {
        // Read from CLIENT_SPEC since damage numbers are client-side visuals
        if (config.getSpec() == CLIENT_SPEC) {
            try {
                cachedDamageNumbers = CLIENT_CONFIG.enableDamageNumbers.get();
                cachedStatusTexts = CLIENT_CONFIG.enableStatusTexts.get();
                cachedDamageNumberSpawnRadius = CLIENT_CONFIG.damageNumberSpawnRadius.get();
            } catch (IllegalStateException ignored) {
            }
        }
    }
}
