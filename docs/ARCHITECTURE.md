# Architecture

## Recommended stack

- Kotlin;
- Jetpack Compose;
- JSON (`org.json`);
- AtomicFile;
- Coroutines;
- StateFlow;
- ViewModel.

## Layers

```text
ui -> viewmodel -> JSON repository
                 -> domain rules
```

## UI layer

Відповідає тільки за відображення і події користувача.

UI не повинен містити бізнес-логіку розрахунку заряду або статусів.

Приклади UI компонентів:

- BatteryListScreen;
- BatteryDetailsScreen;
- ChargeEditScreen;
- SettingsScreen;
- HelpScreen;
- BatteryRow;
- BatteryBar;
- PixelButton.

## ViewModel layer

Відповідає за:

- стан екранів;
- обробку UI actions;
- виклики repository;
- підготовку UI models.

## Domain layer

Відповідає за правила:

- розрахунок відсотка;
- визначення статусу;
- правила активної батареї;
- правила скидання;
- форматування часу `9 ДНІВ ТОМУ`.

## Data layer

Відповідає за:

- єдиний `JsonRepository`;
- читання та валідацію JSON;
- атомарний запис повного стану;
- потоки даних для UI.

## Offline-first

Застосунок повністю офлайн.

Усі дані зберігаються локально у приватному файлі `battdeck.json`. Файл містить налаштування і повний список батарей. Запис виконується атомарно: попередній валідний файл не замінюється, поки нова версія не записана повністю.

## State management

Рекомендовано використовувати:

- `StateFlow` у ViewModel;
- immutable UI state;
- events/actions для змін.

Приклад:

```kotlin
data class BatteryListUiState(
    val batteries: List<BatteryUiModel>,
    val activeBatteryId: Long?,
    val settings: AppSettings,
    val isLoading: Boolean = false
)
```

## Error handling

MVP повинен обробляти:

- невалідні налаштування;
- неможливість зберегти дані;
- некоректну напругу;
- дублікати номерів, якщо вирішено їх заборонити.

Не треба падати через погані дані.
