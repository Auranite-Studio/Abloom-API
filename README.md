# Документация мода Abloom API

## Обзор

**Abloom API** — это библиотечный мод для NeoForge (версия 21.1.215+, Minecraft 1.21.x), который добавляет систему элементального урона, накопления элементальной энергии и пороговых эффектов. Мод предоставляет инфраструктуру для создания оружия со стихийными свойствами, снарядов и системы сопротивлений для мобов.

---

## Основные возможности

### 1. Система элементов

Мод определяет 10 типов элементов:

| Элемент | ID урона | Цвет урона |
|---------|----------|------------|
| FIRE (Огонь) | `fire_dmg` | #FF5500 |
| PHYSICAL (Физический) | `physical_dmg` | #FFAA00 |
| WIND (Ветер) | `wind_dmg` | #00FFFF |
| EARTH (Земля) | `earth_dmg` | #8B4513 |
| WATER (Вода) | `water_dmg` | #0080FF |
| ICE (Лёд) | `ice_dmg` | #00BFFF |
| ELECTRIC (Электричество) | `electric_dmg` | #FF19FF |
| SOURCE (Исходный) | `source_dmg` | #FF5C77 |
| NATURAL (Природный) | `natural_dmg` | #32CD32 |
| QUANTUM (Квантовый) | `quantum_dmg` | #9400D3 |

### 2. Механика накопления (Accumulation)

- При получении урона от элемента цель накапливает очки этого элемента
- Базовое значение накопления: **1 очко резонанса за удар**
- Порог активации: **100 очков резонанса**
- При достижении порога срабатывает специальный эффект, и очки резонанса сбрасываются
- Накопление сбрасывается через **300 тиков** (15 секунд) без получения урона этого типа

### 3. Пороговые эффекты

При достижении 100 очков накопления резонанса:

| Элемент | Эффект |
|---------|--------|
| **FIRE** | Поджигание цели (эффект Burning) |
| **PHYSICAL** | Критический урон (x5) |
| **WIND** | Левитация на 6 секунд |
| **WATER** | Наложение эффекта Wetness |
| **EARTH** | Оглушение (Stun) на 6 секунд |
| **ICE** | Заморозка (Freeze) на 16 секунд |
| **ELECTRIC** | Шок (Shock) на 10 секунд |
| **SOURCE** | Эффект Rift (усиление получаемого урона) |
| **NATURAL** | Эффект Break |
| **QUANTUM** | Телепортация в случайное место |

---

## API для разработчиков

### Регистрация элементального оружия

#### Способ 1: Через ElementalWeaponRegistry

```java
import com.auranite.abloom.ElementalWeaponRegistry;
import com.auranite.abloom.ElementType;
import net.minecraft.world.item.Item;

// Регистрация с количеством накопления по умолчанию (1.0 очка за удар)
ElementalWeaponRegistry.registerWeapon(myItem, ElementType.FIRE);

// Регистрация с кастомным количеством накопления
ElementalWeaponRegistry.registerWeapon(myItem, ElementType.ICE, 2.5f);
```

#### Способ 2: Через компонент данных (Data Component)

```java
import com.auranite.abloom.ElementalWeaponComponent;
import com.auranite.abloom.ElementType;
import net.minecraft.world.item.ItemStack;

// Создание элементального предмета
ItemStack stack = new ItemStack(myItem);
ElementalWeaponComponent.withElement(stack, ElementType.ELECTRIC);

// Создание с кастомным количеством накопления
ElementalWeaponComponent.withElementAndAccum(stack, ElementType.WATER, 1.5f);

// Получение элемента из предмета
Optional<ElementType> element = ElementalWeaponComponent.getElement(stack);

// Проверка наличия элемента
boolean hasElement = ElementalWeaponComponent.hasElement(stack);
```

### Регистрация элементальных снарядов

```java
import com.auranite.abloom.ElementalProjectileRegistry;
import com.auranite.abloom.ElementType;
import net.minecraft.world.entity.EntityType;

// Регистрация по типу сущности
ElementalProjectileRegistry.registerProjectile(
    EntityType.ARROW,
    ElementType.FIRE,
    1.5f  // количество очков накопления
);

// Регистрация по классу сущности
ElementalProjectileRegistry.registerProjectileByClass(
    MyCustomArrow.class,
    ElementType.ICE,
    2.0f  // количество очков накопления
);

// Включение наследования элемента от стрелка
ElementalProjectileRegistry.setInheritElementFromShooter(true);
```

#### Создание и запуск снаряда

```java
import com.auranite.abloom.ElementalProjectileRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;

// Создание снаряда с элементом от оружия стрелка
ElementalProjectileRegistry.createAndLaunchElementalProjectile(
    serverLevel,
    shooter,
    EntityType.ARROW,
    1.5f,  // скорость
    1.0f   // неточность
);

// Создание снаряда с принудительным элементом
ElementalProjectileRegistry.createElementalProjectileWithOverride(
    serverLevel,
    shooter,
    EntityType.SNOWBALL,
    ElementType.FIRE,  // принудительный элемент
    1.5f,
    1.0f
);
```

### Настройка сопротивлений элементов

#### Через теги (рекомендуется)

Создайте JSON-файлы тегов в `data/abloom/tags/entity_type/element/<element>/`:

**Пример: `data/abloom/tags/entity_type/element/fire/immune.json`**
```json
{
  "values": [
    "minecraft:blaze",
    "minecraft:magma_cube"
  ]
}
```

**Пример: `data/abloom/tags/entity_type/element/ice/weakness.json`**
```json
{
  "values": [
    "minecraft:stray",
    "minecraft:polar_bear"
  ]
}
```

Доступные модификаторы:
- `immune` — полный иммунитет (урон = 0, накопление = 0)
- `resistance` — сопротивление (урон × 0.5, накопление × 0.5)
- `weakness` — слабость (урон × 1.5, накопление × 1.5)

#### Программная регистрация

```java
import com.auranite.abloom.ElementResistanceRegistry;
import com.auranite.abloom.ElementType;
import net.minecraft.world.entity.EntityType;

// Регистрация иммунитета для нескольких типов сущностей
ElementResistanceRegistry.registerUniform(
    ElementType.FIRE,
    0.0f,  // сопротивление накоплению
    0.0f,  // сопротивление урону
    EntityType.BLAZE,
    EntityType.MAGMA_CUBE
);

// Регистрация слабости
ElementResistanceRegistry.registerSingleUniform(
    EntityType.ZOMBIE,
    ElementType.FIRE,
    1.5f  // количество очков накопления
);

// Регистрация кастомных сопротивлений
Map<ElementType, ElementResistanceManager.Resistance> resistances = new EnumMap<>(ElementType.class);
resistances.put(ElementType.FIRE, new ElementResistanceManager.Resistance(0.5f, 0.5f));
resistances.put(ElementType.ICE, new ElementResistanceManager.Resistance(1.5f, 1.0f));

ElementResistanceRegistry.registerMultiple(EntityType.CREEPER, resistances);
```

### Применение элементального урона программно

```java
import com.auranite.abloom.ElementDamageHandler;
import com.auranite.abloom.ElementType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

// Создание элементального урона от источника
ElementDamageHandler.applyElementalDamageWithSource(
    targetEntity,      // цель
    sourceEntity,      // источник урона
    5.0f,              // базовый урон
    ElementType.FIRE,  // тип элемента
    1.5f               // количество очков накопления
);
```

### Работа с эффектами состояния

Мод добавляет 8 новых эффектов:

| Эффект | ID | Цвет | Описание |
|--------|----|------|----------|
| BURNING | `burning` | #FF5500 | Поджигает цель каждую секунду |
| WETNESS | `wetness` | #0080FF | Увеличивает накопление резонанса |
| STUN | `stun` | #8B4513 | Оглушает цель |
| FREEZE | `freeze` | #00BFFF | Замораживает и замедляет |
| SHOCK | `shock` | #FF19FF | Снижает урон, наносимый целью |
| BREAK | `break` | #9400D3 | Специальный эффект разрушения |
| BLOOM | `bloom` | #32CD32 | Увеличивает получаемое накопление и урон |
| RIFT | `rift` | #FF5C77 | Увеличивает получаемый урон |

#### Получение эффекта

```java
import com.auranite.abloom.AbloomModEffects;
import net.minecraft.world.effect.MobEffectInstance;

// Добавление эффекта
entity.addEffect(new MobEffectInstance(
    AbloomModEffects.BURNING,
    200,  // длительность в тиках (10 секунд)
    0,    // усилитель
    false, // показывать частицы
    true   // скрыть иконку
));
```

### Вспомогательные методы

#### Получение элемента из источника урона

```java
import com.auranite.abloom.ElementDamageHandler;
import net.minecraft.world.damagesource.DamageSource;

ElementType type = ElementDamageHandler.getElementTypeFromSource(damageSource);
```

#### Получение элемента из предмета

```java
import com.auranite.abloom.ElementDamageHandler;
import net.minecraft.world.item.ItemStack;

ElementType type = ElementDamageHandler.getElementTypeFromItem(itemStack);
```

#### Создание цветных всплывающих чисел урона

```java
import com.auranite.abloom.ElementDamageHandler;
import net.minecraft.world.entity.LivingEntity;

// Спавн числа урона
ElementDamageHandler.spawnDamageNumber(entity, 15.5f, ElementType.FIRE);

// Спавн текстового статуса
ElementDamageHandler.spawnStatusText(entity, "Overheating!", 0xFF5500);
```

---

## Конфигурация и настройка

### Изменение базовых параметров

```java
import com.auranite.abloom.ElementDamageHandler;
import com.auranite.abloom.ElementType;

// Изменение цвета урона для элемента
ElementDamageHandler.setDamageColor(ElementType.FIRE, 0xFF0000);

// Получение всех цветов урона
Map<ElementType, Integer> colors = ElementDamageHandler.getAllDamageColors();

// Получение текущего порога активации
int threshold = ElementDamageHandler.getThreshold();  // по умолчанию 100
```

### Очереди задач сервера

```java
import com.auranite.abloom.AbloomMod;

// Выполнение задачи через N тиков
AbloomMod.queueServerWork(20, () -> {
    // код выполняется через 1 секунду (20 тиков)
});
```

---

## Интеграция с другими модами

### Добавление элементальных свойств к предметам из других модов

```java
// В методе onCommonSetup или аналогичном
ElementalWeaponRegistry.registerWeapon(
    ModItems.SWORD_FROM_OTHER_MOD,
    ElementType.ELECTRIC,
    1.2f
);
```

### Настройка сопротивлений для мобов из других модов

```java
// Через теги (рекомендуется)
// Создайте файл: data/abloom/tags/entity_type/element/fire/immune.json
{
  "values": [
    "othermod:fire_elemental",
    "othermod:lava_golem"
  ]
}
```

---

## Примеры использования

### Создание элементального меча

```java
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(Registries.ITEM, "mymod");

    public static final DeferredHolder<Item, Item> FIRE_SWORD =
        ITEMS.register("fire_sword", () -> {
            Item sword = new SwordItem(Tiers.DIAMOND,
                new Item.Properties().attributes(
                    SwordItem.createAttributes(Tiers.DIAMOND, 3, -2.4f)
                )
            );
            // Регистрация как огненного оружия с количеством накопления 1.5 очка
            ElementalWeaponRegistry.registerWeapon(sword, ElementType.FIRE, 1.5f);
            return sword;
        });
}
```

### Создание магического посоха со снарядами

```java
public class MagicStaffItem extends Item {
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof LivingEntity living) {
            ServerLevel serverLevel = (ServerLevel) level;

            // Запуск огненного шара
            ElementalProjectileRegistry.createAndLaunchElementalProjectile(
                serverLevel,
                living,
                EntityType.FIREBALL,
                1.8f,
                0.5f
            );

            player.getItemInHand(hand).hurtAndBreak(1, living,
                EquipmentSlot.MAINHAND);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
```

### Босс с элементальными сопротивлениями

```java
public class CustomBoss extends Monster {
    public CustomBoss(EntityType<? extends Monster> type, Level level) {
        super(type, level);

        // Регистрация сопротивлений при создании
        ElementResistanceRegistry.registerMultiple(this.getType(), Map.of(
            ElementType.FIRE, new ElementResistanceManager.Resistance(0.0f, 0.0f),  // иммунитет
            ElementType.ICE, new ElementResistanceManager.Resistance(2.0f, 1.5f),   // слабость
            ElementType.PHYSICAL, new ElementResistanceManager.Resistance(0.5f, 0.7f) // сопротивление
        ));
    }
}
```

---

## Отладка

```java
import com.auranite.abloom.ElementResistanceRegistry;
import com.auranite.abloom.ElementalWeaponRegistry;
import com.auranite.abloom.ElementalProjectileRegistry;

// Вывод информации о зарегистрированных сопротивлениях
ElementResistanceRegistry.debugPrint();

// Получение количества зарегистрированных объектов
int weapons = ElementalWeaponRegistry.getRegisteredCount();
int projectiles = ElementalProjectileRegistry.getRegisteredCount();
```