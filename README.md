# Abloom API Mod Documentation

## Overview

**Abloom API** is a library mod for NeoForge (1.21.1) that adds an elemental damage system, elemental energy accumulation, and threshold effects. The mod provides infrastructure for creating weapons with elemental properties, projectiles, armor with elemental resistances, and a resistance system for mobs.

**Mod ID:** `abloom`
**NeoForge Version:** 21.1.215
**Java Version:** 21

---

## Key Features

### 1. Element System

The mod defines 13 element types:

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
| ETHER | `ether_dmg` | #24B3A7 |
| LIGHT | `light_dmg` | #FFFFE0 |
| SHADOW | `shadow_dmg` | #4B0082 |

### 2. Resonance Accumulation Mechanics

- When taking damage from an element, the target accumulates resonance points of that element via Data Attachments
- Base accumulation value: **1 resonance point per hit**
- Activation threshold: **100 resonance points**
- When the threshold is reached, a special effect triggers and resonance points are reset
- Accumulation resets after **300 ticks** (15 seconds) without receiving damage of that type

### 3. Resonance Effects

When reaching 100 accumulation resonance points:

| Element      | Effect                                                                                                                                                                                                        | Duration                |
|--------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------|
| **PHYSICAL** | Physical resonance explosion, applies Rupture effect (target takes 200% initial damage, defense reduced by 30%)                                                                                               | 6 seconds (120 ticks)   |
| **FIRE**     | Fire resonance explosion, applies Burning effect                                                                                                                                                              | 10 seconds (200 ticks)  |
| **WIND**     | Wind resonance explosion, applies Windswept effect. Applying any other elemental damage (except Wind) while this effect is active triggers the corresponding resonance explosion and removes Windswept effect | 8 seconds (160 ticks)   |
| **WATER**    | Water resonance explosion, applies Wetness effect (increases resonance accumulation by 50%)                                                                                                                   | 12 seconds (240 ticks)  |
| **EARTH**    | Earth resonance explosion, applies Stun effect (target cannot deal damage or move)                                                                                                                            | 5 seconds (100 ticks)   |
| **ICE**      | Ice resonance explosion, applies Freeze effect                                                                                                                                                                | 12 seconds (240 ticks)  |
| **ELECTRIC** | Electric resonance explosion, applies Shock effect (target deals 20% less damage)                                                                                                                             | 10 seconds (200 ticks)  |
| **ENERGY**   | Energy resonance explosion, applies Overload effect (damage taken by target increased by 20%)                                                                                                                 | 10 seconds (200 ticks)  |
| **NATURAL**  | Natural resonance explosion, applies Bloom effect (target takes 1 damage per second and receives 20% universal vulnerability)                                                                                 | 8 seconds (160 ticks)   |
| **QUANTUM**  | Quantum resonance explosion, applies Break effect (all damage to target ignores defense)                                                                                                                      | 6 seconds (120 ticks)   |
| **ETHER**    | Ether resonance explosion, applies Corruption effect (target's resistance to all damage types reduced by 20% and takes periodic damage)                                                                       | 8 seconds (160 ticks)   |
| **UNKNOWN**  | Unknown resonance explosion, applies Taunt effect (the target is attacked by hostile and neutral mobs while this effect is active.)                                                                           | ??? seconds (??? ticks) |
| **LIGHT**    | Light resonance explosion, applies Dispersion effect (damage taken by target increased by a certain amount %)                                                                                                 | 10 seconds (200 ticks)  |
| **SHADOW**   | Shadow resonance explosion, applies Eclipse effect (target's defense and damage dealt reduced by 10% + 10% per additional negative effect, max 50% reduction)                                                 | 10 seconds (200 ticks)   |

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

#### Via MobResistanceRegistry (New)

The system of data tags for mob resistances has been removed. All resistances are now registered programmatically through the `MobResistanceRegistry` class.

The `MobResistanceRegistry` is automatically initialized during mod setup and contains all predefined mob resistances (e.g., Blaze is immune to Fire, Snow Golem is weak to Fire).

#### Programmatic Registration

All resistance registration has been moved to `MobResistanceRegistry`. You can still use the `ElementResistanceRegistry` for direct registration, but it is recommended to use the structured registry:

```java
import com.auranite.abloom.ElementResistanceRegistry;
import com.auranite.abloom.ElementType;
import net.minecraft.world.entity.EntityType;

// Register uniform resistance for multiple entity types
ElementResistanceRegistry.registerUniform(
        ElementType.FIRE,
    0.5f,  // resistance value (0.5 = 50% resistance)
        EntityType.BLAZE,
        EntityType.MAGMA_CUBE
        );

// Register single resistance
ElementResistanceRegistry.registerSingleUniform(
        EntityType.ZOMBIE,
        ElementType.FIRE,
    0.5f  // resistance value
);

// Register multiple resistances at once
Map<ElementType, ElementResistanceManager.Resistance> resistances = new EnumMap<>(ElementType.class);
resistances.put(ElementType.FIRE, new ElementResistanceManager.Resistance(0.5f));
resistances.put(ElementType.ICE, new ElementResistanceManager.Resistance(-0.5f));

ElementResistanceRegistry.registerMultiple(EntityType.CREEPER, resistances);
```

#### Using MobResistanceRegistry (Recommended)

The `MobResistanceRegistry` provides a structured way to register mob resistances. It is automatically initialized during mod setup and contains all predefined mob resistances.

```java
import com.auranite.abloom.MobResistanceRegistry;
import com.auranite.abloom.ElementType;
import net.minecraft.world.entity.EntityType;

// Register a custom resistance
MobResistanceRegistry.registerCustomResistance(EntityType.CREEPER, ElementType.FIRE, 0.5f, 0.5f); // 50% resistance
```

### Applying Elemental Damage Programmatically

The mod automatically handles elemental damage through the event system. For custom damage application, use the `ElementDamageHandler` class:

```java
import com.auranite.abloom.ElementDamageHandler;
import com.auranite.abloom.ElementType;
import net.minecraft.world.entity.Entity;

// Deal elemental damage to an entity
ElementDamageHandler.dealElementDamage(
        targetEntity,
        ElementType.FIRE,
        10.0f,           // damage amount
        1.5f,            // accumulation multiplier
        attackerEntity   // optional attacker
);

// Add accumulation points without damage
ElementDamageHandler.addElementPoints(entity, ElementType.ICE, 25);

// Get current accumulation progress
int progress = ElementDamageHandler.getAccumulationProgress(entity, ElementType.FIRE);
// progress will be 0-100 representing percentage to threshold
```

### Helper Methods

#### Getting Element from Item

```java
import com.auranite.abloom.ElementDamageHandler;
import com.auranite.abloom.ElementType;
import net.minecraft.world.item.ItemStack;

ElementType type = ElementDamageHandler.getElementTypeFromItem(itemStack);
if (type != null) {
    // Item has elemental properties
    System.out.println("Item deals " + type + " damage");
}
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

## Mob Resistances and Weaknesses Table

The following table shows which mobs have resistances or weaknesses to each element type:

| Element      | Resistant | Weak |
|--------------|-----------|------|
| **FIRE**     | Blaze, Magma Cube, Wither, Ender Dragon, Strider, Zombified Piglin, Wither Skeleton, Ghast, Warden, Hoglin, Piglin, Piglin Brute, Zoglin, Husk, Camel | Snow Golem, Dolphin, Zombie, Zombie Villager, Drowned, Stray, Bogged |
| **PHYSICAL** | Turtle, Armadillo, Iron Golem, Shulker, Warden, Ender Dragon | Slime, Magma Cube, Phantom, Vex, Allay, Glow Squid, Squid |
| **WIND**     | Phantom, Breeze, Ender Dragon, Ghast, Vex, Allay, Parrot, Chicken, Ocelot, Cat, Fox, Wolf | Turtle, Sniffer, Armadillo, Camel, Ravager, Hoglin, Polar Bear |
| **EARTH**    | Endermite, Silverfish, Shulker, Iron Golem, Warden, Giant, Ravager, Armadillo, Sniffer | Ghast, Phantom, Vex, Allay, Breeze |
| **WATER**    | Squid, Glow Squid, Drowned, Guardian, Elder Guardian, Axolotl, Tadpole, Frog, Turtle, Cod, Salmon, Pufferfish, Tropical Fish, Dolphin, Witch | Blaze, Snow Golem, Strider, Breeze |
| **ICE**      | Snow Golem, Stray, Polar Bear, Goat | Blaze, Magma Cube, Strider, Breeze |
| **ELECTRIC** | Creeper, Enderman, Phantom, Allay, Breeze | Drowned, Turtle, Axolotl, Frog, Tadpole, Cod, Salmon, Pufferfish, Tropical Fish, Dolphin, Squid, Glow Squid, Guardian, Elder Guardian |
| **ENERGY**   | Enderman, Shulker, Warden, Ender Dragon, Wither, Elder Guardian, Evoker, Witch | Creeper, Ghast |
| **NATURAL**  | Bogged, Wither Skeleton, Wither, Slime, Magma Cube, Bee, Wolf, Ocelot, Cat, Panda, Fox, Rabbit | Villager, Wandering Trader, Iron Golem, Snow Golem, Allay, Zoglin, Stray, Zombified Piglin, Zombie, Zombie Villager, Skeleton, Axolotl |
| **QUANTUM**  | Enderman, Endermite, Ender Dragon, Shulker, Wither, Warden | Villager, Wandering Trader, Bat, Allay |
| **ETHER**    | Ender Dragon, Wither | Enderman, Endermite, Shulker, Warden |
| **LIGHT**    | *None* | *None* |
| **SHADOW**   | *None* | *None* |

### Notes

- Resistant mobs have 50% resistance (HALF_RESIST constant)
- Weak mobs have 50% vulnerability (WEAKNESS constant = -0.5f)
- Mobs can have multiple resistances and weaknesses simultaneously
- Some mobs are commented out in source code (Camel Husk, Parched, Happy Ghast, Copper Golem, Nautilus, Zombie Nautilus)
- The system uses `ElementResistanceManager.Resistance.HALF_RESIST` (0.5f) for resistances and `ElementResistanceManager.Resistance.WEAKNESS` (-0.5f) for weaknesses

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

### Default Weapon Registration

The mod automatically registers several vanilla weapons with elemental properties:

| Item | Element | Accumulation Multiplier |
|------|---------|------------------------|
| Netherite Sword | PHYSICAL | 5x |
| Diamond Sword | PHYSICAL | 5x |
| Golden Sword | PHYSICAL | 4x |
| Iron Sword | PHYSICAL | 3x |
| Stone Sword | PHYSICAL | 2x |
| Wooden Sword | PHYSICAL | 2x |
| Netherite Axe | PHYSICAL | 7x |
| Diamond Axe | PHYSICAL | 7x |
| Golden Axe | PHYSICAL | 6x |
| Iron Axe | PHYSICAL | 5x |
| Stone Axe | PHYSICAL | 4x |
| Wooden Axe | PHYSICAL | 2x |
| Crossbow | PHYSICAL | 7x |
| Trident | PHYSICAL | 4x |
| Mace | PHYSICAL | 25x |
| Bow | PHYSICAL | 2x |

All elemental weapon sticks are registered with a 50x accumulation multiplier.

### Creating Elemental Items Programmatically

```java
import com.auranite.abloom.ElementDamageHandler;
import com.auranite.abloom.ElementType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

// Create elemental sword
ItemStack sword = ElementDamageHandler.createElementalItem(Items.DIAMOND_SWORD, ElementType.FIRE, 1);

// Create with custom accumulation
ItemStack axe = ElementDamageHandler.createElementalItemWithAccum(Items.NETHERITE_AXE, ElementType.ICE, 1, 2.0f);
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

### Creating Elemental Items Programmatically

```java
import com.auranite.abloom.ElementDamageHandler;
import com.auranite.abloom.ElementType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

// Create elemental sword
ItemStack sword = ElementDamageHandler.createElementalItem(Items.DIAMOND_SWORD, ElementType.FIRE, 1);

// Create with custom accumulation
ItemStack axe = ElementDamageHandler.createElementalItemWithAccum(Items.NETHERITE_AXE, ElementType.ICE, 1, 2.0f);
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

### Event System

The mod uses NeoForge's event system to intercept damage events:

- `LivingDamageEvent.Pre`: Intercepts incoming damage to apply elemental effects
- `LivingDeathEvent`: Cleans up active displays when entities die
- `ServerTickEvent.Pre`: Manages display cleanup and accumulation reset
- `ChunkDataEvent.Save`: Cleans up displays when chunks unload

### Effect System

The mod implements 14 status effects that trigger on resonance threshold:

| Effect | Duration | Description |
|--------|----------|-------------|
| **Burn** | 10s | Causes fire damage over time |
| **Wetness** | 12s | Increases elemental accumulation by 50% per amplifier |
| **Stun** | 5s | Prevents target from moving or dealing damage |
| **Freeze** | 12s | Completely immobilizes the target |
| **Shock** | 10s | Reduces damage dealt by 20% per amplifier |
| **Break** | 6s | Makes all damage ignore armor |
| **Rupture** | 6s | Increases damage taken by 100% |
| **Bloom** | 8s | Causes 1 damage/second + 20% vulnerability per amplifier |
| **Overload** | 10s | Increases damage taken by 20% per amplifier |
| **Windswept** | 8s | Triggers resonance on non-Wind damage |
| **Corruption** | 8s | Reduces all resistance by 20% |
| **Taunt** | 30s | Attracts hostile and neutral mobs |
| **Dispersion** | 10s | Increases damage taken by 10-30% depending on element |
| **Eclipse** | 10s | Reduces defense and damage by 10% + 5% per effect, max 50% |

### Damage Calculation Formula

The final damage is calculated in the following order:

```
// Step 1: Apply damage multiplier from effects (SHOCK, OVERLOAD, BLOOM, DISPERSION)
baseDamage * damage_multiplier_from_effects

// Step 2: Apply resistance from ElementResistanceManager (entity resistance)
 damage * (1.0 - entity_resistance)

// Step 3: Apply resistance from armor (ElementalResistanceComponent)
 damage * (1.0 - armor_resistance_bonus)

// Step 4: Threshold effect (if resonance threshold reached)
// Resonance points calculation:
basePoints * accumulation_multiplier * (1.0 - resistance)
```

### Network Synchronization

Damage number displays and status texts are synchronized to clients using NeoForge's payload system for smooth visual feedback.

### Performance Considerations

- Damage display entities are automatically cleaned up when chunks unload or entities leave the world
- Maximum of 500 active damage displays to prevent performance issues
- Lazy loading of resistance tags for optimal startup performance
- Cooldown system prevents spam of damage numbers (5 tick cooldown per entity)
- Accumulation points automatically reset after 300 ticks (15 seconds) of inactivity
- Efficient cleanup during server ticks (every 20 ticks for optimization)
