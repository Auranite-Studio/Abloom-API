# Abloom API Mod Documentation

## Overview

**Abloom API** is a library mod for NeoForge (version 21.1.215+, Minecraft 1.21.x) that adds an elemental damage system, elemental energy accumulation, and threshold effects. The mod provides infrastructure for creating weapons with elemental properties, projectiles, and a resistance system for mobs.

---

## Key Features

### 1. Element System

The mod defines 10 element types:

| Element | Damage ID | Damage Color |
|---------|----------|------------|
| FIRE | `fire_dmg` | #FF5500 |
| PHYSICAL | `physical_dmg` | #FFAA00 |
| WIND | `wind_dmg` | #00FFFF |
| EARTH | `earth_dmg` | #8B4513 |
| WATER | `water_dmg` | #0080FF |
| ICE | `ice_dmg` | #00BFFF |
| ELECTRIC | `electric_dmg` | #FF19FF |
| SOURCE | `source_dmg` | #FF5C77 |
| NATURAL | `natural_dmg` | #32CD32 |
| QUANTUM | `quantum_dmg` | #9400D3 |

### 2. Resonance Accumulation Mechanics

- When taking damage from an element, the target accumulates resonance points of that element
- Base accumulation value: **1 resonance point per hit**
- Activation threshold: **100 resonance points**
- When the threshold is reached, a special effect triggers and resonance points are reset
- Accumulation resets after **300 ticks** (15 seconds) without receiving damage of that type

### 3. Resonance Effects

When reaching 100 accumulation resonance points:

| Element | Effect |
|---------|--------|
| **PHYSICAL** | Physical resonance explosion, deals 500% damage to the target |
| **FIRE** | Fire resonance explosion, applies Burning effect for 10 seconds |
| **WIND** | Wind resonance explosion, applies Levitation effect for 6 seconds |
| **WATER** | Water resonance explosion, applies Wetness effect for 15 seconds (increases resonance accumulation on target by 100%) |
| **EARTH** | Earth resonance explosion, applies Stun effect for 6 seconds (target cannot deal damage or move) |
| **ICE** | Ice resonance explosion, applies Freeze effect for 12 seconds |
| **ELECTRIC** | Electric resonance explosion, applies Shock effect for 10 seconds (target deals 20% less damage) |
| **SOURCE** | Source resonance explosion, applies Rift effect for 10 seconds (damage taken by target increased by 20%) |
| **NATURAL** | Natural resonance explosion, applies Bloom effect for 8 seconds (target takes 1 damage per second and receives 20% universal vulnerability) |
| **QUANTUM** | Quantum resonance explosion, applies Penetration effect for 8 seconds (all damage to target ignores its defense) |

---

## Developer API

### Registering Elemental Weapons

#### Method 1: Via ElementalWeaponRegistry

```java
import com.auranite.abloom.ElementalWeaponRegistry;
import com.auranite.abloom.ElementType;
import net.minecraft.world.item.Item;

// Register with default accumulation amount (1.0 point per hit)
ElementalWeaponRegistry.registerWeapon(myItem, ElementType.FIRE);

// Register with custom accumulation amount
ElementalWeaponRegistry.registerWeapon(myItem, ElementType.ICE, 2.5f);
```

#### Method 2: Via Data Component

```java
import com.auranite.abloom.ElementalWeaponComponent;
import com.auranite.abloom.ElementType;
import net.minecraft.world.item.ItemStack;

// Create an elemental item
ItemStack stack = new ItemStack(myItem);
ElementalWeaponComponent.withElement(stack, ElementType.ELECTRIC);

// Create with custom accumulation amount
ElementalWeaponComponent.withElementAndAccum(stack, ElementType.WATER, 1.5f);

        // Get element from item
        Optional<ElementType> element = ElementalWeaponComponent.getElement(stack);

        // Check if item has an element
        boolean hasElement = ElementalWeaponComponent.hasElement(stack);
```

### Registering Elemental Projectiles

```java
import com.auranite.abloom.ElementalProjectileRegistry;
import com.auranite.abloom.ElementType;
import net.minecraft.world.entity.EntityType;

// Register by entity type
ElementalProjectileRegistry.registerProjectile(
        EntityType.ARROW,
        ElementType.FIRE,
    1.5f  // accumulation points amount
);

// Register by entity class
ElementalProjectileRegistry.registerProjectileByClass(
        MyCustomArrow.class,
        ElementType.ICE,
    2.0f  // accumulation points amount
);

// Enable element inheritance from shooter
ElementalProjectileRegistry.setInheritElementFromShooter(true);
```

#### Creating and Launching Projectiles

```java
import com.auranite.abloom.ElementalProjectileRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;

// Create projectile with element from shooter's weapon
ElementalProjectileRegistry.createAndLaunchElementalProjectile(
        serverLevel,
        shooter,
        EntityType.ARROW,
    1.5f,  // velocity
                1.0f   // inaccuracy
);

// Create projectile with forced element
ElementalProjectileRegistry.createElementalProjectileWithOverride(
        serverLevel,
        shooter,
        EntityType.SNOWBALL,
        ElementType.FIRE,  // forced element
    1.5f,
                1.0f
);
```

### Configuring Element Resistances

#### Via Tags (Recommended)

Create JSON tag files in `data/abloom/tags/entity_type/element/<element>/`:

**Example: `data/abloom/tags/entity_type/element/fire/immune.json`**
```json
{
  "values": [
    "minecraft:blaze",
    "minecraft:magma_cube"
  ]
}
```

**Example: `data/abloom/tags/entity_type/element/ice/weakness.json`**
```json
{
  "values": [
    "minecraft:stray",
    "minecraft:polar_bear"
  ]
}
```

Available modifiers:
- `immune` — full immunity (damage = 0, accumulation = 0)
- `resistance` — resistance (damage × 0.5, accumulation × 0.5)
- `weakness` — weakness (damage × 1.5, accumulation × 1.5)

#### Programmatic Registration

```java
import com.auranite.abloom.ElementResistanceRegistry;
import com.auranite.abloom.ElementType;
import net.minecraft.world.entity.EntityType;

// Register immunity for multiple entity types
ElementResistanceRegistry.registerUniform(
        ElementType.FIRE,
    0.0f,  // accumulation resistance
            0.0f,  // damage resistance
        EntityType.BLAZE,
        EntityType.MAGMA_CUBE
        );

// Register weakness
ElementResistanceRegistry.registerSingleUniform(
        EntityType.ZOMBIE,
        ElementType.FIRE,
    1.5f  // accumulation points amount
);

// Register custom resistances
Map<ElementType, ElementResistanceManager.Resistance> resistances = new EnumMap<>(ElementType.class);
resistances.put(ElementType.FIRE, new ElementResistanceManager.Resistance(0.5f, 0.5f));
        resistances.put(ElementType.ICE, new ElementResistanceManager.Resistance(1.5f, 1.0f));

        ElementResistanceRegistry.registerMultiple(EntityType.CREEPER, resistances);
```

### Applying Elemental Damage Programmatically

```java
import com.auranite.abloom.ElementDamageHandler;
import com.auranite.abloom.ElementType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

// Apply elemental damage from source
ElementDamageHandler.applyElementalDamageWithSource(
        targetEntity,      // target
        sourceEntity,      // damage source
    5.0f,              // base damage
        ElementType.FIRE,  // element type
    1.5f               // accumulation points amount
);
```

### Working with Status Effects

The mod adds 8 new effects:

| Effect | ID | Color | Description |
|--------|----|------|-------------|
| BURNING | `burning` | #FF5500 | Ignites target every second |
| WETNESS | `wetness` | #0080FF | Increases resonance accumulation |
| STUN | `stun` | #8B4513 | Stuns the target |
| FREEZE | `freeze` | #00BFFF | Freezes and slows down |
| SHOCK | `shock` | #FF19FF | Reduces damage dealt by target |
| BREAK | `break` | #9400D3 | Special destruction effect |
| BLOOM | `bloom` | #32CD32 | Increases accumulation and damage received |
| RIFT | `rift` | #FF5C77 | Increases damage taken |

#### Applying Effects

```java
import com.auranite.abloom.AbloomModEffects;
import net.minecraft.world.effect.MobEffectInstance;

// Add effect
entity.addEffect(new MobEffectInstance(
                         AbloomModEffects.BURNING,
    200,  // duration in ticks (10 seconds)
                         0,    // amplifier
                         false, // show particles
                         true   // hide icon
));
```

### Helper Methods

#### Getting Element from Damage Source

```java
import com.auranite.abloom.ElementDamageHandler;
import net.minecraft.world.damagesource.DamageSource;

ElementType type = ElementDamageHandler.getElementTypeFromSource(damageSource);
```

#### Getting Element from Item

```java
import com.auranite.abloom.ElementDamageHandler;
import net.minecraft.world.item.ItemStack;

ElementType type = ElementDamageHandler.getElementTypeFromItem(itemStack);
```

#### Spawning Colored Damage Numbers

```java
import com.auranite.abloom.ElementDamageHandler;
import net.minecraft.world.entity.LivingEntity;

// Spawn damage number
ElementDamageHandler.spawnDamageNumber(entity, 15.5f, ElementType.FIRE);

// Spawn status text
ElementDamageHandler.spawnStatusText(entity, "Overheating!", 0xFF5500);
```

---

## Configuration and Customization

### Changing Base Parameters

```java
import com.auranite.abloom.ElementDamageHandler;
import com.auranite.abloom.ElementType;

// Change damage color for element
ElementDamageHandler.setDamageColor(ElementType.FIRE, 0xFF0000);

// Get all damage colors
Map<ElementType, Integer> colors = ElementDamageHandler.getAllDamageColors();

// Get current activation threshold
int threshold = ElementDamageHandler.getThreshold();  // default 100
```

### Server Task Queues

```java
import com.auranite.abloom.AbloomMod;

// Execute task after N ticks
AbloomMod.queueServerWork(20, () -> {
        // code executes after 1 second (20 ticks)
        });
```

---

## Integration with Other Mods

### Adding Elemental Properties to Items from Other Mods

```java
// In onCommonSetup method or similar
ElementalWeaponRegistry.registerWeapon(
        ModItems.SWORD_FROM_OTHER_MOD,
        ElementType.ELECTRIC,
    1.2f
);
```

### Configuring Resistances for Mobs from Other Mods

```java
// Via tags (recommended)
// Create file: data/abloom/tags/entity_type/element/fire/immune.json
{
        "values": [
        "othermod:fire_elemental",
        "othermod:lava_golem"
        ]
        }
```

---

## Usage Examples

### Creating an Elemental Sword

```java
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, "mymod");

    public static final DeferredHolder<Item, Item> FIRE_SWORD =
            ITEMS.register("fire_sword", () -> {
                Item sword = new SwordItem(Tiers.DIAMOND,
                        new Item.Properties().attributes(
                                SwordItem.createAttributes(Tiers.DIAMOND, 3, -2.4f)
                        )
                );
                // Register as fire weapon with 1.5 accumulation points
                ElementalWeaponRegistry.registerWeapon(sword, ElementType.FIRE, 1.5f);
                return sword;
            });
}
```

### Creating a Magic Staff with Projectiles

```java
public class MagicStaffItem extends Item {
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof LivingEntity living) {
            ServerLevel serverLevel = (ServerLevel) level;

            // Launch fireball
            ElementalProjectileRegistry.createAndLaunchElementalProjectile(
                    serverLevel,
                    living,
                    EntityType.FIREBALL,
                    1.8f,
                    0.5f
            );

            player.getItemInHand(hand).hurtAndBreak(1, living,
                    EquipmentSlot.MAINHAND);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
```

### Boss with Elemental Resistances

```java
public class CustomBoss extends Monster {
    public CustomBoss(EntityType<? extends Monster> type, Level level) {
        super(type, level);

        // Register resistances on creation
        ElementResistanceRegistry.registerMultiple(this.getType(), Map.of(
                ElementType.FIRE, new ElementResistanceManager.Resistance(0.0f, 0.0f),  // immunity
                ElementType.ICE, new ElementResistanceManager.Resistance(2.0f, 1.5f),   // weakness
                ElementType.PHYSICAL, new ElementResistanceManager.Resistance(0.5f, 0.7f) // resistance
        ));
    }
}
```

---

## Debugging

```java
import com.auranite.abloom.ElementResistanceRegistry;
import com.auranite.abloom.ElementalWeaponRegistry;
import com.auranite.abloom.ElementalProjectileRegistry;

// Print information about registered resistances
ElementResistanceRegistry.debugPrint();

// Get count of registered objects
int weapons = ElementalWeaponRegistry.getRegisteredCount();
int projectiles = ElementalProjectileRegistry.getRegisteredCount();
```