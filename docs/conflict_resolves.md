\# Damage Priority Conflict Resolution



\## Problem

Abloom API had issues with damage modification conflicts when multiple mods simultaneously modified damage in the `LivingDamageEvent.Pre` event:

1\. Mods modified damage BEFORE Abloom processed it

2\. Mods modified damage AFTER Abloom processed it

3\. Multiple mods tried to modify the same damage event

4\. Conflicts with equal priorities (order was not guaranteed)

5\. Mods without explicit priority specification (unknown order)



\## Solution



\### 1. DamageModificationManager System

New system for registering damage modifiers with priorities:

\- Modifiers are sorted by priority (highest to lowest)

\- Modifiers with equal priorities are called in registration order (FIFO)

\- Guaranteed stable and predictable call order



\### 2. Conflict Prevention Mechanism



\*\*Priorities\*\*

\- Modifiers are sorted by priority (highest to lowest)

\- NeoForge standard values: HIGHEST(300), HIGH(200), NORMAL(0), LOW(-100), LOWEST(-300)

\- Abloom API uses priority LOW (-100) for damage processing



\*\*Registration Order\*\*

\- Modifiers with equal priorities are called in registration order (FIFO)

\- Earlier registration = earlier call

\- This solves conflicts when multiple mods use the same priorities



\*\*High/Low Priority Separation\*\*

\- `processHighPriorityDamage()`: calls modifiers with priority > 0 (BEFORE Abloom)

\- `processLowPriorityDamage()`: calls modifiers with priority <= 0 (AFTER Abloom)

\- This guarantees Abloom always processes damage at the correct moment



\*\*Default Handling\*\*

\- If other mods don't specify priority explicitly, they get value 0 (NORMAL)

\- Abloom API works with priority LOW (-100)

\- Modifiers with priority > 0 (HIGH, HIGHEST) are called BEFORE Abloom

\- Modifiers with priority <= 0 (NORMAL, LOW, LOWEST) are called AFTER Abloom



\### 3. System Guarantees



✅ \*\*Conflicts with equal priorities\*\*: Solved via FIFO order (registration order)

✅ \*\*Missing priorities from other mods\*\*: Solved via default value 0 (NORMAL)

✅ \*\*Global call order\*\*: High priority (>0) first, then Abloom (-100), then low priority (<=0)

✅ \*\*Sequence within priority\*\*: In registration order (FIFO)

✅ \*\*Thread safety\*\*: `CopyOnWriteArrayList`



\## Behavior with Equal Priorities



When multiple mods register modifiers with \*\*equal priorities\*\*, they will be called in \*\*registration order (FIFO)\*\*:



```

Example:

Mod A.registerModifier(modA, 200)  // Registered first

Mod B.registerModifier(modB, 200)  // Registered second

Mod C.registerModifier(modC, 200)  // Registered third



Call order during damage:

1\. modA (priority=200, registered first)

2\. modB (priority=200, registered second)

3\. modC (priority=200, registered third)

```



This means:

\- Earlier registration = earlier call

\- Order is guaranteed and stable

\- Mods can control their relative order through registration order



\## Working Example



```java

// Mod A registers modifier with priority 200 (HIGH)

DamageModificationManager.registerModifier(modA, 200);



// Mod B registers modifier with priority 200 (HIGH)

DamageModificationManager.registerModifier(modB, 200);



// Abloom API registers modifier with priority -100 (LOW)

DamageModificationManager.registerModifier(abloom, -100);



// Mod C registers modifier with priority 0 (NORMAL)

DamageModificationManager.registerModifier(modC, 0);



// Call order during damage:

// === High Priority (priority > 0, BEFORE Abloom) ===

// 1. modA (priority=200)

// 2. modB (priority=200)

// === Abloom (priority = -100, LOW) ===

// 3. baseDamage processed by ElementDamageHandler

// === Low Priority (priority <= 0, AFTER Abloom) ===

// 4. modC (priority=0)

// (modifiers with priority < -100, e.g., LOWEST(-300))

```



\## API Methods



```java

// Register modifier with priority

public static void registerModifier(DamageModifier modifier, int priority)



// Register modifier with default priority (0)

public static void registerModifier(DamageModifier modifier)



// Process damage through high priority modifiers (> 0)

public static float processHighPriorityDamage(LivingEntity target, DamageSource source, float baseDamage)



// Process damage through low priority modifiers (<= 0)

public static float processLowPriorityDamage(LivingEntity target, DamageSource source, float currentDamage)



// Get number of registered modifiers

public static int getRegisteredModifierCount()



// Clear all modifiers

public static void clearAllModifiers()

```



\## Integration



\### Option 1: Explicit Priority

```java

DamageModificationManager.registerModifier(myModifier, 200); // HIGH - called BEFORE Abloom

DamageModificationManager.registerModifier(myModifier, -100); // LOW - called AFTER Abloom

```



\### Option 2: Default Priority

```java

DamageModificationManager.registerModifier(myModifier); // DEFAULT (0) - called AFTER Abloom

```



\## Priority Table



| Priority | Value | Description | When to use |

|----------|-------|-------------|-------------|

| HIGHEST | 300 | Highest | For critically important modifications |

| HIGH | 200 | High | When processing BEFORE Abloom |

| NORMAL | 0 | Default | For simple modifiers |

| LOW | -100 | Low (Abloom) | When processing AFTER most mods |

| LOWEST | -300 | Very low | When processing last |



\## Best Practices



1\. \*\*Use HIGH (200)\*\* when your mod needs to process damage BEFORE Abloom API

2\. \*\*Use NORMAL (0)\*\* for simple modifiers (they will be AFTER Abloom)

3\. \*\*Document priorities\*\* in your mod

4\. \*\*Avoid too high priorities\*\* (>300) to prevent conflicts with other mods

5\. \*\*Test with other mods\*\* that modify damage



\## Debugging



```java

// Check number of registered modifiers

int count = DamageModificationManager.getRegisteredModifierCount();

AbloomMod.LOGGER.info("Registered: {} modifiers", count);



// Clear (debug only!)

DamageModificationManager.clearAllModifiers();

```



\## Files

1\. `DamageModificationManager.java` - Modifier registration system

2\. `ElementDamageHandler.java` - Integration with DamageModificationManager

3\. `DAMAGE\_PRIORITY\_SYSTEM.md` - Detailed priority system documentation



