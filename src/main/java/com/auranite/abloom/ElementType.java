package com.auranite.abloom;

import com.auranite.abloom.data.ElementalDamageTypeData;
import com.auranite.abloom.data.ElementalDamageTypeReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents an elemental damage type. Can be one of the 11 built-in types
 * or a custom type loaded from datapacks.
 */
public class ElementType {
    
    // Built-in elemental types
    public static final ElementType FIRE = new ElementType("fire_dmg", 0xFF5500, "elemental.tooltip.fire", "elemental.resistance.fire", null, 0, null);
    public static final ElementType PHYSICAL = new ElementType("physical_dmg", 0xC0C0C0, "elemental.tooltip.physical", "elemental.resistance.physical", null, 0, null);
    public static final ElementType WIND = new ElementType("wind_dmg", 0x00FFFF, "elemental.tooltip.wind", "elemental.resistance.wind", null, 0, null);
    public static final ElementType EARTH = new ElementType("earth_dmg", 0x8B4513, "elemental.tooltip.earth", "elemental.resistance.earth", null, 0, null);
    public static final ElementType WATER = new ElementType("water_dmg", 0x0080FF, "elemental.tooltip.water", "elemental.resistance.water", null, 0, null);
    public static final ElementType ICE = new ElementType("ice_dmg", 0x00BFFF, "elemental.tooltip.ice", "elemental.resistance.ice", null, 0, null);
    public static final ElementType ELECTRIC = new ElementType("electric_dmg", 0xFF19FF, "elemental.tooltip.electric", "elemental.resistance.electric", null, 0, null);
    public static final ElementType ENERGY = new ElementType("energy_dmg", 0xFFFF00, "elemental.tooltip.energy", "elemental.resistance.energy", null, 0, null);
    public static final ElementType NATURAL = new ElementType("natural_dmg", 0x32CD32, "elemental.tooltip.natural", "elemental.resistance.natural", null, 0, null);
    public static final ElementType QUANTUM = new ElementType("quantum_dmg", 0x9400D3, "elemental.tooltip.quantum", "elemental.resistance.quantum", null, 0, null);
    public static final ElementType ETHER = new ElementType("ether_dmg", 0x24B3A7, "elemental.tooltip.ether", "elemental.resistance.ether", null, 0, null);
    
    private static final Map<String, ElementType> BUILTIN_TYPES = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, ElementType> CUSTOM_TYPES = new ConcurrentHashMap<>();
    
    static {
        BUILTIN_TYPES.put("fire_dmg", FIRE);
        BUILTIN_TYPES.put("physical_dmg", PHYSICAL);
        BUILTIN_TYPES.put("wind_dmg", WIND);
        BUILTIN_TYPES.put("earth_dmg", EARTH);
        BUILTIN_TYPES.put("water_dmg", WATER);
        BUILTIN_TYPES.put("ice_dmg", ICE);
        BUILTIN_TYPES.put("electric_dmg", ELECTRIC);
        BUILTIN_TYPES.put("energy_dmg", ENERGY);
        BUILTIN_TYPES.put("natural_dmg", NATURAL);
        BUILTIN_TYPES.put("quantum_dmg", QUANTUM);
        BUILTIN_TYPES.put("ether_dmg", ETHER);
    }
    
    private final String damageTypeId;
    private final int color;
    private final String elementItemTooltip;
    private final String elementArmorResistanceTooltip;
    private final ResourceLocation resonanceEffectId;
    private final int resonanceEffectDuration;
    private final String resonanceEffectStatusTextDisplay;
    private final boolean isCustom;
    
    private ElementType(String damageTypeId, int color, String elementItemTooltip, 
                       String elementArmorResistanceTooltip, ResourceLocation resonanceEffectId,
                       int resonanceEffectDuration, String resonanceEffectStatusTextDisplay) {
        this(damageTypeId, color, elementItemTooltip, elementArmorResistanceTooltip, 
             resonanceEffectId, resonanceEffectDuration, resonanceEffectStatusTextDisplay, false);
    }
    
    private ElementType(String damageTypeId, int color, String elementItemTooltip,
                       String elementArmorResistanceTooltip, ResourceLocation resonanceEffectId,
                       int resonanceEffectDuration, String resonanceEffectStatusTextDisplay, boolean isCustom) {
        this.damageTypeId = damageTypeId;
        this.color = color;
        this.elementItemTooltip = elementItemTooltip;
        this.elementArmorResistanceTooltip = elementArmorResistanceTooltip;
        this.resonanceEffectId = resonanceEffectId;
        this.resonanceEffectDuration = resonanceEffectDuration;
        this.resonanceEffectStatusTextDisplay = resonanceEffectStatusTextDisplay != null ? resonanceEffectStatusTextDisplay : elementItemTooltip;
        this.isCustom = isCustom;
    }
    
    /**
     * Creates a custom ElementType from datapack data.
     */
    public static ElementType fromDatapackData(ElementalDamageTypeData data) {
        String cleanId = data.id().getPath();
        ElementType type = new ElementType(
                cleanId,
                data.color(),
                data.elementItemTooltip(),
                data.elementArmorResistanceTooltip(),
                data.resonanceEffect().orElse(null),
                data.resonanceEffectDuration(),
                data.resonanceEffectStatusTextDisplay().orElse(data.elementItemTooltip()),
                true
        );
        CUSTOM_TYPES.put(data.id(), type);
        return type;
    }
    
    /**
     * Reloads custom types from datapack data. Called when datapacks are reloaded.
     */
    public static void reloadCustomTypes() {
        CUSTOM_TYPES.clear();
        Map<ResourceLocation, ElementalDamageTypeData> customData = ElementalDamageTypeReloadListener.getAllCustomDamageTypes();
        for (Map.Entry<ResourceLocation, ElementalDamageTypeData> entry : customData.entrySet()) {
            fromDatapackData(entry.getValue());
        }
        AbloomMod.LOGGER.info("Reloaded {} custom elemental damage types", CUSTOM_TYPES.size());
    }
    
    public String getDamageTypeId() {
        return damageTypeId;
    }
    
    public String getFullDamageTypeId() {
        return "abloom:" + damageTypeId;
    }
    
    public int getColor() {
        return color;
    }
    
    public String getElementItemTooltip() {
        return elementItemTooltip;
    }
    
    public String getElementArmorResistanceTooltip() {
        return elementArmorResistanceTooltip;
    }
    
    public ResourceLocation getResonanceEffectId() {
        return resonanceEffectId;
    }
    
    public MobEffect getResonanceEffect() {
        if (resonanceEffectId == null) return null;
        return net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.get(resonanceEffectId).orElse(null);
    }
    
    public int getResonanceEffectDuration() {
        return resonanceEffectDuration;
    }
    
    public String getResonanceEffectStatusTextDisplay() {
        return resonanceEffectStatusTextDisplay;
    }
    
    public boolean isCustom() {
        return isCustom;
    }
    
    /**
     * Gets an ElementType by its damage type ID (e.g., "fire_dmg" or "abloom:sharp_dmg").
     */
    public static Optional<ElementType> fromDamageTypeId(String id) {
        if (id == null) return Optional.empty();
        
        String cleanId = id.contains(":") ? id.substring(id.indexOf(":") + 1) : id;
        
        // Check built-in types first
        ElementType builtin = BUILTIN_TYPES.get(cleanId);
        if (builtin != null) {
            return Optional.of(builtin);
        }
        
        // Check custom types
        for (Map.Entry<ResourceLocation, ElementType> entry : CUSTOM_TYPES.entrySet()) {
            if (entry.getKey().getPath().equals(cleanId) || entry.getValue().getDamageTypeId().equals(cleanId)) {
                return Optional.of(entry.getValue());
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Maps vanilla damage types to elemental types.
     */
    public static ElementType fromVanillaDamageType(String damageTypeId) {
        if (damageTypeId == null || damageTypeId.isEmpty()) {
            AbloomMod.LOGGER.warn("DamageType ID is null or empty, defaulting to PHYSICAL");
            return PHYSICAL;
        }
        
        String id = normalizeDamageTypeId(damageTypeId);
        AbloomMod.LOGGER.debug("Mapped DamageType '{}' -> normalized '{}'", damageTypeId, id);
        
        return switch (id) {
            case "arrow", "player_attack", "entity_attack", "mob_attack", "mob_projectile",
                 "fall", "anvil", "cactus", "sweet_berry_bush", "fly_into_wall",
                 "dragon_breath", "wither_skull", "trident", "sweep_attack",
                 "fireball", "thrown" -> PHYSICAL;
            
            case "in_fire", "on_fire", "lava", "hot_floor", "campfire",
                 "unattributed_fireball", "fireworks" -> FIRE;
            
            case "drown", "wet" -> WATER;
            
            case "generic", "explosion", "explosion_player", "wind_charge", "generic_knockback" -> WIND;
            
            case "stalagmite", "falling_stalactite", "falling_anvil", "falling_block" -> EARTH;
            
            case "lightning_bolt" -> ELECTRIC;
            
            case "freeze", "frostbite" -> ICE;
            
            case "indirect_magic", "magic", "sonic_boom", "thorns", "guardian",
                 "evocation_fangs", "wither_effect" -> ENERGY;
            
            case "poison", "wither", "starve", "cramming", "dry_out" -> NATURAL;
            
            case "out_of_world", "generic_kill", "void", "outside_border" -> QUANTUM;
            
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
        return isCustom ? "CUSTOM[" + damageTypeId + "]" : damageTypeId;
    }
    
    public String getDisplayName() {
        return damageTypeId.replace("_dmg", "").replace('_', ' ').toUpperCase();
    }
}
