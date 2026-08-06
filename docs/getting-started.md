# Getting Started

Welcome to **Abloom-API** — an advanced elemental damage system library for NeoForge 1.21.1. This guide will help you install and integrate the API into your mod.

## Prerequisites

Before getting started, ensure you have:

- **Minecraft 1.21.1**
- **NeoForge 21.1.215**
- **Java 21**
- **Gradle** (for build configuration)

## Installation

### 1. Add the Dependency

Abloom-API is available on [Modrinth](https://modrinth.com/mod/abloom-api). Add the following to your `build.gradle`:

```groovy
repositories {
    maven {
        url = uri("https://api.modrinth.com/maven")
        content {
            includeGroup "maven.mojmirror"
        }
    }
}

dependencies {
    implementation("maven.mojmirror:abloom-api:1.0.0-beta.19+1.21.1-neoforge")
}
```

### 2. Add the Mod Dependency

In your `mods.toml` (or NeoForge annotation), declare Abloom-API as a runtime dependency:

```java
@Mod(value = "your_mod_id", modType = ModType.Lib)
public class YourMod {
    // Your mod code
}
```

Or in your `mods.toml`:

```toml
[dependencies.your_mod_id]
modId = "abloom"
type = "required"
version = "1.0.0-beta.19"
```

### 3. Initialize Integration

Create an initialization class that registers your elemental weapons or items:

```java
public class YourModIntegration {

    public static void registerElementalWeapons() {
        // Register a single weapon
        ElementalWeaponUtils.registerItem(
            YourItems.YOUR_SWORD.get(),
            ElementType.FIRE,
            1.0f  // damage multiplier
        );

        // Register multiple items of the same element
        ElementalWeaponUtils.registerMultiple(
            ElementType.ELECTRIC,
            YourItems.RAY_GUN.get(),
            YourItems.CHARGE_LANCE.get()
        );
    }
}
```

Call this method from your mod's initialization event:

```java
@Mod.EventBusSubscriber(modid = "your_mod_id", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @Mod.EventBusSubscriber.Bus.Mod.Value
    public static void onSetup(final CreativeModeTabs event) {
        YourModIntegration.registerElementalWeapons();
    }
}
```

## Quick Start Examples

### Example 1: Simple Elemental Sword

```java
// Create an elemental item stack
ItemStack elementalSword = ElementalWeaponUtils.createElementalItem(
    YourItems.YOUR_SWORD.get(),
    ElementType.WATER,
    50  // initial accumulation value
);

// Use in your code
player.setItemInHand(EquipmentSlot.MAINHAND, elementalSword);
```

### Example 2: Multi-Element Weapon

For weapons that change elements based on attack stages, use the datapack system:

```json
// data/your_mod/elemental_weapons/my_weapon.json
{
  "item": "your_mod:my_weapon",
  "element": "FIRE",
  "accumulation_multiplier": 1.5,
  "crit_chance": 0.1,
  "crit_damage": 1.5,
  "stages": [
    {
      "element": "FIRE",
      "accumulation_multiplier": 1.0
    },
    {
      "element": "WATER",
      "accumulation_multiplier": 1.0
    }
  ]
}
```

See the [Datapack Guide](/datapack-guide) for more details.

## Next Steps

- Read the [Core Concepts](/core-concepts) to understand the element system
- Check the [API Reference](/api-reference) for integration details
- Explore the [Effects Reference](/effects) to see all available threshold effects
