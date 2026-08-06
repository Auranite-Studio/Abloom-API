# Core Concepts

This page covers the fundamental systems and mechanics of Abloom-API.

## Element Types

Abloom-API supports **13 elemental types**, each with unique damage characteristics and visual representations.

| Element | Damage ID | Color | Icon |
|---------|-----------|-------|------|
| **FIRE** | `fire_dmg` | `#FF5500` | 🔥 |
| **PHYSICAL** | `physical_dmg` | `#C0C0C0` | ⚔️ |
| **WIND** | `wind_dmg` | `#00FFFF` | 💨 |
| **EARTH** | `earth_dmg` | `#8B4513` | 🪨 |
| **WATER** | `water_dmg` | `#0080FF` | 💧 |
| **ICE** | `ice_dmg` | `#00BFFF` | ❄️ |
| **ELECTRIC** | `electric_dmg` | `#FF19FF` | ⚡ |
| **ENERGY** | `energy_dmg` | `#FFFF00` | ✨ |
| **NATURAL** | `natural_dmg` | `#32CD32` | 🌿 |
| **QUANTUM** | `quantum_dmg` | `#9400D3` | 🔮 |
| **ETHER** | `ether_dmg` | `#24B3A7` | 👻 |
| **LIGHT** | `light_dmg` | `#FFFFE0` | ☀️ |
| **SHADOW** | `shadow_dmg` | `#4B0082` | 🌑 |

### Element Mapping

Abloom-API automatically maps vanilla Minecraft damage types to elemental types:

| Vanilla Damage Type | Elemental Type |
|---------------------|----------------|
| `lava` | FIRE |
| `fire` | FIRE |
| `on_fire` | FIRE |
| `lightning_bolt` | ELECTRIC |
| `freeze_arrow` | ICE |
| `drown` | WATER |
| `cactus` | EARTH |
| `wither` | SHADOW |
| ... | ... |

## Resonance Mechanics

The core mechanic of Abloom-API is **resonance accumulation** — tracking elemental damage over time.

### How It Works

1. **Accumulation**: Each hit of an elemental type adds **1 point** to the target's accumulator
2. **Threshold**: When the accumulator reaches **100 points**, a threshold effect triggers
3. **Reset Delay**: If the target doesn't receive damage of that type for **300 ticks (15 seconds)**, the accumulator resets
4. **Display Cooldown**: Damage numbers display with a **5-tick cooldown** per entity, with a maximum of **500** active displays

### Accumulation Flow

```
Hit 1 → 1 pt    Hit 10 → 10 pts    Hit 50 → 50 pts    Hit 100 → TRIGGER!
                                                ↓
                                   Reset to 0 (or continue stacking)
```

### Entity Storage

Accumulation values are stored via **Entity Attachments**:

```java
// Access an entity's accumulator
Map<ElementType, Integer> accumulator = entity.getMutableData().get(AbloomModAttachments.ELEMENT_ACCUMULATOR);

// Get accumulation for a specific element
int points = accumulator.getOrDefault(ElementType.FIRE, 0);

// Get projectile element
ElementType element = projectile.getMutableData().get(AbloomModAttachments.PROJECTILE_ELEMENT);
```

## Threshold Effects

When an accumulator reaches 100 points, the corresponding threshold effect activates. Each element has a unique effect:

| Element | Effect Name | Duration | Impact |
|---------|-------------|----------|--------|
| **PHYSICAL** | Rupture | 6s (120 ticks) | Damage ×2, armor −30% |
| **FIRE** | Burn | 10s (200 ticks) | Sets target on fire |
| **WIND** | Windswept | 8s (160 ticks) | Any non-wind damage triggers resonance |
| **WATER** | Wetness | 12s (240 ticks) | Damage ×1.5, accumulation +100% |
| **EARTH** | Stun | 5s (100 ticks) | Cannot move or attack |
| **ICE** | Freeze | 12s (240 ticks) | Freezes target, damage on full freeze |
| **ELECTRIC** | Shock | 10s (200 ticks) | Deals 20% less damage |
| **ENERGY** | Overload | 10s (200 ticks) | Takes 20% more damage |
| **NATURAL** | Bloom | 8s (160 ticks) | 1 damage/tick + 20% universal vulnerability |
| **QUANTUM** | Break | 6s (120 ticks) | Ignores armor (armor = 0) |
| **ETHER** | Corruption | 8s (160 ticks) | 20% reduced resistance, periodic damage |
| **LIGHT** | Dispersion | 10s (200 ticks) | Increased damage taken |
| **SHADOW** | Eclipse | 10s (200 ticks) | Defense/damage −10% per negative effect |

## Damage Priority System

Abloom-API processes damage through a three-tier priority system:

```
┌─────────────────────────────────────┐
│  High Priority Modifiers (P > 0)    │  ← Custom pre-processing
├─────────────────────────────────────┤
│  Abloom Core Damage Handler         │  ← Main elemental logic
├─────────────────────────────────────┤
│  Low Priority Modifiers (P ≤ 0)     │  ← Custom post-processing
└─────────────────────────────────────┘
```

### Registering Modifiers

```java
DamageModificationManager.registerModifier((entity, damage, element, modifier) -> {
    // Modify damage before Abloom processing
    return modifier.withAmount(modifier.getAmount() * 1.5f);
}, 100);  // Priority > 0 = runs BEFORE Abloom
```

See [API Reference](/api-reference) for full modifier documentation.

## Custom Attributes

Abloom-API adds two custom attributes for elemental combat:

| Attribute | Type | Range | Default | Description |
|-----------|------|-------|---------|-------------|
| **CRIT_DMG** | double | 0–10 | 0 | Critical hit damage multiplier |
| **CRIT_CHANCE** | double | 0–1 | 0 | Critical hit chance (percentage) |

### Usage

```java
// Get attribute value
double critChance = entity.getAttributeValue(AbloomAttributes.CRIT_CHANCE);

// Modify attribute
AttributeInstance instance = entity.getAttribute(AbloomAttributes.CRIT_DMG);
instance.setBaseValue(1.5);  // 150% crit damage
```

## Mob Resistances

Mobs can have elemental resistances or weaknesses defined via **entity type tags**:

| Tag | Resistance Value | Effect |
|-----|------------------|--------|
| `abloom:element_resistance/fire/half_resist` | 0.5 | 50% damage reduction |
| `abloom:element_weakness/ice/weakness` | -0.5 | 50% damage increase |

### Example Mob Resistance Config

A mob with fire resistance and ice weakness:

```json
// data/abloom/tags/entity_type/element_resistance/fire/half_resist.json
{
  "values": [
    "minecraft:blaze",
    "minecraft:magma_cube"
  ]
}

// data/abloom/tags/entity_type/element_weakness/ice/weakness.json
{
  "values": [
    "minecraft:stray",
    "minecraft:skeleton"
  ]
}
```

## Damage Modification Manager

For external mods that need to modify damage processing:

```java
// DamageModifier interface
@FunctionalInterface
public interface DamageModifier {
    DamageState modify(LivingEntity target, DamageState state, ElementType element);
}

// Register a modifier
DamageModificationManager.registerModifier((target, state, element) -> {
    // Custom logic
    return state.withAmount(state.getAmount() * 0.8f);
}, 0);  // Priority 0 = runs AFTER Abloom
```
