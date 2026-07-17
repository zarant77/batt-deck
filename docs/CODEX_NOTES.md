# Codex Notes

This file contains project-specific guidance for AI coding agents.

## Project identity

This is **BattDeck**, an Android app for manually tracking battery packs.

Do not confuse it with **Sarmat Monitor**, a separate desktop telemetry, Wi-Fi bridge, and MAVLink project.

## Do not use the old stack

Do not introduce:

- Little One;
- native C;
- NDK;
- CMake;
- a custom game loop;
- the old renderer;
- OpenGL;
- SDL;
- WebView.

BattDeck must remain a clean Android-native application.

## Current stack

- Kotlin;
- Jetpack Compose and Material 3;
- private local JSON storage;
- `AtomicFile` for safe writes;
- Coroutines and StateFlow;
- ViewModel.

## UI style

Maintain the established interface:

- dark technical UI;
- compact battery cards;
- clear green, orange, and red charge colors;
- large voltage and percentage values;
- simple screens and high-contrast controls;
- standard Material 3 behavior without unnecessary visual complexity.

## Scope discipline

Do not add unrelated features such as:

- cloud synchronization;
- accounts;
- a backend server;
- Bluetooth;
- telemetry;
- automatic battery readings;
- analytics, ads, or tracking SDKs;
- a complex inventory system.

## Business rules

- Store voltage, not percentage.
- Calculate percentage from settings.
- Allow at most one active battery, including none.
- Queue order matters.
- Reset sets a battery to the configured minimum voltage.
- Settings changes affect calculated percentage, not stored voltage.
- Batteries reference markings by list index; markings have no persisted ID.
- Changing a marking updates all batteries that reference its index.

## Code quality

- Keep battery rules out of UI composables.
- Keep persistence and JSON parsing out of UI.
- Coordinate actions through ViewModels and repositories.
- Keep models simple and immutable.
- Prefer small focused files.
- Avoid oversized screens containing unrelated logic.
- Preserve backward-compatible JSON migration where practical.

## Language

User-facing text is localized in Ukrainian and English Android resources. Code, code comments, commit messages, and project documentation should be in English.
