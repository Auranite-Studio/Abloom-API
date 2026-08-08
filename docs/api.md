\# Abloom API Developer Guide



\## Overview



This guide explains how to use the Abloom API for mod development. The API provides programmatic control over elemental weapons, projectiles, armor resistances, and damage calculation.



\## Core Concepts



\### Element Types



The Abloom API supports 13 element types defined in `ElementType`:



| Element      | Effect                                                                                                                                                                                                        | Duration                |

|--------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------|

| \*\*PHYSICAL\*\* | Physical resonance explosion, applies Rupture effect (target takes 200% initial damage, defense reduced by 30%)                                                                                               | 6 seconds (120 ticks)   |

| \*\*FIRE\*\*     | Fire resonance explosion, applies Burning effect                                                                                                                                                              | 10 seconds (200 ticks)  |

| \*\*WIND\*\*     | Wind resonance explosion, applies Windswept effect. Applying any other elemental damage (except Wind) while this effect is active triggers the corresponding resonance explosion and removes Windswept effect | 8 seconds (160 ticks)   |

| \*\*WATER\*\*    | Water resonance explosion, applies Wetness effect (increases resonance accumulation by 50%)                                                                                                                   | 12 seconds (240 ticks)  |

| \*\*EARTH\*\*    | Earth resonance explosion, applies Stun effect (target cannot deal damage or move)                                                                                                                            | 5 seconds (100 ticks)   |

| \*\*ICE\*\*      | Ice resonance explosion, applies Freeze effect                                                                                                                                                                | 12 seconds (240 ticks)  |

| \*\*ELECTRIC\*\* | Electric resonance explosion, applies Shock effect (target deals 20% less damage)                                                                                                                             | 10 seconds (200 ticks)  |

| \*\*ENERGY\*\*   | Energy resonance explosion, applies Overload effect (damage taken by target increased by 20%)                                                                                                                 | 10 seconds (200 ticks)  |

| \*\*NATURAL\*\*  | Natural resonance explosion, applies Bloom effect (target takes 1 damage per second and receives 20% universal vulnerability)                                                                                 | 8 seconds (160 ticks)   |

| \*\*QUANTUM\*\*  | Quantum resonance explosion, applies Break effect (all damage to target ignores defense)                                                                                                                      | 6 seconds (120 ticks)   |

| \*\*ETHER\*\*    | Ether resonance explosion, applies Corruption effect (target's resistance to all damage types reduced by 20% and takes periodic damage)                                                                       | 8 seconds (160 ticks)   |

| \*\*UNKNOWN\*\*  | Unknown resonance explosion, applies Taunt effect (the target is attacked by hostile and neutral mobs while this effect is active.)                                                                           | ??? seconds (??? ticks) |

| \*\*LIGHT\*\*    | Light resonance explosion, applies Dispersion effect (damage taken by target increased by a certain amount %)                                                                                                 | 10 seconds (200 ticks)  |

| \*\*SHADOW\*\*   | Shadow resonance explosion, applies Eclipse effect (target's defense and damage dealt reduced by 10% + 10% per additional negative effect, max 50% reduction)                                                 | 10 seconds (200 ticks)   |



\### Resonance Threshold



\- Base accumulation: \*\*1 point per hit\*\*

\- Activation threshold: \*\*100 points\*\*

\- Effect duration: Varies by element (see documentation)



\---



\## Elemental Weapons API



\### ElementalWeaponRegistry



Register weapons to make them elemental:



```java

import com.auranite.abloom.ElementalWeaponRegistry;

import com.auranite.abloom.ElementType;



// Register a weapon with default multiplier (1.0)

ElementalWeaponRegistry.registerWeapon(myItem, ElementType.FIRE);



// Register a weapon with custom multiplier

ElementalWeaponRegistry.registerWeapon(myItem, ElementType.FIRE, 2.5f);

```



\### ElementalWeaponUtils



Convenient utilities for elemental weapons:



```java

import com.auranite.abloom.ElementalWeaponUtils;

import com.auranite.abloom.ElementType;

import net.minecraft.world.item.ItemStack;



// Register an item by registry name

ElementalWeaponUtils.registerItemById("mymod", "dragon\_sword", ElementType.FIRE);



// Register multiple items at once

ElementalWeaponUtils.registerMultiple(ElementType.WATER, item1, item2, item3);



// Register multiple items with custom multiplier

ElementalWeaponUtils.registerMultiple(ElementType.WATER, 3.0f, item1, item2, item3);



// Check if item is elemental

boolean isElemental = ElementalWeaponUtils.isElemental(stack);



// Get element type from stack

ElementType element = ElementalWeaponUtils.getElementType(stack);



// Get accumulation multiplier

float multiplier = ElementalWeaponUtils.getAccumulationMultiplier(stack);



// Add element to existing item stack

ItemStack elementalStack = ElementalWeaponUtils.addElementToStack(stack, ElementType.FIRE);



// Add element with custom multiplier

ItemStack elementalStack = ElementalWeaponUtils.addElementToStackWithAccum(stack, ElementType.FIRE, 2.0f);



// Remove element from stack

ItemStack cleanStack = ElementalWeaponUtils.removeElementFromStack(stack);

```



\### ElementalWeaponComponent



Low-level component API for direct NBT manipulation:



```java

import com.auranite.abloom.ElementalWeaponComponent;

import net.minecraft.world.item.ItemStack;

import java.util.Optional;



// Check if item has elemental component

boolean hasElement = ElementalWeaponComponent.hasElement(stack);



// Get element from item

Optional<ElementType> element = ElementalWeaponComponent.getElement(stack);



// Get accumulation multiplier

float multiplier = ElementalWeaponComponent.getAccumMultiplier(stack);



// Create item with element

ItemStack elementalStack = ElementalWeaponComponent.withElement(stack, ElementType.FIRE);



// Create item with element and multiplier

ItemStack elementalStack = ElementalWeaponComponent.withElementAndAccum(stack, ElementType.FIRE, 2.5f);



// Remove elemental component

ItemStack cleanStack = ElementalWeaponComponent.removeElement(stack);

```



\---



\## Elemental Projectiles API



\### ElementalProjectileRegistry



Register projectiles to make them elemental:



```java

import com.auranite.abloom.ElementalProjectileRegistry;

import com.auranite.abloom.ElementType;

import net.minecraft.world.entity.EntityType;

import net.minecraft.world.entity.LivingEntity;



// Register projectile by entity type

ElementalProjectileRegistry.registerProjectile(EntityType.ARROW, ElementType.FIRE, 1.5f);



// Register projectile by class

ElementalProjectileRegistry.registerProjectileByClass(MyCustomProjectile.class, ElementType.ICE, 2.0f);



// Check if projectile is elemental

boolean isElemental = ElementalProjectileRegistry.isElementalProjectile(projectile);



// Get element from projectile

Optional<ElementType> element = ElementalProjectileRegistry.getElementForEntity(projectile);



// Get accumulation multiplier from projectile

Optional<Float> multiplier = ElementalProjectileRegistry.getAccumulationMultiplierForEntity(projectile);

```



\### Creating Elemental Projectiles



```java

import com.auranite.abloom.ElementalProjectileRegistry;

import com.auranite.abloom.ElementType;

import net.minecraft.server.level.ServerLevel;

import net.minecraft.world.entity.LivingEntity;



// Create and launch elemental projectile

Arrow projectile = ElementalProjectileRegistry.createAndLaunchElementalProjectile(

&#x20;   level,

&#x20;   shooter,

&#x20;   EntityType.ARROW,

&#x20;   1.5f,  // velocity

&#x20;   0.1f   // inaccuracy

);



// Create projectile with forced element (overrides shooter's weapon)

Arrow projectile = ElementalProjectileRegistry.createElementalProjectileWithOverride(

&#x20;   level,

&#x20;   shooter,

&#x20;   EntityType.ARROW,

&#x20;   ElementType.FIRE,  // forced element

&#x20;   1.5f,              // velocity

&#x20;   0.1f               // inaccuracy

);

```



\### Inherit Element from Shooter



```java

import com.auranite.abloom.ElementalProjectileRegistry;



// Enable inheriting element from shooter's weapon (default: true)

ElementalProjectileRegistry.setInheritElementFromShooter(true);



// Check if inheriting is enabled

boolean inherits = ElementalProjectileRegistry.getInheritElementFromShooter();

```



\---



\## Armor Resistance API



\### ArmorResistanceRegistry



Register armor with elemental resistances:



```java

import com.auranite.abloom.ArmorResistanceRegistry;

import com.auranite.abloom.ElementType;

import java.util.Map;



// Register armor with single resistance

ArmorResistanceRegistry.registerArmor(myArmor, ElementType.FIRE, 0.3f);



// Register armor with multiple resistances

Map<ElementType, Float> resistances = Map.of(

&#x20;   ElementType.FIRE, 0.3f,

&#x20;   ElementType.WATER, 0.2f,

&#x20;   ElementType.ICE, 0.4f

);

ArmorResistanceRegistry.registerArmor(myArmor, resistances);

```



\### ElementalResistanceComponent



Low-level component API for direct NBT manipulation:



```java

import com.auranite.abloom.ElementalResistanceComponent;

import net.minecraft.world.item.ItemStack;



// Check if item has resistance component

boolean hasResistance = ElementalResistanceComponent.hasResistance(stack);



// Get resistance for specific element

float fireRes = ElementalResistanceComponent.getResistance(stack, ElementType.FIRE);



// Get all resistances

Map<ElementType, Float> allResistances = ElementalResistanceComponent.getAllResistances(stack);



// Create item with resistance

ItemStack resistantStack = ElementalResistanceComponent.withResistance(stack, ElementType.FIRE, 0.3f);



// Create item with multiple resistances

Map<ElementType, Float> resistances = Map.of(

&#x20;   ElementType.FIRE, 0.3f,

&#x20;   ElementType.ICE, 0.4f

);

ItemStack resistantStack = ElementalResistanceComponent.withResistances(stack, resistances);



// Remove resistance component

ItemStack cleanStack = ElementalResistanceComponent.removeResistance(stack);



// Remove specific resistance

ItemStack cleanStack = ElementalResistanceComponent.removeResistance(stack, ElementType.FIRE);

```



\---



\## Element Damage API



\### ElementDamageHandler



Main API for dealing elemental damage:



```java

import com.auranite.abloom.ElementDamageHandler;

import com.auranite.abloom.ElementType;

import net.minecraft.world.entity.Entity;



// Deal elemental damage to entity

ElementDamageHandler.dealElementDamage(target, ElementType.FIRE, 10.0f);



// Deal elemental damage with custom accumulation

ElementDamageHandler.dealElementDamage(target, ElementType.FIRE, 10.0f, 2.0f, attacker);



// Deal elemental damage with specific accumulation points

ElementDamageHandler.dealElementDamage(target, ElementType.FIRE, 10.0f, 50);



// Instant elemental damage (bypasses some modifiers)

ElementDamageHandler.applyElementalDamageInstant(target, source, ElementType.FIRE, 10.0f, 1.0f);

```



\### Adding Points Directly



```java

import com.auranite.abloom.ElementDamageHandler;

import com.auranite.abloom.ElementType;

import net.minecraft.world.entity.LivingEntity;



// Add accumulation points

ElementDamageHandler.addElementPoints(entity, ElementType.FIRE, 25);



// Get current accumulation points

int points = ElementDamageHandler.getElementPoints(entity, ElementType.FIRE);



// Reset specific element points

ElementDamageHandler.resetElementPoints(entity, ElementType.FIRE);



// Reset all element points

ElementDamageHandler.resetAllElementPoints(entity);



// Get accumulation progress (0-100%)

int progress = ElementDamageHandler.getAccumulationProgress(entity, ElementType.FIRE);

```



\---



\## Element Resistance API



\### ElementResistanceManager



Check and modify entity resistances:



```java

import com.auranite.abloom.ElementResistanceManager;

import com.auranite.abloom.ElementType;

import net.minecraft.world.entity.Entity;



// Get entity resistance

ElementResistanceManager.Resistance resistance = ElementResistanceManager.getResistance(entity, ElementType.FIRE);



// Check if entity has resistance

boolean hasResistance = ElementResistanceManager.hasResistanceFor(entity, ElementType.FIRE);



// Check if entity is weak to element

boolean isWeak = ElementResistanceManager.isWeakness(entity, ElementType.FIRE);



// Check if entity is immune

boolean isImmune = ElementResistanceManager.isImmune(entity, ElementType.FIRE);



// Calculate reduced damage (takes resistance into account)

float reducedDamage = ElementResistanceManager.calculateReducedDamage(entity, ElementType.FIRE, 10.0f);



// Calculate accumulation points (takes resistance into account)

int points = ElementResistanceManager.calculateAccumulationPoints(entity, ElementType.FIRE, 10);

```



\### ElementResistanceRegistry



Register resistances programmatically:



```java

import com.auranite.abloom.ElementResistanceRegistry;

import com.auranite.abloom.ElementType;

import com.auranite.abloom.ElementResistanceManager;

import net.minecraft.world.entity.EntityType;



// Register uniform resistance for multiple entities

ElementResistanceRegistry.registerUniform(ElementType.FIRE, 0.5f, EntityType.BLAZE, EntityType.MAGMA\_CUBE);



// Register single entity resistance

ElementResistanceRegistry.registerSingle(EntityType.WITHER, ElementType.FIRE, 0.8f);



// Register multiple resistances for single entity

Map<ElementType, ElementResistanceManager.Resistance> resistances = Map.of(

&#x20;   ElementType.FIRE, ElementResistanceManager.Resistance.HALF\_RESIST,

&#x20;   ElementType.WATER, ElementResistanceManager.Resistance.ZERO,

&#x20;   ElementType.ELECTRIC, ElementResistanceManager.Resistance.WEAKNESS

);

ElementResistanceRegistry.registerMultiple(EntityType.WITHER, resistances);



// Register resistance via tags (requires registry access)

// See ElementResistanceRegistry Tags section below

```



\### Tags for Entity Resistances



Predefined tag keys for entity resistance:



```java

import com.auranite.abloom.ElementResistanceRegistry.Tags;



// Fire resistance tag

TagKey<EntityType<?>> fireResistance = Tags.FIRE\_RESISTANCE;



// Fire weakness tag

TagKey<EntityType<?>> fireWeakness = Tags.FIRE\_WEAKNESS;



// All element tags are available:

// WATER\_RESISTANCE, WATER\_WEAKNESS

// EARTH\_RESISTANCE, EARTH\_WEAKNESS

// WIND\_RESISTANCE, WIND\_WEAKNESS

// ICE\_RESISTANCE, ICE\_WEAKNESS

// ELECTRIC\_RESISTANCE, ELECTRIC\_WEAKNESS

// PHYSICAL\_RESISTANCE, PHYSICAL\_WEAKNESS

// SOURCE\_RESISTANCE, SOURCE\_WEAKNESS

// NATURAL\_RESISTANCE, NATURAL\_WEAKNESS

// QUANTUM\_RESISTANCE, QUANTUM\_WEAKNESS

// ETHER\_RESISTANCE, ETHER\_WEAKNESS

// LIGHT\_RESISTANCE, LIGHT\_WEAKNESS

// SHADOW\_RESISTANCE, SHADOW\_WEAKNESS

```



\---



\## Display API



\### ElementDamageDisplayManager



Manage damage numbers and status texts:



```java

import com.auranite.abloom.ElementDamageDisplayManager;

import com.auranite.abloom.ElementType;

import net.minecraft.network.chat.Component;

import net.minecraft.world.entity.LivingEntity;



// Check if damage numbers are enabled (from config)

boolean enabled = AbloomConfig.areDamageNumbersEnabled();



// Check if status texts are enabled (from config)

boolean statusEnabled = AbloomConfig.areStatusTextsEnabled();



// Spawn damage number

ElementDamageDisplayManager displayManager = new ElementDamageDisplayManager();

displayManager.spawnDamageNumber(entity, 10.5f, ElementType.FIRE);



// Spawn status text with component

displayManager.spawnStatusText(entity, Component.translatable("elemental.tooltip.overheating"), 0xFF5500);



// Spawn status text with string

displayManager.spawnStatusText(entity, "OVERHEATING!", 0xFF5500);

```



\---



\## Config API



\### AbloomConfig



Access configuration values:



```java

import com.auranite.abloom.config.AbloomConfig;



// Check if damage numbers are enabled

boolean damageEnabled = AbloomConfig.areDamageNumbersEnabled();



// Check if status texts are enabled

boolean statusEnabled = AbloomConfig.areStatusTextsEnabled();



// Get damage number spawn radius

int spawnRadius = AbloomConfig.getDamageNumberSpawnRadius();



// Get spawn radius squared (for distance checks)

int spawnRadiusSq = AbloomConfig.getDamageNumberSpawnRadiusSq();

```



\---



\## Complete Example: Custom Mod Integration



```java

package com.example.mymod;



import com.auranite.abloom.\*;

import net.minecraft.world.item.Item;

import net.minecraft.world.item.Rarity;



public class MyModElements {

&#x20;   

&#x20;   public static final Item DRAGON\_SWORD = new Item(new Item.Properties().rarity(Rarity.RARE));

&#x20;   public static final Item ICE\_BOW = new Item(new Item.Properties().rarity(Rarity.RARE));

&#x20;   public static final Item IRON\_CHESTPLATE = new Item(new Item.Properties().rarity(Rarity.COMMON));

&#x20;   

&#x20;   public static void init() {

&#x20;       // Register elemental weapons

&#x20;       registerElementalWeapons();

&#x20;       

&#x20;       // Register armor resistances

&#x20;       registerArmorResistances();

&#x20;       

&#x20;       // Register projectiles if needed

&#x20;       registerProjectiles();

&#x20;   }

&#x20;   

&#x20;   private static void registerElementalWeapons() {

&#x20;       // Make dragon sword deal fire damage with 3x accumulation

&#x20;       ElementalWeaponUtils.registerItem(DRAGON\_SWORD, ElementType.FIRE, 3.0f);

&#x20;       

&#x20;       // Make ice bow deal ice damage with 2x accumulation

&#x20;       ElementalWeaponUtils.registerItem(ICE\_BOW, ElementType.ICE, 2.0f);

&#x20;   }

&#x20;   

&#x20;   private static void registerArmorResistances() {

&#x20;       // Make iron chestplate resistant to fire

&#x20;       ArmorResistanceRegistry.registerArmor(

&#x20;           IRON\_CHESTPLATE,

&#x20;           ElementType.FIRE,

&#x20;           0.3f  // 30% resistance

&#x20;       );

&#x20;   }

&#x20;   

&#x20;   private static void registerProjectiles() {

&#x20;       // Make fireball projectiles fire-elemental

&#x20;       ElementalProjectileRegistry.registerProjectile(

&#x20;           EntityType.FIREBALL,

&#x20;           ElementType.FIRE,

&#x20;           1.5f  // 1.5x accumulation

&#x20;       );

&#x20;       

&#x20;       // Make arrows inherit element from shooter's weapon

&#x20;       ElementalProjectileRegistry.setInheritElementFromShooter(true);

&#x20;   }

}

```



\---



\## Best Practices



1\. \*\*Use ElementalWeaponUtils over ElementalWeaponRegistry\*\* for convenience

2\. \*\*Always check for null\*\* when getting elements from items

3\. \*\*Use config values\*\* instead of hardcoding display settings

4\. \*\*Register in common setup\*\* (not client or server only)

5\. \*\*Test with `/reload`\*\* when using datapacks during development

6\. \*\*Clamp values\*\* between `-0.99` and `0.99` for resistances



\---



\## Migration from Old Versions



If upgrading from an older version:



1\. \*\*ElementalWeaponComponent\*\* - Already in place, no changes needed

2\. \*\*ElementalProjectileRegistry\*\* - New API for projectiles

3\. \*\*ElementResistanceManager\*\* - New resistance calculation

4\. \*\*Config API\*\* - Moved to AbloomConfig class

