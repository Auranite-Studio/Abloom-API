package com.auranite.abloom.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class AbloomConfig {

    public static class Client {
        public final ModConfigSpec.BooleanValue enableDamageNumbers;
        public final ModConfigSpec.BooleanValue enableStatusTexts;

        public Client(ModConfigSpec.Builder builder) {
            builder.push("Damage Display Settings");

            enableDamageNumbers = builder
                    .comment("Enable floating damage numbers above entities")
                    .translation("abloom.config.enableDamageNumbers")
                    .define("enableDamageNumbers", true);

            enableStatusTexts = builder
                    .comment("Enable status text displays (buffs, debuffs, etc.)")
                    .translation("abloom.config.enableStatusTexts")
                    .define("enableStatusTexts", true);

            builder.pop();
        }
    }

    public static class Server {
        public Server(ModConfigSpec.Builder builder) {
            builder.push("Server Settings");
            builder.pop();
        }
    }

    public static final ModConfigSpec CLIENT_SPEC;
    public static final AbloomConfig.Client CLIENT_CONFIG;

    public static final ModConfigSpec SERVER_SPEC;
    public static final AbloomConfig.Server SERVER_CONFIG;

    static {
        final Pair<Client, ModConfigSpec> clientSpec = new ModConfigSpec.Builder()
                .configure(Client::new);
        CLIENT_SPEC = clientSpec.getRight();
        CLIENT_CONFIG = clientSpec.getLeft();

        final Pair<Server, ModConfigSpec> serverSpec = new ModConfigSpec.Builder()
                .configure(Server::new);
        SERVER_SPEC = serverSpec.getRight();
        SERVER_CONFIG = serverSpec.getLeft();
    }

    public static boolean areDamageNumbersEnabled() {
        try {
            return CLIENT_CONFIG.enableDamageNumbers.get();
        } catch (IllegalStateException e) {
            return true;
        }
    }

    public static boolean areStatusTextsEnabled() {
        try {
            return CLIENT_CONFIG.enableStatusTexts.get();
        } catch (IllegalStateException e) {
            return true;
        }
    }
}