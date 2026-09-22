package com.auranite.abloom.handler;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.init.AbloomModAttributes;
import com.auranite.abloom.init.AbloomModEffects;
import com.auranite.abloom.util.ElementType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;

/**
 * Handles damage calculation modifiers including resistance, shred, and elemental bonuses.
 */
public class DamageModifierHandler {

    private DamageModifierHandler() {
        // Utility class
    }

    /**
     * Gets the armor resistance bonus for a specific element type.
     *
     * @param entity the entity wearing armor
     * @param type the element type
     * @return the total resistance bonus from all armor pieces, clamped to [-0.99, 0.99]
     */
    public static float getArmorResistanceBonus(LivingEntity entity, ElementType type) {
        if (entity == null || type == null) return 0.0f;

        float totalResistance = 0.0f;

        for (ItemStack armorStack : entity.getArmorSlots()) {
            if (!armorStack.isEmpty()) {
                float resistance = com.auranite.abloom.component.ElementalResistanceComponent.getResistance(armorStack, type);
                totalResistance += resistance;
            }
        }

        return Math.max(-0.99f, Math.min(totalResistance, 0.99f));
    }

    /**
     * Gets the elemental resistance modifier attribute value for an entity.
     * Each element has a dedicated attribute with range [-1, 1], representing ±100% resistance modifier.
     * Positive = more resistance (less damage), Negative = less resistance (more damage).
     *
     * @param entity the entity
     * @param type the element type
     * @return the resistance modifier value
     */
    public static double getElementResistMod(LivingEntity entity, ElementType type) {
        return switch (type) {
            case FIRE -> getAttributeOrZero(entity, AbloomModAttributes.FIRE_RESIST_MOD);
            case PHYSICAL -> getAttributeOrZero(entity, AbloomModAttributes.PHYSICAL_RESIST_MOD);
            case WIND -> getAttributeOrZero(entity, AbloomModAttributes.WIND_RESIST_MOD);
            case EARTH -> getAttributeOrZero(entity, AbloomModAttributes.EARTH_RESIST_MOD);
            case WATER -> getAttributeOrZero(entity, AbloomModAttributes.WATER_RESIST_MOD);
            case ICE -> getAttributeOrZero(entity, AbloomModAttributes.ICE_RESIST_MOD);
            case ELECTRIC -> getAttributeOrZero(entity, AbloomModAttributes.ELECTRIC_RESIST_MOD);
            case ENERGY -> getAttributeOrZero(entity, AbloomModAttributes.ENERGY_RESIST_MOD);
            case NATURAL -> getAttributeOrZero(entity, AbloomModAttributes.NATURAL_RESIST_MOD);
            case QUANTUM -> getAttributeOrZero(entity, AbloomModAttributes.QUANTUM_RESIST_MOD);
            case ETHER -> getAttributeOrZero(entity, AbloomModAttributes.ETHER_RESIST_MOD);
            case LIGHT -> getAttributeOrZero(entity, AbloomModAttributes.LIGHT_RESIST_MOD);
            case SHADOW -> getAttributeOrZero(entity, AbloomModAttributes.SHADOW_RESIST_MOD);
            case PRISMATIC -> getAttributeOrZero(entity, AbloomModAttributes.PRISMATIC_RESIST_MOD);
            default -> 0.0;
        };
    }

    /**
     * Gets the elemental resistance shred attribute value for an attacker.
     * Each element has a dedicated attribute with range [0, 1], representing 0-100% resistance shred.
     *
     * @param attacker the attacking entity
     * @param type the element type
     * @return the resistance shred value
     */
    public static double getElementResShred(LivingEntity attacker, ElementType type) {
        return switch (type) {
            case FIRE -> getAttributeOrZero(attacker, AbloomModAttributes.FIRE_RES_SHRED);
            case PHYSICAL -> getAttributeOrZero(attacker, AbloomModAttributes.PHYSICAL_RES_SHRED);
            case WIND -> getAttributeOrZero(attacker, AbloomModAttributes.WIND_RES_SHRED);
            case EARTH -> getAttributeOrZero(attacker, AbloomModAttributes.EARTH_RES_SHRED);
            case WATER -> getAttributeOrZero(attacker, AbloomModAttributes.WATER_RES_SHRED);
            case ICE -> getAttributeOrZero(attacker, AbloomModAttributes.ICE_RES_SHRED);
            case ELECTRIC -> getAttributeOrZero(attacker, AbloomModAttributes.ELECTRIC_RES_SHRED);
            case ENERGY -> getAttributeOrZero(attacker, AbloomModAttributes.ENERGY_RES_SHRED);
            case NATURAL -> getAttributeOrZero(attacker, AbloomModAttributes.NATURAL_RES_SHRED);
            case QUANTUM -> getAttributeOrZero(attacker, AbloomModAttributes.QUANTUM_RES_SHRED);
            case ETHER -> getAttributeOrZero(attacker, AbloomModAttributes.ETHER_RES_SHRED);
            case LIGHT -> getAttributeOrZero(attacker, AbloomModAttributes.LIGHT_RES_SHRED);
            case SHADOW -> getAttributeOrZero(attacker, AbloomModAttributes.SHADOW_RES_SHRED);
            case PRISMATIC -> getAttributeOrZero(attacker, AbloomModAttributes.PRISMATIC_RES_SHRED);
            default -> 0.0;
        };
    }

    /**
     * Applies elemental damage bonus attributes from the attacker.
     * Each element has a dedicated attribute with range [-1, 1], representing ±100% damage modifier.
     *
     * @param attacker the attacking entity
     * @param type the element type
     * @param baseDamage the damage before applying elemental bonus
     * @return the damage with elemental bonus applied
     */
    public static float applyElementalDamageBonus(LivingEntity attacker, ElementType type, float baseDamage) {
        double bonusValue = switch (type) {
            case FIRE -> getAttributeOrZero(attacker, AbloomModAttributes.FIRE_DMG_BONUS);
            case PHYSICAL -> getAttributeOrZero(attacker, AbloomModAttributes.PHYSICAL_DMG_BONUS);
            case WIND -> getAttributeOrZero(attacker, AbloomModAttributes.WIND_DMG_BONUS);
            case EARTH -> getAttributeOrZero(attacker, AbloomModAttributes.EARTH_DMG_BONUS);
            case WATER -> getAttributeOrZero(attacker, AbloomModAttributes.WATER_DMG_BONUS);
            case ICE -> getAttributeOrZero(attacker, AbloomModAttributes.ICE_DMG_BONUS);
            case ELECTRIC -> getAttributeOrZero(attacker, AbloomModAttributes.ELECTRIC_DMG_BONUS);
            case ENERGY -> getAttributeOrZero(attacker, AbloomModAttributes.ENERGY_DMG_BONUS);
            case NATURAL -> getAttributeOrZero(attacker, AbloomModAttributes.NATURAL_DMG_BONUS);
            case QUANTUM -> getAttributeOrZero(attacker, AbloomModAttributes.QUANTUM_DMG_BONUS);
            case ETHER -> getAttributeOrZero(attacker, AbloomModAttributes.ETHER_DMG_BONUS);
            case LIGHT -> getAttributeOrZero(attacker, AbloomModAttributes.LIGHT_DMG_BONUS);
            case SHADOW -> getAttributeOrZero(attacker, AbloomModAttributes.SHADOW_DMG_BONUS);
            case PRISMATIC -> getAttributeOrZero(attacker, AbloomModAttributes.PRISMATIC_DMG_BONUS);
            default -> 0.0;
        };

        if (bonusValue == 0.0) return baseDamage;
        return Math.max(0.0f, baseDamage * (1.0f + (float) bonusValue));
    }

    /**
     * Gets the dispersion bonus for a specific element type.
     *
     * @param type the element type
     * @return the dispersion bonus multiplier
     */
    public static float getDispersionBonus(ElementType type) {
        return switch (type) {
            case PHYSICAL -> 0.15f;
            case FIRE -> 0.15f;
            case WIND -> 0.30f;
            case WATER -> 0.30f;
            case EARTH -> 0.15f;
            case ICE -> 0.15f;
            case ELECTRIC -> 0.30f;
            case ENERGY -> 0.15f;
            case NATURAL -> 0.15f;
            case QUANTUM -> 0.15f;
            case ETHER -> 0.15f;
            case LIGHT -> 0.30f;
            case SHADOW -> 0.20f;
            case PRISMATIC -> 0.30f;
            default -> 0.00f;
        };
    }

    /**
     * Helper method to get an attribute value or zero if not present.
     */
    private static double getAttributeOrZero(LivingEntity entity, net.neoforged.neoforge.common.extensions.IForgeAttribute attribute) {
        var attr = entity.getAttribute(BuiltInRegistries.ATTRIBUTE.getHolderOrThrow(attribute.getKey()));
        return attr != null ? attr.getValue() : 0.0;
    }
}
