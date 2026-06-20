# Abloom API Mod Documentation

## Overview

**Abloom API** is a library mod for NeoForge (1.21.1) that adds an elemental damage system, elemental energy accumulation, and threshold effects. The mod provides infrastructure for creating weapons with elemental properties, projectiles, armor with elemental resistances, and a resistance system for mobs.

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