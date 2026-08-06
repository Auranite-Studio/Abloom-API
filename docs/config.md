# Configuration

Abloom-API provides extensive configuration options to customize the elemental damage system.

## Configuration File

The config file is located at:

```
/configs/abloom-common.toml
```

This is a **common** config, meaning it syncs between server and client for consistent gameplay.

## Config Options

### Display Settings

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `showDamageNumbers` | boolean | `true` | Show floating damage numbers on hit |
| `showStatusText` | boolean | `true` | Show threshold effect status text |
| `spawnRadius` | int | `64` | Radius for damage display entity spawning (1–128) |

### Threshold Settings

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `accumulationThreshold` | int | `100` | Points needed to trigger threshold effect |
| `resetDelayTicks` | int | `300` | Ticks without damage before accumulator resets |
| `displayCooldownTicks` | int | `5` | Minimum ticks between damage displays per entity |
| `maxActiveDisplays` | int | `500` | Maximum simultaneous damage displays |

### Damage Settings

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `baseAccumulationPerHit` | int | `1` | Base accumulation points per hit |
| `wetnessAccumulationMultiplier` | float | `2.0` | Accumulation multiplier under Wetness effect |
| `resonanceTriggerOnOtherElement` | boolean | `true` | Whether non-wind damage triggers wind resonance |

### Visual Settings

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `damageNumberFontSize` | int | `16` | Font size for floating damage numbers |
| `statusTextFontSize` | int | `20` | Font size for threshold effect text |
| `displayLifetimeTicks` | int | `60` | How long displays remain visible (ticks) |
| `displayGravity` | boolean | `true` | Whether damage numbers follow gravity physics |

### Performance Settings

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enableDatapackLoading` | boolean | `true` | Enable loading elemental weapons from datapacks |
| `enableEntityTags` | boolean | `true` | Enable entity type tag loading |
| `asyncDatapackLoading` | boolean | `true` | Load datapacks asynchronously |

## Example Configuration

```toml
# Abloom-API Configuration
# File: configs/abloom-common.toml

[display]
    # Show floating damage numbers when entities take elemental damage
    showDamageNumbers = true
    
    # Show threshold effect status text (e.g., "BURN!", "STUNNED!")
    showStatusText = true
    
    # Radius for damage display entity spawning (1-128)
    # Higher values = displays can spawn further away
    spawnRadius = 64

[threshold]
    # Points needed to trigger a threshold effect
    accumulationThreshold = 100
    
    # Ticks without damage of a type before accumulator resets
    # 300 ticks = 15 seconds at 20 TPS
    resetDelayTicks = 300
    
    # Minimum ticks between damage displays on the same entity
    displayCooldownTicks = 5
    
    # Maximum simultaneous damage display entities
    maxActiveDisplays = 500

[damage]
    # Base accumulation points per hit
    baseAccumulationPerHit = 1
    
    # Multiplier for accumulation under Wetness effect
    wetnessAccumulationMultiplier = 2.0
    
    # Whether any non-wind damage triggers wind resonance
    resonanceTriggerOnOtherElement = true

[visual]
    # Font size for floating damage numbers
    damageNumberFontSize = 16
    
    # Font size for threshold effect status text
    statusTextFontSize = 20
    
    # How long displays remain visible (in ticks)
    displayLifetimeTicks = 60
    
    # Whether damage numbers follow gravity physics
    displayGravity = true

[performance]
    # Enable loading elemental weapons from datapacks
    enableDatapackLoading = true
    
    # Enable loading entity type resistance tags
    enableEntityTags = true
    
    # Load datapacks asynchronously for faster world loading
    asyncDatapackLoading = true
```

## Runtime Configuration

You can modify config values at runtime via the API:

```java
import com.auranite.abloom.config.AbloomConfig;

// Get the config instance
AbloomConfig config = AbloomConfig.getInstance();

// Change display settings
config.setShowDamageNumbers(false);
config.setShowStatusText(true);

// Adjust performance
config.setSpawnRadius(32);  // Reduce display radius

// Apply changes
config.save();
```

## Config Priority

Configuration is loaded in this order (later overrides earlier):

1. **Default values** — Hardcoded defaults in the code
2. **Config file** — Values from `abloom-common.toml`
3. **Datapack overrides** — Some values can be overridden via datapack fields

```
Defaults → Config File → Datapack Override → Runtime API
```

## Performance Tuning

### For Low-End Machines

```toml
[display]
    showDamageNumbers = false
    showStatusText = true
    spawnRadius = 32

[performance]
    maxActiveDisplays = 100
    asyncDatapackLoading = true
```

### For High-Performance Servers

```toml
[display]
    showDamageNumbers = true
    showStatusText = true
    spawnRadius = 128

[threshold]
    maxActiveDisplays = 1000

[performance]
    asyncDatapackLoading = true
```

### For Competitive Play

```toml
[display]
    showDamageNumbers = false
    showStatusText = false
    spawnRadius = 1

[threshold]
    accumulationThreshold = 100
    resetDelayTicks = 300
```

## Resetting to Defaults

To reset to default values:

1. Delete `configs/abloom-common.toml`
2. Restart the game/server
3. A new config file will be generated with defaults

## Notes

- Changes to the config file take effect on world reload or server restart
- The `showDamageNumbers` and `showStatusText` options can be toggled dynamically
- The `spawnRadius` only affects new displays, not existing ones
- `maxActiveDisplays` should be set based on expected concurrent combat scenarios
