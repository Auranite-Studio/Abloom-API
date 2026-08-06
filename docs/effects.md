# Effects Reference

Complete reference for all threshold effects in Abloom-API.

## Quick Reference Table

| Effect | Element | Duration | Icon | Color |
|--------|---------|----------|------|-------|
| **Rupture** | PHYSICAL | 6s | 💥 | `#C0C0C0` |
| **Burn** | FIRE | 10s | 🔥 | `#FF5500` |
| **Windswept** | WIND | 8s | 💨 | `#00FFFF` |
| **Wetness** | WATER | 12s | 💧 | `#0080FF` |
| **Stun** | EARTH | 5s | ⚫ | `#8B4513` |
| **Freeze** | ICE | 12s | ❄️ | `#00BFFF` |
| **Shock** | ELECTRIC | 10s | ⚡ | `#FF19FF` |
| **Overload** | ENERGY | 10s | ✨ | `#FFFF00` |
| **Bloom** | NATURAL | 8s | 🌿 | `#32CD32` |
| **Break** | QUANTUM | 6s | 🔮 | `#9400D3` |
| **Corruption** | ETHER | 8s | 👻 | `#24B3A7` |
| **Dispersion** | LIGHT | 10s | ☀️ | `#FFFFE0` |
| **Eclipse** | SHADOW | 10s | 🌑 | `#4B0082` |
| **Taunt** | — | Variable | 📢 | `#FF69B4` |
| **Critical Gain** | — | Variable | 🎯 | `#FFD700` |

---

## Physical: Rupture

```java
// Triggered when PHYSICAL accumulation reaches 100
PhysicalEffect.applyRupture(target, amplifier);
```

| Property | Value |
|----------|-------|
| **Duration** | 120 ticks (6 seconds) |
| **Damage Multiplier** | 2× (double damage) |
| **Armor Reduction** | 30% |

### Description

Rupture causes deep tissue damage, making the target extraordinarily vulnerable to all physical damage. Armor is significantly compromised.

### Visual Feedback

- Status text: `"RUPTURE!"`
- Color: Silver/Gray

### Interactions

- Stacks additively with other armor-reducing effects
- Works with the BREAK effect for extreme armor bypass

---

## Fire: Burn

```java
// Triggered when FIRE accumulation reaches 100
BurnEffect.applyBurn(target, amplifier);
```

| Property | Value |
|----------|-------|
| **Duration** | 200 ticks (10 seconds) |
| **Fire Duration** | Set on fire |
| **Extra Damage** | In water/rain/freezing conditions |

### Description

The target is set ablaze, taking continuous fire damage. In water, rain, or freezing conditions, the burn effect is amplified with additional damage.

### Visual Feedback

- Status text: `"BURN!"`
- Color: Orange
- Entity catches fire visually

### Interactions

- Additional damage when target is in water or rain
- Extended duration in freezing conditions
- Can extinguish in water (but deals bonus damage first)

---

## Wind: Windswept

```java
// Triggered when WIND accumulation reaches 100
WindsweptEffect.applyWindswept(target, amplifier);
```

| Property | Value |
|----------|-------|
| **Duration** | 160 ticks (8 seconds) |
| **Trigger** | Any non-wind damage |

### Description

The target is caught in a maelstrom of wind. **Any non-wind elemental damage** taken during this effect will trigger wind resonance (immediate accumulation).

### Visual Feedback

- Status text: `"WINDSWEPT!"`
- Color: Cyan

### Interactions

- Each non-wind hit triggers immediate wind resonance
- Wind damage does NOT trigger resonance
- Can cause rapid accumulation chain reactions

---

## Water: Wetness

```java
// Triggered when WATER accumulation reaches 100
WetnessEffect.applyWetness(target, amplifier);
```

| Property | Value |
|----------|-------|
| **Duration** | 240 ticks (12 seconds) |
| **Damage Multiplier** | 1.5× (50% increase) |
| **Accumulation Multiplier** | 2× (100% increase) |

### Description

The target is thoroughly soaked. All elemental damage taken is increased by 50%, and accumulation gains are doubled.

### Visual Feedback

- Status text: `"WETNESS!"`
- Color: Blue
- Target appears drenched

### Interactions

- Stacks multiplicatively with other damage multipliers
- Extremely dangerous when combined with high-damage elements
- Synergizes with Burn (fire on wet target)

---

## Earth: Stun

```java
// Triggered when EARTH accumulation reaches 100
StunEffect.applyStun(target, amplifier);
```

| Property | Value |
|----------|-------|
| **Duration** | 100 ticks (5 seconds) |
| **Movement Speed** | Severely reduced |
| **Attack Damage** | Reduced |
| **Attack Speed** | Reduced |
| **Jump Strength** | Reduced |
| **Armor** | 20% reduction |

### Description

The target is stunned by a seismic impact, immobilizing them and reducing their combat effectiveness across all stats.

### Visual Feedback

- Status text: `"STUNNED!"`
- Color: Brown/Earth tone

### Interactions

- Movement speed reduction is substantial
- All combat stats are penalized
- Armor reduction stacks with other sources

---

## Ice: Freeze

```java
// Triggered when ICE accumulation reaches 100
FreezeEffect.applyFreeze(target, amplifier);
```

| Property | Value |
|----------|-------|
| **Duration** | 240 ticks (12 seconds) |
| **Full Freeze Damage** | Significant ice damage |
| **Fire Interaction** | Breaks freeze instantly with explosion |

### Description

The target is encased in ice. As freeze ticks accumulate, additional ice damage is dealt. When fully frozen, massive damage is dealt. Fire breaks the freeze instantly with explosive force.

### Visual Feedback

- Status text: `"FROZEN!"`
- Color: Light Blue
- Target appears encased in ice

### Interactions

- Fire breaks freeze instantly (but takes the freeze damage first)
- Cumulative damage as freeze progresses
- Can be used as a crowd control mechanic

---

## Electric: Shock

```java
// Triggered when ELECTRIC accumulation reaches 100
ShockEffect.applyShock(target, amplifier);
```

| Property | Value |
|----------|-------|
| **Duration** | 200 ticks (10 seconds) |
| **Damage Output** | 20% reduction |

### Description

Electrical disruption paralyzes the target's muscles, reducing their damage output by 20%.

### Visual Feedback

- Status text: `"SHOCKED!"`
- Color: Magenta/Electric pink

### Interactions

- Reduces all damage dealt by the target
- Does not affect damage taken
- Effective against high-damage bosses

---

## Energy: Overload

```java
// Triggered when ENERGY accumulation reaches 100
OverloadEffect.applyOverload(target, amplifier);
```

| Property | Value |
|----------|-------|
| **Duration** | 200 ticks (10 seconds) |
| **Damage Taken** | 20% increase |

### Description

The target's energy core is destabilized, making them 20% more vulnerable to all incoming damage.

### Visual Feedback

- Status text: `"OVERLOAD!"`
- Color: Yellow
- Target may glow with unstable energy

### Interactions

- Stacks additively with other damage-increasing effects
- Excellent prep for burst damage rotations
- Synergizes with Wetness (combined 35% damage increase)

---

## Natural: Bloom

```java
// Triggered when NATURAL accumulation reaches 100
BloomEffect.applyBloom(target, amplifier);
```

| Property | Value |
|----------|-------|
| **Duration** | 160 ticks (8 seconds) |
| **Tick Damage** | 1 + 0.5 × amplifier per second |
| **Universal Vulnerability** | 20% increase |

### Description

Nature's bounty becomes a curse. The target takes continuous Natural damage and becomes 20% more vulnerable to all elemental damage.

### Visual Feedback

- Status text: `"BLOOM!"`
- Color: Green
- Organic growth effects on target

### Interactions

- Continuous tick damage scales with amplifier
- Universal vulnerability stacks with other sources
- Persistent damage over time

---

## Quantum: Break

```java
// Triggered when QUANTUM accumulation reaches 100
BreakEffect.applyBreak(target, amplifier);
```

| Property | Value |
|----------|-------|
| **Duration** | 120 ticks (6 seconds) |
| **Armor Value** | Set to 0 |

### Description

Quantum disruption causes armor to相位偏移 (phase shift), completely negating all armor protection.

### Visual Feedback

- Status text: `"BREAK!"`
- Color: Purple/Quantum

### Interactions

- Completely bypasses armor (same as Rupture but stronger)
- Stacks with Rupture for even more vulnerability
- Extremely effective against heavily armored targets

---

## Ether: Corruption

```java
// Triggered when ETHER accumulation reaches 100
CorruptionEffect.applyCorruption(target, amplifier);
```

| Property | Value |
|----------|-------|
| **Duration** | 160 ticks (8 seconds) |
| **Tick Damage** | 1 + 0.5 × amplifier per second |
| **Resistance Reduction** | 20% |

### Description

Ethereal corruption seeps into the target, dealing continuous damage and weakening their natural resistances.

### Visual Feedback

- Status text: `"CORRUPTED!"`
- Color: Teal/Ethereal green
- Ghostly effects around target

### Interactions

- Tick damage scales with amplifier
- Reduces all elemental resistances
- Stacks with other resistance-reducing effects

---

## Light: Dispersion

```java
// Triggered when LIGHT accumulation reaches 100
DispersionEffect.applyDispersion(target, amplifier);
```

| Property | Value |
|----------|-------|
| **Duration** | 200 ticks (10 seconds) |
| **Damage Taken** | Increased |

### Description

Intense light scatters and amplifies incoming damage, making the target more vulnerable.

### Visual Feedback

- Status text: `"DISPERSION!"`
- Color: Bright white/yellow

### Interactions

- Increases all damage taken
- Synergizes with Overload (both increase damage taken)
- Duration is moderate but impactful

---

## Shadow: Eclipse

```java
// Triggered when SHADOW accumulation reaches 100
EclipseEffect.applyEclipse(target, amplifier);
```

| Property | Value |
|----------|-------|
| **Duration** | 200 ticks (10 seconds) |
| **Defense Reduction** | 10% per negative effect |
| **Damage Reduction** | 10% per negative effect |

### Description

Darkness engulfs the target. Each negative effect on the target reduces their defense and damage output by 10%.

### Visual Feedback

- Status text: `"ECLIPSE!"`
- Color: Dark purple/black

### Interactions

- Scales with number of debuffs (more effects = stronger)
- Synergizes with multi-debuff strategies
- Can reduce defense/damage by up to 50% with 5 debuffs

---

## Taunt (Standalone)

```java
// Applied independently, not a threshold effect
TauntEffect.applyTaunt(target, amplifier);
```

| Property | Value |
|----------|-------|
| **Duration** | Variable |
| **Effect** | Attracts nearby hostile mobs |

### Description

The target emits a taunting signal that attracts nearby hostile mobs, causing them to attack the target instead.

### Visual Feedback

- Status text: `"TAUNT!"`
- Color: Pink

### Interactions

- Effective for crowd control and drawing aggro
- Useful for protecting allies
- Mobs within range will prioritize taunted target

---

## Critical Gain (Standalone)

```java
// Applied independently, not a threshold effect
CriticalGainEffect.applyCriticalGain(target, amplifier);
```

| Property | Value |
|----------|-------|
| **Duration** | Variable |
| **Crit Damage** | +30% |
| **Crit Chance** | +15% |
| **Attack Speed** | +5% |

### Description

The target gains enhanced critical strike capabilities, boosting all combat stats related to critical hits.

### Visual Feedback

- Status text: `"CRITICAL GAIN!"`
- Color: Gold

### Interactions

- Applies to the entity it's given to (usually an ally)
- Stacks with other crit-boosting effects
- Synergizes with high-damage weapons

---

## Effect Synergies

### Optimal Damage Chains

1. **Wetness + Burn** — Water accumulation → Wetness → Fire hits (1.5× damage + fire)
2. **Overload + Quantum Break** — Energy accumulation → Overload → Quantum (20% + full armor bypass)
3. **Windswept Chain** — Wind accumulation → Windswept → Non-wind hits trigger resonance → rapid stacking

### Defensive Counters

1. **Anti-Stun** — Remove Earth damage sources, use healing effects
2. **Anti- Freeze** — Fire damage to break freeze, avoid Ice weapons
3. **Anti-Break** — Stack armor resistances, use damage reduction effects
