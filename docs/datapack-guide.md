# Datapack Guide

This guide covers creating datapacks to configure Abloom-API without code changes.

## Overview

Abloom-API supports three types of datapack configurations:

1. **Elemental Weapons** — Register items as elemental weapons with damage multipliers
2. **Armor Resistances** — Define elemental resistances for armor items
3. **Entity Type Tags** — Configure mob resistances and weaknesses

## Directory Structure

```
data/
├── your_mod_id/
│   ├── elemental_weapons/     # Elemental weapon definitions
│   ├── armor_resistances/     # Armor resistance definitions
│   └── tags/
│       └── entity_type/
│           └── element_resistance/  # Mob resistance tags
│               ├── fire/
│               │   ├── half_resist.json
│               │   └── weakness.json
│               ├── ice/
│               └── ...
```

## Elemental Weapons Datapack

### File Format

Location: `data/{modid}/elemental_weapons/{weapon_name}.json`

### Basic Example

```json
{
  "item": "minecraft:diamond_sword",
  "element": "FIRE",
  "accumulation_multiplier": 1.0,
  "crit_chance": 0.0,
  "crit_damage": 0.0
}
```

### Multi-Stage Weapon

Weapons can have multiple stages that change the element after certain attack counts:

```json
{
  "item": "mymod:ultimate_staff",
  "element": "FIRE",
  "accumulation_multiplier": 1.5,
  "crit_chance": 0.1,
  "crit_damage": 1.5,
  "stages": [
    {
      "attack_count": 1,
      "element": "FIRE",
      "accumulation_multiplier": 1.0
    },
    {
      "attack_count": 3,
      "element": "ELECTRIC",
      "accumulation_multiplier": 1.2
    },
    {
      "attack_count": 6,
      "element": "WATER",
      "accumulation_multiplier": 0.8
    },
    {
      "attack_count": 10,
      "element": "ENERGY",
      "accumulation_multiplier": 2.0
    }
  ]
}
```

### Full Field Reference

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `item` | string | Yes | — | Item resource location (e.g., `"minecraft:diamond_sword"`) |
| `element` | string | Yes | — | Primary element type |
| `accumulation_multiplier` | float | No | 1.0 | Multiplier for accumulation per hit |
| `crit_chance` | float | No | 0.0 | Bonus critical hit chance (0.0–1.0) |
| `crit_damage` | float | No | 0.0 | Bonus critical hit damage multiplier |
| `stages` | array | No | — | Array of multi-stage configurations (max 4 stages) |

### Stage Field Reference

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `attack_count` | int | Yes | — | Number of attacks to reach this stage |
| `element` | string | Yes | — | Element for this stage |
| `accumulation_multiplier` | float | No | 1.0 | Accumulation multiplier for this stage |

### Example: Fire Staff with Progression

```json
{
  "item": "mymod:flame_staff",
  "element": "FIRE",
  "accumulation_multiplier": 1.0,
  "crit_chance": 0.05,
  "crit_damage": 0.2,
  "stages": [
    {
      "attack_count": 1,
      "element": "FIRE",
      "accumulation_multiplier": 1.0
    },
    {
      "attack_count": 5,
      "element": "FIRE",
      "accumulation_multiplier": 1.5
    },
    {
      "attack_count": 10,
      "element": "ENERGY",
      "accumulation_multiplier": 2.0
    }
  ]
}
```

## Armor Resistances Datapack

### File Format

Location: `data/{modid}/armor_resistances/{armor_name}.json`

### Basic Example

```json
{
  "item": "minecraft:diamond_chestplate",
  "resistances": {
    "FIRE": 0.2,
    "ICE": 0.1,
    "ELECTRIC": 0.15
  }
}
```

### Full Field Reference

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `item` | string | Yes | Armor item resource location |
| `resistances` | object | Yes | Map of element → resistance value |

### Resistance Value

| Value | Effect |
|-------|--------|
| `0.0` | No resistance |
| `0.25` | 25% damage reduction |
| `0.5` | 50% damage reduction |
| `0.75` | 75% damage reduction |
| `1.0` | Complete immunity |

### Example: Dragon Armor Set

**dragon_chestplate.json:**
```json
{
  "item": "mymod:dragon_chestplate",
  "resistances": {
    "FIRE": 0.8,
    "ENERGY": 0.4,
    "PHYSICAL": 0.2
  }
}
```

**dragon_helmet.json:**
```json
{
  "item": "mymod:dragon_helmet",
  "resistances": {
    "WIND": 0.3,
    "ELECTRIC": 0.3,
    "SHADOW": 0.2
  }
}
```

## Entity Type Tags

### Directory Structure

```
data/abloom/tags/entity_type/element_resistance/
├── fire/
│   ├── half_resist.json
│   └── full_resist.json
├── ice/
│   ├── weakness.json
│   └── half_resist.json
└── shadow/
    └── weakness.json
```

### Tag Types

| Tag Suffix | Resistance Value | Effect |
|------------|------------------|--------|
| `half_resist` | 0.5 | 50% damage reduction |
| `full_resist` | 1.0 | Complete immunity |
| `weakness` | -0.5 | 50% damage increase |

### Example: Blaze Fire Resistance

`data/abloom/tags/entity_type/element_resistance/fire/half_resist.json`:
```json
{
  "values": [
    "minecraft:blaze",
    "minecraft:magma_cube"
  ]
}
```

### Example: Skeleton Ice Weakness

`data/abloom/tags/entity_type/element_resistance/ice/weakness.json`:
```json
{
  "values": [
    "minecraft:skeleton",
    "minecraft:stray",
    "minecraft:warden"
  ]
}
```

### Example: Custom Mob Tag

Create a custom resistance in your own datapack:

`data/mymod/tags/entity_type/element_resistance/wind/half_resist.json`:
```json
{
  "values": [
    "minecraft:phantom",
    "minecraft:snow_golem"
  ],
  "replace": false
}
```

## Loading Order

Abloom-API loads datapacks in the following order:

1. **Entity type tags** — Loaded first (determines base mob resistances)
2. **Armor resistances** — Loaded second (configures armor item resistances)
3. **Elemental weapons** — Loaded last (registers weapon configurations)

This ensures proper fallback behavior when multiple datapacks define the same item.

## Best Practices

### 1. Use Unique Names

Each datapack file should have a unique name to avoid conflicts:

```
✅ data/mymod/elemental_weapons/flame_sword.json
❌ data/mymod/elemental_weapons/sword.json  (too generic)
```

### 2. Validate Your JSON

Always validate JSON syntax before loading:

```bash
# Use a JSON validator or linter
jq . data/mymod/elemental_weapons/my_weapon.json
```

### 3. Test with Vanilla Items

When adding resistances to vanilla items, use `minecraft:` prefix:

```json
{
  "item": "minecraft:netherite_chestplate",
  "resistances": {
    "FIRE": 0.5
  }
}
```

### 4. Stack Resistances

Armor resistances from different sources stack additively. Wear full sets for maximum protection:

```json
// Helmet
{ "item": "mymod:crystal_helmet", "resistances": { "ENERGY": 0.2 } }

// Chestplate
{ "item": "mymod:crystal_chestplate", "resistances": { "ENERGY": 0.2 } }

// Total: 40% energy resistance when wearing full set
```

## Troubleshooting

### Weapons Not Working

1. Check JSON syntax
2. Verify item path is correct (`namespace:item_name`)
3. Check console for load errors

### Resistances Not Applied

1. Ensure entity tags use `data/abloom/tags/` path
2. Check element names match exactly (uppercase)
3. Verify resistance values are between 0.0 and 1.0

### Multistage Not Changing

1. Verify attack count sequence is increasing
2. Max 4 stages supported
3. Check that the weapon is registered with Abloom
