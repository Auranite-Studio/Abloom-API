# Abloom API Mod Documentation

## Overview

**Abloom API** is a library mod for NeoForge (version 21.1.215+, Minecraft 1.21.1) that adds an elemental damage system, elemental energy accumulation, and threshold effects. The mod provides infrastructure for creating weapons with elemental properties, projectiles, armor with elemental resistances, and a resistance system for mobs.

---

## Key Features

### 1. Element System

The mod defines 10 element types:

| Element | Damage ID | Damage Color |
|---------|----------|------------|
| FIRE | `fire_dmg` | #FF5500 |
| PHYSICAL | `physical_dmg` | #C0C0C0 |
| WIND | `wind_dmg` | #00FFFF |
| EARTH | `earth_dmg` | #8B4513 |
| WATER | `water_dmg` | #0080FF |
| ICE | `ice_dmg` | #00BFFF |
| ELECTRIC | `electric_dmg` | #FF19FF |
| ENERGY | `energy_dmg` | #FFFF00 |
| NATURAL | `natural_dmg` | #32CD32 |
| QUANTUM | `quantum_dmg` | #9400D3 |

### 2. Resonance Accumulation Mechanics

- When taking damage from an element, the target accumulates resonance points of that element via Data Attachments
- Base accumulation value: **1 resonance point per hit**
- Activation threshold: **100 resonance points**
- When the threshold is reached, a special effect triggers and resonance points are reset
- Accumulation resets after **300 ticks** (15 seconds) without receiving damage of that type

### 3. Resonance Effects

When reaching 100 accumulation resonance points:

| Element | Effect | Duration |
|---------|--------|----------|
| **PHYSICAL** | Critical damage explosion, deals 500% damage to the target | Instant |
| **FIRE** | Fire resonance explosion, applies Burning effect | 10 seconds (200 ticks) |
| **WIND** | Wind resonance explosion, applies Levitation effect | 6 seconds (120 ticks) |
| **WATER** | Water resonance explosion, applies Wetness effect (increases resonance accumulation by 100% per level) | 15 seconds (300 ticks) |
| **EARTH** | Earth resonance explosion, applies Stun effect (target cannot deal damage or move) | 6 seconds (120 ticks) |
| **ICE** | Ice resonance explosion, applies Freeze effect | 12 seconds (240 ticks) |
| **ELECTRIC** | Electric resonance explosion, applies Shock effect (target deals 20% less damage per level) | 10 seconds (200 ticks) |
| **ENERGY** | Energy resonance explosion, applies Rift effect (damage taken by target increased by 20% per level) | 10 seconds (200 ticks) |
| **NATURAL** | Natural resonance explosion, applies Bloom effect (target takes periodic damage and receives 20% universal vulnerability per level) | 8 seconds (160 ticks) |
| **QUANTUM** | Quantum resonance explosion, applies Overload effect (all damage to target ignores defense) | 8 seconds (160 ticks) |

### 4. Custom Mob Effects

The mod includes 8 custom mob effects:
- **Burning** (Fire) - Damage over time effect
- **Wetness** (Water) - Increases resonance accumulation
- **Stun** (Earth) - Prevents movement and actions
- **Freeze** (Ice) - Immobilizes target
- **Shock** (Electric) - Reduces damage output
- **Overload** (Energy) - Increases damage taken
- **Bloom** (Natural) - Periodic damage + vulnerability
- **Break** (Quantum) - Ignores defense

### 5. Elemental Armor System

Armor pieces can have elemental resistance bonuses using data components, providing percentage-based resistance to specific element types.

---

## Developer API

### Registering Elemental Weapons

#### Method 1: Via ElementalWeaponRegistry

```java
import com.auranite.abloom.ElementalWeaponRegistry;
import com.auranite.abloom.ElementType;
import net.minecraft.world.item.Item;

// Register with default accumulation points (1.0x per hit)
ElementalWeaponRegistry.registerWeapon(myItem, ElementType.FIRE);

// Register with custom accumulation points
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

// Create with custom accumulation points
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
    1.5f  // accumulation points
);

// Register by entity class
ElementalProjectileRegistry.registerProjectileByClass(
        MyCustomArrow.class,
        ElementType.ICE,
    2.0f  // accumulation points
);

// Enable element inheritance from shooter (default: true)
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

// Register uniform resistance for multiple entity types
ElementResistanceRegistry.registerUniform(
        ElementType.FIRE,
    0.0f,  // accumulation resistance
            0.0f,  // damage resistance
        EntityType.BLAZE,
        EntityType.MAGMA_CUBE
        );

// Register single resistance
ElementResistanceRegistry.registerSingleUniform(
        EntityType.ZOMBIE,
        ElementType.FIRE,
    1.5f  // resistance value
);

// Register multiple resistances
Map<ElementType, ElementResistanceManager.Resistance> resistances = new EnumMap<>(ElementType.class);
resistances.put(ElementType.FIRE, new ElementResistanceManager.Resistance(0.5f, 0.5f));
        resistances.put(ElementType.ICE, new ElementResistanceManager.Resistance(-0.5f, -0.5f));

        ElementResistanceRegistry.registerMultiple(EntityType.CREEPER, resistances);
```

### Applying Elemental Damage Programmatically

The mod automatically handles elemental damage through the event system. For custom damage application, use the damage source system with appropriate element mapping.

### Helper Methods

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

// Spawn damage number (automatically called during damage events)
// ElementDamageHandler.spawnDamageNumber(entity, 15.5f, ElementType.FIRE);

// Spawn status text
ElementDamageHandler.spawnStatusText(entity, "Overheating!", 0xFF5500);
```

---

## Configuration

The mod supports server-side configuration for damage display settings:

```hocon
# Damage Display Settings
enableDamageNumbers=true     # Enable/disable floating damage numbers
enableStatusTexts=true       # Enable/disable status text displays
damageNumberSpawnRadius=16   # Radius (in blocks) for damage number visibility (1-128)
```

Configuration file location: `world/serverconfig/abloom-server.toml`

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
                // Register as fire weapon with 1.5x accumulation points
                ElementalWeaponRegistry.registerWeapon(sword, ElementType.FIRE, 1.5f);
                return sword;
            });
}
```

### Creating Elemental Armor with Resistances

```java
import com.auranite.abloom.ElementalResistanceComponent;
import com.auranite.abloom.ElementType;
import net.minecraft.world.item.ItemStack;

// Create armor piece with fire resistance
ItemStack chestplate = new ItemStack(ModItems.CHESTPLATE);
ElementalResistanceComponent.withResistance(chestplate, ElementType.FIRE, 0.25f); // 25% resistance

// Add multiple resistances
Map<ElementType, Float> resistances = new EnumMap<>(ElementType.class);
resistances.put(ElementType.ICE, 0.5f);    // 50% ice resistance
resistances.put(ElementType.ELECTRIC, 0.3f); // 30% electric resistance
ElementalResistanceComponent.withResistances(chestplate, resistances);
```

### Creating a Magic Staff with Projectiles

```java
public class MagicStaffItem extends Item {
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof LivingEntity living) {
            ServerLevel serverLevel = (ServerLevel) level;

            // Launch fireball with element from shooter's weapon
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
import com.auranite.abloom.ElementResistanceManager;
import com.auranite.abloom.ElementResistanceRegistry;
import com.auranite.abloom.ElementType;

public class CustomBoss extends Monster {
    public CustomBoss(EntityType<? extends Monster> type, Level level) {
        super(type, level);

        // Register resistances on creation
        Map<ElementType, ElementResistanceManager.Resistance> resistances = new EnumMap<>(ElementType.class);
        resistances.put(ElementType.FIRE, ElementResistanceManager.Resistance.IMMUNE);      // Full immunity
        resistances.put(ElementType.ICE, new ElementResistanceManager.Resistance(-0.5f, -0.5f)); // Weakness
        resistances.put(ElementType.PHYSICAL, ElementResistanceManager.Resistance.HALF_RESIST); // 50% resistance
        
        ElementResistanceRegistry.registerMultiple(this.getType(), resistances);
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

---

## Technical Details

### Data Components

The mod uses NeoForge's data component system for storing elemental properties:

- **Elemental Weapon Component**: Stores element type and accumulation points on weapons
- **Elemental Resistance Component**: Stores resistance values for each element type on armor
- **Data Attachments**: Used for runtime data like resonance accumulation points and projectile elements

### Network Synchronization

Damage number displays and status texts are synchronized to clients using NeoForge's payload system for smooth visual feedback.

### Performance Considerations

- Damage display entities are automatically cleaned up when chunks unload or entities leave the world
- Maximum of 500 active damage displays to prevent performance issues
- Lazy loading of resistance tags for optimal startup performance
