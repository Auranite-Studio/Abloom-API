package com.auranite.abloom;

import java.util.Arrays;
import java.util.Optional;

import net.minecraft.resources.ResourceLocation;

public enum ElementType {
    FIRE("fire_dmg"),
    PHYSICAL("physical_dmg"),
    WIND("wind_dmg"),
    EARTH("earth_dmg"),
    WATER("water_dmg"),
    ICE("ice_dmg"),
    ELECTRIC("electric_dmg"),
    ENERGY("energy_dmg"),
    NATURAL("natural_dmg"),
    QUANTUM("quantum_dmg");

    private final String damageTypeId;

    ElementType(String damageTypeId) {
        this.damageTypeId = damageTypeId;
    }

    public String getDamageTypeId() {
        return damageTypeId;
    }

    public String getFullDamageTypeId() {
        return "power:" + damageTypeId;
    }

    public static Optional<ElementType> fromDamageTypeId(String id) {
        if (id == null) return Optional.empty();

        String cleanId = id.contains(":") ? id.substring(id.indexOf(":") + 1) : id;

        return Arrays.stream(values())
                .filter(type -> type.getDamageTypeId().equals(cleanId))
                .findFirst();
    }

    public static ElementType fromVanillaDamageType(String damageTypeId) {
        if (damageTypeId == null || damageTypeId.isEmpty()) {
            AbloomMod.LOGGER.warn("DamageType ID is null or empty, defaulting to PHYSICAL");
            return PHYSICAL;
        }

        String id = normalizeDamageTypeId(damageTypeId);

        AbloomMod.LOGGER.debug("Mapped DamageType '{}' -> normalized '{}'", damageTypeId, id);

        return switch (id) {

            case "arrow",
                 "player_attack",
                 "entity_attack",
                 "mob_attack",
                 "mob_projectile",
                 "fall",
                 "anvil",
                 "cactus",
                 "sweet_berry_bush",
                 "fly_into_wall",
                 "dragon_breath",
                 "wither_skull",
                 "trident",
                 "sweep_attack",
                 "fireball",
                 "thrown" -> PHYSICAL;

            case "in_fire",
                 "on_fire",
                 "lava",
                 "hot_floor",
                 "campfire",
                 "unattributed_fireball",
                 "fireworks" -> FIRE;

            case "drown",
                 "wet" -> WATER;

            case "generic",
                 "explosion",
                 "explosion_player",
                 "wind_charge",
                 "generic_knockback" -> WIND;

            case "stalagmite",
                 "falling_stalactite",
                 "falling_anvil",
                 "falling_block" -> EARTH;


            case "lightning_bolt" -> ELECTRIC;

            case "freeze",
                 "frostbite" -> ICE;

            case "indirect_magic",
                 "magic",
                 "sonic_boom",
                 "thorns",
                 "guardian",
                 "evocation_fangs",
                 "wither_effect" -> ENERGY;

            case "poison",
                 "wither",
                 "starve",
                 "cramming",
                 "dry_out" -> NATURAL;

            case "out_of_world",
                 "generic_kill",
                 "void",
                 "outside_border" -> QUANTUM;


            default -> {
                AbloomMod.LOGGER.debug("Unknown DamageType '{}', defaulting to PHYSICAL", id);
                yield PHYSICAL;
            }
        };
    }

    private static String normalizeDamageTypeId(String input) {
        if (input == null) return "generic";

        String str = input.trim();
        if (str.startsWith("ResourceKey[")) {
            int colonIdx = str.indexOf(':');
            int bracketIdx = str.indexOf(']');
            if (colonIdx > 0 && bracketIdx > colonIdx) {
                str = str.substring(colonIdx + 1, bracketIdx).trim();
            }
        }
        if (str.contains(":")) {
            String[] parts = str.split(":", 2);
            str = parts.length > 1 && !parts[1].isEmpty() ? parts[1] : parts[0];
        }
        str = camelToSnake(str);
        str = str.toLowerCase(java.util.Locale.ROOT);

        str = str.replaceAll("[^a-z0-9/._-]", "_");
        try {
            ResourceLocation rl = ResourceLocation.parse("minecraft:" + str);
            return rl.getPath();
        } catch (Exception e) {
            return str;
        }
    }

    private static String camelToSnake(String input) {
        if (input == null || input.isEmpty()) return input;

        return input.replaceAll("([a-z])([A-Z]+)", "$1_$2");
    }

    @Override
    public String toString() {
        return name();
    }
}