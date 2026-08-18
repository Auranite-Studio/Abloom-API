# Регистрация кастомных элементов через датапак

Abloom API поддерживает регистрацию кастомных элементов через датапаки. Это позволяет добавлять новые типы элементального урона без изменения кода мода.

## Структура JSON файла

Файлы кастомных элементов должны находиться в директории `data/<modid>/custom_elements/` и иметь расширение `.json`.

### Формат JSON

```json
{
  "element_id": "CUSTOM_ELEMENT",
  "damage_type": "yourmod:custom_element",
  "color": 0x2B004C,
  "based_elemental_dmg": "FIRE",
  "element_translation_key": "element.yourmod.custom",
  "resonance_translation_key": "resonance_text.yourmod.custom",
  "can_resonance_accumulation": true,
  "effect": {
    "type": "yourmod:custom_resonance_effect",
    "config": {
      "duration": 100,
      "amplifier": 0
    }
  }
}
```

## Поля JSON

### Обязательные поля

| Поле | Тип | Описание |
|------|-----|----------|
| `element_id` | String | ID элемента (без namespace). Должен содержать только заглавные буквы, цифры и подчёркивания. Пример: `COSMIC`, `VOID_ELEMENT` |
| `damage_type` | String | Полный ID типа урона с namespace. Должен соответствовать типу урона из вашего датапака. Пример: `yourmod:cosmic_damage` |
| `color` | Integer | Цвет элемента в формате RGB (шестнадцатеричное или десятичное число). Пример: `0x2B004C` или `2818124` |

### Необязательные поля

| Поле | Тип | Значение по умолчанию | Описание |
|------|-----|----------------------|----------|
| `based_elemental_dmg` | String | `PHYSICAL` | Базовый элемент для fallback-поведения. Должен быть одним из встроенных элементов: `FIRE`, `WATER`, `EARTH`, `WIND`, `ICE`, `ELECTRIC`, `ENERGY`, `NATURAL`, `QUANTUM`, `ETHER`, `LIGHT`, `SHADOW`, `PRISMATIC`, `PHYSICAL` |
| `element_translation_key` | String | `element.<namespace>.<element_id>` | Ключ локализации для названия элемента |
| `resonance_translation_key` | String | `resonance_text.<namespace>.<element_id>` | Ключ локализации для текста эффекта резонанса |
| `can_resonance_accumulation` | Boolean | `true` | Может ли элемент накапливать очки резонанса |
| `effect` | Object | `null` | Конфигурация эффекта резонанса, срабатывающего при достижении 100 очков |

### Поля объекта effect

| Поле | Тип | Описание |
|------|-----|----------|
| `type` | String | ID типа эффекта резонанса (регистрационное имя вашего эффекта) |
| `config.duration` | Integer | Длительность эффекта в тиках (20 тиков = 1 секунда) |
| `config.amplifier` | Integer | Уровень усиления эффекта (0 = базовый уровень) |

## Примеры

### Пример 1: Простой кастомный элемент

Файл: `data/myaddon/custom_elements/void.json`

```json
{
  "element_id": "VOID",
  "damage_type": "myaddon:void_damage",
  "color": 0x1a0033,
  "based_elemental_dmg": "SHADOW",
  "element_translation_key": "element.myaddon.void",
  "resonance_translation_key": "resonance_text.myaddon.void"
}
```

### Пример 2: Элемент с эффектом резонанса

Файл: `data/cosmic_weapons/custom_elements/cosmic.json`

```json
{
  "element_id": "COSMIC",
  "damage_type": "cosmic_weapons:cosmic_radiation",
  "color": 0x6600cc,
  "based_elemental_dmg": "QUANTUM",
  "element_translation_key": "element.cosmic_weapons.cosmic",
  "resonance_translation_key": "resonance_text.cosmic_weapons.cosmic",
  "can_resonance_accumulation": true,
  "effect": {
    "type": "cosmic_weapons:cosmic_decay",
    "config": {
      "duration": 200,
      "amplifier": 1
    }
  }
}
```

### Пример 3: Элемент без накопления резонанса

Файл: `data/hardcore_modes/custom_elements/pure_energy.json`

```json
{
  "element_id": "PURE_ENERGY",
  "damage_type": "hardcore_modes:pure_energy_dmg",
  "color": 0xffffff,
  "based_elemental_dmg": "ENERGY",
  "can_resonance_accumulation": false
}
```

## Локализация

Добавьте ключи локализации в ваш файл языка:

**assets/myaddon/lang/ru_ru.json:**
```json
{
  "element.myaddon.void": "Пустота",
  "resonance_text.myaddon.void": "Эффект пустоты: поглощение энергии"
}
```

**assets/myaddon/lang/en_us.json:**
```json
{
  "element.myaddon.void": "Void",
  "resonance_text.myaddon.void": "Void Effect: Energy Drain"
}
```

## Создание типа урона

Для работы кастомного элемента необходимо создать соответствующий тип урона в датапаке:

**data/yourmod/damage_type/custom_element.json:**
```json
{
  "exhaustion": 0.1,
  "message_id": "yourmod:custom_element",
  "scaling": "when_caused_by_living_non_player"
}
```

## Интеграция с оружием

После регистрации кастомного элемента его можно использовать в конфигурации элементального оружия:

**data/yourmod/elemental_weapons/void_sword.json:**
```json
{
  "item": "yourmod:void_sword",
  "element": "VOID",
  "crit_chance": 0.15,
  "crit_damage": 1.5,
  "accumulation_multiplier": 2.0
}
```

## Программная регистрация

Для продвинутых случаев поддерживается программная регистрация через Java API:

```java
import com.auranite.abloom.CustomElementRegistry;
import com.auranite.abloom.ElementType;

// Во время инициализации мода
CustomElementRegistry.registerCustom(
    "mymod",                              // namespace
    "CUSTOM_ELEMENT",                      // element_id
    "mymod:custom_damage",                 // damage_type
    0x2B004C,                              // color
    ElementType.FIRE,                      // baseElement
    "element.mymod.custom",                // translationKey
    "resonance_text.mymod.custom",         // resonanceTranslationKey
    true,                                  // canAccumulate
    null                                   // effectData (или объект ResonanceEffectData)
);
```

## Отладка

Проверьте логи сервера после загрузки мира:
```
[Server thread/INFO]: Registering custom elements from datapacks...
[Server thread/INFO]: Registered custom element: mymod.CUSTOM_ELEMENT
[Server thread/INFO]: Custom elements registered successfully
```

## Ограничения

1. **ID элемента** должен быть уникальным и не конфликтовать со встроенными элементами
2. **Namespace** берётся из пути к файлу (`data/<namespace>/custom_elements/`)
3. **Тип урона** должен быть зарегистрирован в датапаке до использования
4. **Эффекты резонанса** должны быть зарегистрированы в моде до загрузки мира

## Совместимость

- Кастомные элементы работают со всеми системами Abloom API
- Поддерживаются в элементальном оружии, снарядах и сопротивлениях
- Совместимы с MCreator через процедурные блоки
