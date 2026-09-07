## Overview

**Abloom API** is a library mod for NeoForge that adds an elemental damage system, elemental energy accumulation, and resonance effects. The mod provides infrastructure for creating weapons with elemental properties, projectiles, and a resistance system for mobs.

---

## Key Features

### Element System

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
| **PRISMATIC** | `prismatic_dmg` | *Rainbow (dynamic)* |

### Mob Resistances and Weaknesses

The following table shows which mobs have immunities, resistances, or weaknesses to each element type:

| Element      | Resistant                                                                                                                                             | Weak                                                                                                                                   |
|--------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| **FIRE**     | Blaze, Magma Cube, Wither, Ender Dragon, Strider, Zombified Piglin, Wither Skeleton, Ghast, Warden, Hoglin, Piglin, Piglin Brute, Zoglin, Husk, Camel | Snow Golem, Dolphin, Zombie, Zombie Villager, Drowned, Stray, Bogged                                                                   |
| **PHYSICAL** | Turtle, Armadillo, Iron Golem, Shulker, Warden, Ender Dragon                                                                                          | Slime, Magma Cube, Phantom, Vex, Allay, Glow Squid, Squid                                                                              |
| **WIND**     | Phantom, Breeze, Ender Dragon, Ghast, Vex, Allay, Parrot, Chicken, Ocelot, Cat, Fox, Wolf                                                             | Turtle, Sniffer, Armadillo, Camel, Ravager, Hoglin, Polar Bear                                                                         |
| **EARTH**    | Endermite, Silverfish, Shulker, Iron Golem, Warden, Giant, Ravager, Armadillo, Sniffer                                                                | Ghast, Phantom, Vex, Allay, Breeze                                                                                                     |
| **WATER**    | Squid, Glow Squid, Drowned, Guardian, Elder Guardian, Axolotl, Tadpole, Frog, Turtle, Cod, Salmon, Pufferfish, Tropical Fish, Dolphin, Witch          | Blaze, Snow Golem, Strider, Breeze                                                                                                     |
| **ICE**      | Snow Golem, Stray, Polar Bear, Goat                                                                                                                   | Blaze, Magma Cube, Strider, Breeze                                                                                                     |
| **ELECTRIC** | Creeper, Enderman, Phantom, Allay, Breeze                                                                                                             | Drowned, Turtle, Axolotl, Frog, Tadpole, Cod, Salmon, Pufferfish, Tropical Fish, Dolphin, Squid, Glow Squid, Guardian, Elder Guardian  |
| **ENERGY**   | Enderman, Shulker, Warden, Ender Dragon, Wither, Elder Guardian, Evoker, Witch                                                                        | Creeper, Ghast                                                                                                                         |
| **NATURAL**  | Bogged, Wither Skeleton, Wither, Slime, Magma Cube, Bee, Wolf, Ocelot, Cat, Panda, Fox, Rabbit                                                        | Villager, Wandering Trader, Iron Golem, Snow Golem, Allay, Zoglin, Stray, Zombified Piglin, Zombie, Zombie Villager, Skeleton, Axolotl |
| **QUANTUM**  | Enderman, Endermite, Ender Dragon, Shulker, Wither, Warden                                                                                            | Villager, Wandering Trader, Bat, Allay                                                                                                 |
| **ETHER**    | Ender Dragon, Wither                                                                                                                                  | Enderman, Endermite, Shulker, Warden                                                                                                   |
| **LIGHT**    | Warden, Wither                                                                                                                                        | Zombified Piglin, Zombie, Zombie Villager, Skeleton, Zoglin                                                                                  |
| **SHADOW**   | Warden, Wither                                                                                                                                        | *None*                                                                                                                                 |
| **PRISMATIC** | **DO NOT SUPPORT**                                                                                                                                    | **DO NOT SUPPORT**                                                                                                                                 |

---