package com.auranite.abloom.handler;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.component.ElementalWeaponComponent;
import com.auranite.abloom.datapack.ElementalWeaponRegistry;
import com.auranite.abloom.init.AbloomModAttachments;
import com.auranite.abloom.registries.ElementalProjectileRegistry;
import com.auranite.abloom.registries.ElementalWeaponRegistry;
import com.auranite.abloom.util.ElementType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Determines the elemental type for damage sources from various origins.
 * Handles enchantment overrides, weapon components, projectiles, and stage progression.
 */
public class ElementTypeResolver {
    
    /**
     * Gets the effective elemental type for a weapon stack, considering enchantment overrides.
     */
    public static ElementType getEffectiveElementType(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        
        // Check enchantment override first
        ElementType enchantmentOverride = ElementDamageHandler.getOverrideFromStack(stack);
        if (enchantmentOverride != null) {
            return enchantmentOverride;
        }
        
        // Fall back to original element from weapon
        return ElementalWeaponUtils.getElementType(stack);
    }
    
    /**
     * Checks the attacker's weapons for enchantments that override the elemental damage type.
     */
    @Nullable
    public static ElementType getOverrideElementTypeFromEnchantments(LivingEntity attacker) {
        if (attacker == null) return null;
        
        ItemStack mainHand = attacker.getMainHandItem();
        ItemStack offHand = attacker.getOffhandItem();
        
        ElementType override = ElementDamageHandler.getOverrideFromStack(mainHand);
        if (override != null) return override;
        
        return ElementDamageHandler.getOverrideFromStack(offHand);
    }
    
    /**
     * Gets element type from damage source, checking all possible origins.
     */
    @Nullable
    public static ElementType getElementTypeFromSource(net.minecraft.world.damagesource.DamageSource source) {
        Entity directEntity = source.getDirectEntity();
        Entity causingEntity = source.getEntity();
        
        // Check projectile first
        if (directEntity != null && ElementalProjectileRegistry.isElementalProjectile(directEntity)) {
            return ElementalProjectileRegistry.getElementType(directEntity);
        }
        
        // Check causing entity (attacker)
        if (causingEntity instanceof LivingEntity livingEntity) {
            // Check weapon in main hand
            ItemStack weapon = livingEntity.getMainHandItem();
            if (!weapon.isEmpty()) {
                ElementType type = getElementTypeFromWeapon(livingEntity, weapon);
                if (type != null) return type;
            }
        }
        
        return null;
    }
    
    /**
     * Gets element type from weapon, considering multi-stage weapons.
     */
    @Nullable
    private static ElementType getElementTypeFromWeapon(LivingEntity attacker, ItemStack weapon) {
        ResourceLocation weaponId = BuiltInRegistries.ITEM.getKey(weapon.getItem());
        
        // Check for enchantment override first (highest priority)
        ElementType enchantmentOverride = ElementDamageHandler.getOverrideFromStack(weapon);
        if (enchantmentOverride != null) {
            return enchantmentOverride;
        }
        
        // Check multi-stage weapon
        if (ElementalWeaponRegistry.hasStages(weaponId)) {
            // Stage progression handled separately by StageProgressionManager
            // Return null to indicate stage system should determine element
            return null;
        }
        
        // Check elemental weapon component
        if (ElementalWeaponComponent.hasElement(weapon)) {
            return ElementalWeaponComponent.getElementType(weapon);
        }
        
        // Check registered weapon data
        var weaponData = ElementalWeaponRegistry.getWeaponData(weapon);
        if (weaponData != null) {
            return weaponData.elementType();
        }
        
        return null;
    }
    
    /**
     * Gets element type from an item stack using registry or component data.
     */
    @Nullable
    public static ElementType getElementTypeFromItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        var weaponData = ElementalWeaponRegistry.getWeaponData(stack);
        if (weaponData != null) {
            return weaponData.elementType();
        }
        
        if (ElementalWeaponComponent.hasElement(stack)) {
            return ElementalWeaponComponent.getElementType(stack);
        }
        
        return null;
    }
    
    /**
     * Checks if damage is projectile-driven.
     */
    public static boolean isProjectileDriven(net.minecraft.world.damagesource.DamageSource source) {
        Entity directEntity = source.getDirectEntity();
        return directEntity != null && ElementalProjectileRegistry.isElementalProjectile(directEntity);
    }
    
    /**
     * Checks if element was overridden by enchantments.
     */
    public static boolean isElementOverriddenByEnchantment(LivingEntity attacker, ElementType currentType) {
        if (attacker == null || currentType == null) return false;
        
        ElementType enchantmentType = getOverrideElementTypeFromEnchantments(attacker);
        return enchantmentType != null && enchantmentType == currentType;
    }
}
