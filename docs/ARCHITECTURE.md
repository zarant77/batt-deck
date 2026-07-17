# Architecture

## Stack

- Kotlin;
- Jetpack Compose;
- Material 3;
- JSON (`org.json`);
- Android `AtomicFile`;
- Coroutines;
- StateFlow;
- ViewModel.

## Layers

```text
ui -> viewmodel -> JSON repository
                 -> backup services
                 -> domain rules
```

## UI layer

The UI layer is responsible only for rendering state and handling user events. It must not contain business logic for charge calculations, status selection, or persistence.

Main UI components include:

- `BatteryListScreen`;
- `BatteryDetailsScreen`;
- `ChargeEditScreen`;
- `SettingsScreen`;
- `HelpScreen`;
- shared components in `ui/components`.

The UI launches Android system integrations such as the document picker and share sheet. JSON parsing and mapping remain outside composables.

## ViewModel layer

`AppViewModel` is responsible for:

- exposing screen state;
- handling UI actions;
- calling the repository;
- coordinating import previews and confirmation;
- managing temporary language preview state.

## Domain layer

The domain layer owns:

- charge percentage calculation;
- charge status selection;
- input normalization and validation;
- active battery rules;
- reset behavior;
- relative update-time calculations.

## Data layer

The data layer contains:

- the single `JsonRepository`;
- private-state JSON decoding and migration;
- atomic writes of the complete application state;
- `BackupExporter` and `BackupImporter` for the portable JSON format;
- observable data streams for the UI.

## Offline-first

The app is fully offline. All state is stored in the private `battdeck.json` file, which contains settings and the complete battery list.

Writes are atomic: the previous valid file is not replaced until the new state has been written successfully. Import and export use Android system file APIs and do not require broad storage permissions or network access.

## State management

The app uses:

- `StateFlow` in the ViewModel;
- immutable domain and UI state;
- explicit events and repository methods for changes.

Example:

```kotlin
data class AppUiState(
    val batteries: List<Battery>,
    val settings: AppSettings,
    val isLoading: Boolean,
)
```

## Error handling

The app must handle:

- invalid settings;
- storage failures;
- invalid voltage and date input;
- malformed or unsupported backup files;
- invalid marking references;
- multiple active batteries in imported data.

Bad external data must not corrupt the existing local state or crash the app.
