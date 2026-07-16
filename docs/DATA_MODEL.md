# Data Model

## Battery

Основна сутність — комплект батареї.

```kotlin
data class Battery(
    val id: Long,
    val number: Int,
    val type: BatteryType,
    val voltage: Double,
    val sortOrder: Int,
    val isActive: Boolean,
    val isRemoved: Boolean,
    val lastUpdatedAt: Long,
    val createdAt: Long
)
```

## BatteryType

```kotlin
enum class BatteryType {
    BLUE,
    BLACK
}
```

UI назви українською:

- `BLUE` → `СИНЯ`;
- `BLACK` → `ЧОРНА`.

## BatteryStatus

```kotlin
enum class BatteryStatus {
    Ready,
    Warning,
    Low,
    Danger
}
```

Можливі UI назви:

- `Ready` → `ГОТОВА`;
- `Warning` → `ПЕРЕВІР`;
- `Low` → `НИЗЬКА`;
- `Danger` → `НЕБЕЗПЕКА`.

## AppSettings

```kotlin
data class AppSettings(
    val batteryCount: Int,
    val minVoltage: Double,
    val maxVoltage: Double
)
```

Default values:

```text
batteryCount = 10
minVoltage = 40.2
maxVoltage = 50.2
```

## Storage

Усі дані зберігаються в одному приватному файлі застосунку:

- назва: `battdeck.json`;
- формат: UTF-8 JSON;
- кореневі поля: `schemaVersion`, `settings`, `batteries`;
- запис: атомарний через Android `AtomicFile`;
- доступ до файлу послідовний через coroutine `Mutex`.

Приклад структури:

```json
{
  "schemaVersion": 1,
  "settings": {
    "batteryCount": 10,
    "minVoltage": 40.2,
    "maxVoltage": 50.2
  },
  "batteries": []
}
```

## Правила збереження

- Напруга зберігається як фактичне значення.
- Відсоток не зберігається, а рахується з налаштувань.
- Статус не зберігається, а рахується з напруги і налаштувань.
- Активна батарея може бути тільки одна.
- Черга визначається `sortOrder`.
- Вилучена батарея залишається у файлі з `isRemoved = true`.
