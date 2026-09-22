package com.auranite.abloom.handler;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.util.ElementType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles enchantment-based elemental type overrides.
 * Enchantments in the mapping will override the weapon's natural element with the mapped element.
 */
public class EnchantmentOverrideHandler {

    /** Enchantment to ElementType mapping for elemental override */
    public static final Map<ResourceLocation, ElementType> ENCHANTMENT_ELEMENT_MAP = new ConcurrentHashMap<>();

    private EnchantmentOverrideHandler() {
        // Utility class
    }

    /**
     * Initializes the mapping between enchantments and element types.
     * Enchantments in this map will override the weapon's natural element with the mapped element.
     */
    public static void initEnchantmentElementMapping() {
        // Fire Aspect → FIRE (vanilla enchantment)
        ENCHANTMENT_ELEMENT_MAP.put(ResourceLocation.withDefaultNamespace("fire_aspect"), ElementType.FIRE);
        ENCHANTMENT_ELEMENT_MAP.put(ResourceLocation.withDefaultNamespace("flame"), ElementType.FIRE);
        // Add more elemental enchantments here as they are created:
        // ENCHANTMENT_ELEMENT_MAP.put(ResourceLocation.fromNamespaceAndPath("abloom", "ice_aspect"), ElementType.ICE);
        // ENCHANTMENT_ELEMENT_MAP.put(ResourceLocation.fromNamespaceAndPath("abloom", "electric_aspect"), ElementType.ELECTRIC);
    }

    /**
     * Checks the attacker's weapons (main hand and offhand) for enchantments that override
     * the elemental damage type. If an enchantment in the mapping is found, returns the
     * corresponding ElementType.
     * <p>
     * This override has the HIGHEST priority - it is checked before any other element
     * determination logic (datapack, components, stages, etc.).
     *
     * @param attacker the attacking entity
     * @return the overridden ElementType if an enchantment override is found, null otherwise
     */
    public static ElementType getOverrideElementTypeFromEnchantments(LivingEntity attacker) {
        if (attacker == null) return null;

        ItemStack mainHand = attacker.getMainHandItem();
        ItemStack offHand = attacker.getOffhandItem();

        // Check main hand weapon
        ElementType override = getOverrideFromStack(mainHand);
        if (override != null) return override;

        // Check offhand weapon
        return getOverrideFromStack(offHand);
    }

    /**
     * Helper method to check a single ItemStack for elemental enchantment overrides.
     *
     * @param stack the ItemStack to check
     * @return the overridden ElementType if an enchantment override is found, null otherwise
     */
    public static ElementType getOverrideFromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;

        // getEnchantments() returns Object2IntMap<Holder<Enchantment>>
        for (var entry : stack.getEnchantments().entrySet()) {
            Holder<net.minecraft.world.item.enchantment.Enchantment> holder = entry.getKey();
            int level = entry.getIntValue();
            ResourceLocation enchantId = holder.unwrapKey()
                    .map(key -> key.location())
                    .orElse(null);
            if (enchantId != null) {
                ElementType elementType = ENCHANTMENT_ELEMENT_MAP.get(enchantId);
                if (elementType != null) {
                    AbloomMod.LOGGER.debug("Enchantment override: {} (level {}) on {} -> {}",
                            enchantId, level, stack.getHoverName(), elementType);
                    return elementType;
                }
            }
        }

        return null;
    }

    /**
     * Gets the effective elemental type for a weapon stack, considering enchantment overrides.
     * This method is safe to call from both client and server side.
     *
     * @param stack the weapon ItemStack
     * @return the effective ElementType (enchantment override if present, otherwise original element)
     */
    public static ElementType getEffectiveElementType(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;

        // Check enchantment override first (works on both client and server)
        ElementType enchantmentOverride = getOverrideFromStack(stack);
        if (enchantmentOverride != null) {
            return enchantmentOverride;
        }

        // Fall back to original element from weapon
        return com.auranite.abloom.util.ElementalWeaponUtils.getElementType(stack);
    }
}
