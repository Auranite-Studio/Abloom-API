# API Reference

This page documents the public API for mod developers integrating with Abloom-API.

## DamageModificationManager

Manage external damage modifiers that run before or after Abloom's core processing.

### Methods

#### `registerModifier(DamageModifier modifier, int priority)`

Register a custom damage modifier.

| Parameter | Type | Description |
|-----------|------|-------------|
| `modifier` | `DamageModifier` | Functional interface for modifying damage |
| `priority` | `int` | Processing priority (>0 = before Abloom, ≤0 = after Abloom) |

```java
import com.auranite.abloom.DamageModificationManager;
import com.auranite.abloom.ElementDamageHandler.DamageState;
import com.auranite.abloom.ElementType;
import net.minecraft.world.entity.LivingEntity;

// Register a modifier that boosts electric damage
DamageModificationManager.registerModifier((target, state, element) -> {
    if (element == ElementType.ELECTRIC) {
        return state.withAmount(state.getAmount() * 1.2f);
    }
    return state;
}, 100);  // High priority = runs BEFORE Abloom
```

### DamageModifier Interface

```java
@FunctionalInterface
public interface DamageModifier {
    DamageState modify(LivingEntity target, DamageState state, ElementType element);
}
```

### DamageState Class

| Method | Description | Returns |
|--------|-------------|---------|
| `getAmount()` | Current damage amount | `float` |
| `withAmount(float amount)` | Create new state with modified amount | `DamageState` |

### DamagePriority Annotation

Use this annotation on event handler methods to specify processing priority:

```java
@DamagePriority(50)
@SubscribeEvent
public void onElementalDamage(ElementalDamageEvent event) {
    // Custom damage handling
}
```

## ElementalWeaponUtils

Convenience utility class for registering elemental weapons.

### Methods

| Method | Description | Example |
|--------|-------------|---------|
| `registerItem(Item, ElementType, float)` | Register single item as elemental weapon | `ElementalWeaponUtils.registerItem(sword, ElementType.FIRE, 1.0f)` |
| `registerItemById(String, String, ElementType)` | Register by resource location | `ElementalWeaponUtils.registerItemById("mymod", "my_sword", ElementType.WATER)` |
| `registerMultiple(ElementType, Item...)` | Register multiple items at once | `ElementalWeaponUtils.registerMultiple(ElementType.ELECTRIC, item1, item2)` |
| `createElementalItem(Item, ElementType, int)` | Create an ItemStack with elemental data | `ElementalWeaponUtils.createElementalItem(sword, ElementType.FIRE, 50)` |

### Usage Examples

```java
import com.auranite.abloom.ElementType;
import com.auranite.abloom.ElementalWeaponUtils;
import net.minecraft.world.item.Item;

public class WeaponRegistration {

    public static void registerAll() {
        // Single weapon
        ElementalWeaponUtils.registerItem(
            ModItems.DRAGON_SWORD.get(),
            ElementType.FIRE,
            1.5f  // 150% damage multiplier
        );

        // Multiple weapons at once
        ElementalWeaponUtils.registerMultiple(
            ElementType.ELECTRIC,
            ModItems.RAILGUN.get(),
            ModItems.THUNDER_ROD.get(),
            ModItems.LIGHTNING_STAFF.get()
        );

        // Create elemental item for testing/giving
        ItemStack elementalSword = ElementalWeaponUtils.createElementalItem(
            ModItems.RUNE_SWORD.get(),
            ElementType.SHADOW,
            0  // start with 0 accumulation
        );
    }
}
```

## ElementalWeaponComponent

Custom data component for ItemStacks that stores elemental weapon information.

### Data Structure

```
ElementalWeaponComponent
├── element: ElementType       // Primary element
├── accumulation: int          // Current accumulation value
├── damageMultiplier: float    // Damage multiplier (default 1.0)
└── critChance: float          // Bonus crit chance (default 0.0)
```

### Accessing Component Data

```java
import com.auranite.abloom.ElementalWeaponComponent;
import com.auranite.abloom.ElementType;

public class ItemUsage {

    public void onItemUse(Player player, ItemStack stack) {
        // Get component from stack
        Optional<ElementalWeaponComponent> component = stack.get(ComponentTypes.COMPONENT_TYPE.get("abloom:weapon"));
        
        if (component.isPresent()) {
            ElementalWeaponComponent data = component.get();
            ElementType element = data.getElement();
            float multiplier = data.getDamageMultiplier();
            
            // Use the data
            player.sendMessage(TranslatableComponent.text("Abloom: %s element, %.1fx multiplier", element.getName(), multiplier), Util.NIL_UUID);
        }
    }
}
```

## ElementalResistanceComponent

Custom data component for armor items that stores elemental resistances.

### Data Structure

```
ElementalResistanceComponent
└── resistances: Map<ElementType, Float>
    ├── FIRE: 0.5          // 50% fire resistance
    ├── ICE: 0.3           // 30% ice resistance
    └── SHADOW: 0.2        // 20% shadow resistance
```

### Accessing Resistance Data

```java
import com.auranite.abloom.ElementalResistanceComponent;

public class ArmorCheck {

    public float getResistance(LivingEntity entity, ElementType element) {
        // Get equipped armor
        ItemStack chestplate = entity.getItemBySlot(EquipmentSlot.CHEST);
        
        // Check for resistance component
        float resistance = ElementalResistanceComponent.getResistance(chestplate, element);
        
        return resistance;  // 0.5 = 50% reduction
    }
}
```

## AbloomModAttachments

Entity attachment types for storing elemental data on entities.

### ELEMENT_ACCUMULATOR

Stores accumulation points per element per entity.

```java
import com.auranite.abloom.AbloomModAttachments;
import com.auranite.abloom.ElementType;
import net.minecraft.world.entity.Entity;

// Get or create accumulator map
Map<ElementType, Integer> accumulator = entity.getMutableData()
    .computeIfAbsent(AbloomModAttachments.ELEMENT_ACCUMULATOR, HashMap::new);

// Update accumulation
accumulator.merge(ElementType.FIRE, 1, Integer::sum);

// Get current accumulation
int fireAccumulation = accumulator.getOrDefault(ElementType.FIRE, 0);
```

### PROJECTILE_ELEMENT

Stores the element type for projectile entities.

```java
// Attach element to projectile
entity.getMutableData().put(AbloomModAttachments.PROJECTILE_ELEMENT, ElementType.WATER);

// Read element from projectile
ElementType element = entity.getMutableData().get(AbloomModAttachments.PROJECTILE_ELEMENT);
```

## AbloomAttributes

Custom attribute registry for elemental combat stats.

### Attributes

| Attribute | Identifier | Type | Default | Max |
|-----------|-----------|------|---------|-----|
| `CRIT_DMG` | `abloom:crit_damage` | double | 0 | 10 |
| `CRIT_CHANCE` | `abloom:crit_chance` | double | 0 | 1 |

### Usage

```java
import com.auranite.abloom.AbloomAttributes;

// Register attribute modifier
public static void applyElementalBonuses(LivingEntity entity) {
    AttributeInstance critChance = entity.getAttribute(AbloomAttributes.CRIT_CHANCE);
    if (critChance != null) {
        critChance.addLegacyModifier(new AttributeModifier(
            UUID.randomUUID(),
            "abloom.bonus_crit",
            0.15,  // +15% crit chance
            AttributeModifier.Operation.ADD_VALUE
        ));
    }
}
```

## ElementalDamageDisplayManager

Controls floating damage numbers and status effect text on hit entities.

### Configuration

```java
import com.auranite.abloom.ElementalDamageDisplayManager;

// Toggle damage numbers
ElementalDamageDisplayManager.setShowDamageNumbers(true);

// Toggle status text
ElementalDamageDisplayManager.setShowStatusText(true);

// Set spawn radius (1-128)
ElementalDamageDisplayManager.setSpawnRadius(64);
```

### Display Types

| Display Type | Description | Visual |
|-------------|-------------|--------|
| **Damage Number** | Floating number showing damage amount | `123` (colored by element) |
| **Status Text** | Threshold effect notification | `Rupture!` (animated) |

## ElementalProjectileRegistry

Registry for elemental projectile entities.

### Usage

```java
import com.auranite.abloom.ElementalProjectileRegistry;

// Register a custom elemental projectile
ElementalProjectileRegistry.register(
    "water_bolt",  // Identifier
    WaterBoltEntity::new,  // Supplier
    ElementType.WATER  // Associated element
);
```

## ArmorResistanceRegistry

Registry for armor-based elemental resistances.

### Methods

| Method | Description |
|--------|-------------|
| `registerResistance(Item, ElementType, float)` | Register armor resistance for an item |
| `getResistance(ItemStack, ElementType)` | Get effective resistance from armor |
| `getAllResistances(ItemStack)` | Get all resistances from equipped armor |

### Usage

```java
import com.auranite.abloom.ArmorResistanceRegistry;

// Register armor resistance
ArmorResistanceRegistry.registerResistance(
    ModItems.DRAGON_CHESTPLATE.get(),
    ElementType.FIRE,
    0.8f  // 80% fire resistance
);

// Query resistance
float resistance = ArmorResistanceRegistry.getResistance(
    player.getItemBySlot(EquipmentSlot.CHEST),
    ElementType.FIRE
);
```

## Config Classes

### AbloomConfig

Access the current configuration:

```java
import com.auranite.abloom.config.AbloomConfig;

// Get config values
boolean showDamageNumbers = AbloomConfig.getInstance().isShowDamageNumbers();
boolean showStatusText = AbloomConfig.getInstance().isShowStatusText();
int spawnRadius = AbloomConfig.getInstance().getSpawnRadius();
```

### Config Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `showDamageNumbers` | boolean | true | Enable floating damage numbers |
| `showStatusText` | boolean | true | Enable threshold effect text |
| `spawnRadius` | int | 64 | Radius for damage display entities (1-128) |
