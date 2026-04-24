package com.auranite.abloom.config;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class AbloomConfig {

    public static class Client {
        public Client(ModConfigSpec.Builder builder) {
            builder.push("Client Settings");
            builder.pop();
        }
    }

    public static class Server {
        public final ModConfigSpec.BooleanValue enableDamageNumbers;
        public final ModConfigSpec.BooleanValue enableStatusTexts;
        public final ModConfigSpec.IntValue damageNumberSpawnRadius;

        public Server(ModConfigSpec.Builder builder) {
            builder.push("Damage Display Settings");
            enableDamageNumbers = builder
                    .translation("abloom.config.enableDamageNumbers")
                    .define("enableDamageNumbers", true);
            enableStatusTexts = builder
                    .translation("abloom.config.enableStatusTexts")
                    .define("enableStatusTexts", true);
            damageNumberSpawnRadius = builder
                    .translation("abloom.config.damageNumberSpawnRadius")
                    .defineInRange("damageNumberSpawnRadius", 16, 1, 128);
            builder.pop();
        }
    }

    public static final ModConfigSpec CLIENT_SPEC;
    public static final AbloomConfig.Client CLIENT_CONFIG;
    public static final ModConfigSpec SERVER_SPEC;
    public static final AbloomConfig.Server SERVER_CONFIG;

    private static volatile boolean cachedDamageNumbers = true;
    private static volatile boolean cachedStatusTexts = true;
    private static volatile int cachedDamageNumberSpawnRadius = 16;

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
        if (config.getSpec() == SERVER_SPEC) {
            try {
                cachedDamageNumbers = SERVER_CONFIG.enableDamageNumbers.get();
                cachedStatusTexts = SERVER_CONFIG.enableStatusTexts.get();
                cachedDamageNumberSpawnRadius = SERVER_CONFIG.damageNumberSpawnRadius.get();
            } catch (IllegalStateException ignored) {
            }
        }
    }
}
