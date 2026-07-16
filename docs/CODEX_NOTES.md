# Codex Notes

Цей файл містить правила для AI-агента, який буде допомагати писати код.

## Project identity

Це **BattDeck**, Android-застосунок для обліку батарей.

Не плутати з **Sarmat Monitor**.

Sarmat Monitor — це окремий desktop-проєкт для телеметрії, WiFi bridge і MAVLink.

BattDeck — це Android-застосунок для ручного списку батарей.

## Do not use old stack

Не використовувати:

- Little One;
- native C;
- NDK;
- CMake;
- custom game loop;
- old renderer;
- OpenGL;
- SDL;
- WebView.

Нова версія повинна бути чистим Android-native застосунком.

## Recommended stack

- Kotlin;
- Jetpack Compose;
- локальний JSON-файл;
- AtomicFile для безпечного запису;
- Coroutines;
- ViewModel.

## UI style

Зберегти стиль першої версії:

- dark tactical UI;
- pixel/HUD aesthetics;
- green/orange/red status colors;
- large numbers;
- compact battery rows;
- simple screens;
- no generic Material look.

## Scope discipline

Не додавати зайве в MVP.

Не додавати:

- cloud sync;
- accounts;
- server;
- Bluetooth;
- telemetry;
- QR scanning;
- statistics dashboards;
- complicated inventory system.

Спочатку зробити базовий облік батарей.

## Business rules

- Store voltage, not percent.
- Percent is calculated from settings.
- Only one active battery is allowed.
- Queue order matters.
- Battery reset sets it to low/min voltage in MVP.
- Settings changes affect percent display, not stored voltage.

## Code quality

- Keep battery rules out of UI composables.
- Keep persistence out of UI.
- Use ViewModels.
- Use repositories.
- Keep models simple.
- Prefer small focused files.
- Avoid giant screens with all logic inside.

## Language

User-facing UI text should be Ukrainian.

Code comments, if needed, should be in English.
