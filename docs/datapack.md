\# Abloom API Datapack Guide



\## Overview



This guide explains how to create datapacks for the Abloom API mod. Datapacks allow you to configure elemental weapons, armor resistances, and mob elemental properties without modifying code.



\## Directory Structure



```

your\_datapack.zip

└── data

&#x20;   └── abloom

&#x20;       ├── damage\_type/           # Custom damage types (optional)

&#x20;       ├── elemental\_weapons/     # Elemental weapon configurations

&#x20;       ├── armor\_resistances/     # Armor resistance configurations

&#x20;       └── tags

&#x20;           └── entity\_type

&#x20;               └── element\_resistance/  # Mob elemental properties

```



\---



\## Elemental Weapons Configuration



Elemental weapons are defined in `data/abloom/elemental\_weapons/`.



\### File Format



Each weapon configuration is a JSON file with the following structure:



```json

{

&#x20; "item": "modid:item\_name",

&#x20; "element": "ELEMENT\_NAME",

&#x20; "accumulation\_multiplier": 1.0

}

```



\### Parameters



| Parameter | Type | Required | Description |

|-----------|------|----------|-------------|

| `item` | string | Yes | Registry name of the item (e.g., `minecraft:iron\_sword`) |

| `element` | string | Yes | One of the 13 element types (see below) |

| `accumulation\_multiplier` | float | No | Multiplier for accumulation points (default: 1.0) |



\### Element Types



Available elements:



\- `FIRE` - Fire damage

\- `PHYSICAL` - Physical damage

\- `WIND` - Wind damage

\- `EARTH` - Earth damage

\- `WATER` - Water damage

\- `ICE` - Ice damage

\- `ELECTRIC` - Electric damage

\- `ENERGY` - Energy damage

\- `NATURAL` - Natural damage

\- `QUANTUM` - Quantum damage

\- `ETHER` - Ether damage

\- `LIGHT` - Light damage

\- `SHADOW` - Shadow damage



\### Example Files



\#### `data/abloom/elemental\_weapons/iron\_sword\_fire.json`

```json

{

&#x20; "item": "minecraft:iron\_sword",

&#x20; "element": "FIRE",

&#x20; "accumulation\_multiplier": 3.0

}

```



This makes iron swords deal fire damage and accumulate resonance 3x faster.



\#### `data/abloom/elemental\_weapons/diamond\_axe\_wind.json`

```json

{

&#x20; "item": "minecraft:diamond\_axe",

&#x20; "element": "WIND",

&#x20; "accumulation\_multiplier": 5.0

}

```



This makes diamond axes deal wind damage and accumulate resonance 5x faster.



\---



\## Armor Resistances Configuration



Armor resistances are defined in `data/abloom/armor\_resistances/`.



\### File Format



```json

{

&#x20; "item": "modid:armor\_name",

&#x20; "resistances": {

&#x20;   "ELEMENT\_NAME": 0.0,

&#x20;   "ANOTHER\_ELEMENT": 0.1

&#x20; }

}

```



\### Parameters



| Parameter | Type | Required | Description |

|-----------|------|----------|-------------|

| `item` | string | Yes | Registry name of the armor piece |

| `resistances` | object | Yes | Map of element → resistance value |



\### Resistance Values



\- `0.0` - No resistance (default)

\- `0.5` - 50% resistance (half damage)

\- `-0.5` - 50% weakness (double damage)

\- Range: `-0.99` to `0.99`



\### Example Files



\#### `data/abloom/armor\_resistances/leather\_helmet.json`

```json

{

&#x20; "item": "minecraft:leather\_helmet",

&#x20; "resistances": {

&#x20;   "ELECTRIC": 0.02,

&#x20;   "ICE": 0.01

&#x20; }

}

```



This gives leather helmets slight resistance to electric and ice damage.



\#### `data/abloom/armor\_resistances/chainmail\_chestplate.json`

```json

{

&#x20; "item": "minecraft:chainmail\_chestplate",

&#x20; "resistances": {

&#x20;   "WIND": 0.04

&#x20; }

}

```



This gives chainmail chestplates resistance to wind damage.



\---



\## Mob Elemental Properties



Mob elemental properties are defined using entity type tags in `data/abloom/tags/entity\_type/element\_resistance/`.



\### Directory Structure



```

data/abloom/tags/entity\_type/element\_resistance/

├── fire/

│   ├── resistance.json    # Mobs resistant to fire

│   └── weakness.json      # Mobs weak to fire

├── water/

│   ├── resistance.json

│   └── weakness.json

└── \[other elements]/

&#x20;   ├── resistance.json

&#x20;   └── weakness.json

```



\### File Format



```json

{

&#x20; "replace": false,

&#x20; "values": \[

&#x20;   "minecraft:entity\_type\_1",

&#x20;   "minecraft:entity\_type\_2"

&#x20; ]

}

```



\### Parameters



| Parameter | Type | Required | Description |

|-----------|------|----------|-------------|

| `replace` | boolean | No | Whether to replace existing values (default: false) |

| `values` | array | Yes | Array of entity type registry names |



\### Example Files



\#### `data/abloom/tags/entity\_type/element\_resistance/fire/resistance.json`

```json

{

&#x20; "replace": false,

&#x20; "values": \[

&#x20;   "minecraft:blaze",

&#x20;   "minecraft:magma\_cube",

&#x20;   "minecraft:wither",

&#x20;   "minecraft:ender\_dragon",

&#x20;   "minecraft:strider",

&#x20;   "minecraft:zombified\_piglin",

&#x20;   "minecraft:wither\_skeleton",

&#x20;   "minecraft:ghast",

&#x20;   "minecraft:warden",

&#x20;   "minecraft:hoglin",

&#x20;   "minecraft:piglin",

&#x20;   "minecraft:piglin\_brute",

&#x20;   "minecraft:zoglin",

&#x20;   "minecraft:husk",

&#x20;   "minecraft:camel"

&#x20; ]

}

```



This makes all listed mobs resistant to fire damage (50% reduction).



\#### `data/abloom/tags/entity\_type/element\_resistance/fire/weakness.json`

```json

{

&#x20; "replace": false,

&#x20; "values": \[

&#x20;   "minecraft:snow\_golem",

&#x20;   "minecraft:dolphin",

&#x20;   "minecraft:zombie",

&#x20;   "minecraft:zombie\_villager",

&#x20;   "minecraft:drowned",

&#x20;   "minecraft:stray",

&#x20;   "minecraft:bogged"

&#x20; ]

}

```



This makes all listed mobs weak to fire damage (50% increase).



\---



\## Loading Datapacks



1\. Place your datapack in `.minecraft/saves/\[worldname]/datapacks/`

2\. Reload with `/reload` command

3\. Check logs for confirmation messages:

&#x20;  - `Loaded X elemental weapon configurations from datapacks`

&#x20;  - `Loaded X armor resistance configurations from datapacks`



\---



\## Common Issues



\### Weapon not dealing elemental damage



\- Check that the `item` registry name is correct

\- Verify the `element` value is one of the 13 supported types

\- Ensure the datapack is loaded (check `/reload` logs)



\### Armor not providing resistance



\- Verify the `item` registry name matches the exact armor piece

\- Check that `resistances` values are within `-0.99` to `0.99`

\- Ensure resistance values are positive for resistance, negative for weakness



\### Mobs not having elemental properties



\- Verify entity type registry names are correct

\- Check that tag files are in the correct subdirectory (`element\_resistance/\[element]/`)

\- Ensure `values` array contains valid entity type IDs



\---



\## Advanced Usage



\### Multiple Elements on One Item



You can create multiple datapack files for the same item with different elements:



```json

// fire\_sword.json

{

&#x20; "item": "minecraft:iron\_sword",

&#x20; "element": "FIRE",

&#x20; "accumulation\_multiplier": 3.0

}



// physical\_sword.json

{

&#x20; "item": "minecraft:iron\_sword",

&#x20; "element": "PHYSICAL",

&#x20; "accumulation\_multiplier": 5.0

}

```



\### Custom Mod Integration



To make items from your own mod elemental:



```json

{

&#x20; "item": "mymod:dragon\_sword",

&#x20; "element": "FIRE",

&#x20; "accumulation\_multiplier": 4.0

}

```



\### Negative Resistance (Weakness)



To make mobs take more damage from specific elements:



```json

{

&#x20; "item": "minecraft:iron\_chestplate",

&#x20; "resistances": {

&#x20;   "FIRE": -0.2

&#x20; }

}

```



This makes the chestplate take 20% more fire damage (weakness).



