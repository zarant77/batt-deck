# Project Structure

This document describes the current source layout and ownership of the main components.

## Repository layout

```text
batt-deck/
  README.md
  LICENSE
  icon.png
  runner.sh
  tools/
    generate_launcher_icons.sh
    build_fdroid_repo.sh
  docs/
    PURPOSE.md
    PRODUCT_SPEC.md
    UI_SPEC.md
    DATA_MODEL.md
    BATTERY_RULES.md
    ARCHITECTURE.md
    STRUCTURE.md
    ROADMAP.md
    CODEX_NOTES.md
    FDROID_REPO.md
  metadata/
    com.catemup.battdeck.yml
  fastlane/metadata/android/
    en-US/
    uk-UA/
  fdroid/
    config.yml
    repo/
  app/
    build.gradle.kts
    src/main/
      AndroidManifest.xml
      java/com/catemup/battdeck/
        MainActivity.kt
        BattDeckApplication.kt
        data/
          JsonRepository.kt
          backup/
            BackupModels.kt
            BackupExporter.kt
            BackupImporter.kt
        domain/
          Battery.kt
          BatteryRules.kt
        ui/
          App.kt
          LocalizedText.kt
          theme/
            Colors.kt
            Theme.kt
          navigation/
            AppNavHost.kt
          screens/
            BatteryListScreen.kt
            BatteryDetailsScreen.kt
            ChargeEditScreen.kt
            SettingsScreen.kt
            HelpScreen.kt
          components/
            Components.kt
        viewmodel/
          AppViewModel.kt
    src/test/java/com/catemup/battdeck/domain/
      BatteryRulesTest.kt
```

## Responsibilities

`AppViewModel` coordinates the application state and screen actions. Charge rules, input normalization, and validation remain in `domain`. Compatible private JSON migration and atomic persistence live in `data/JsonRepository.kt`.

The portable backup schema is created and validated by `data/backup`. The UI launches Android's document picker and share sheet but does not parse JSON directly. Shared Material 3 components and fields live in `ui/components`.

## Package name

```text
com.catemup.battdeck
```

## Naming rules

- Domain models use clean names without an `Entity` suffix.
- Persistence uses `AppData` and serializable domain models.
- UI state uses immutable data classes such as `AppUiState`.
- ViewModels use the `ViewModel` suffix.
- Shell tools use lowercase snake_case and must be exposed through `runner.sh`.

## Assets

The root `icon.png` is the single source for Android launcher icons and the custom F-Droid repository icon. Generated Android resources are stored under `app/src/main/res/mipmap-*`; the generated F-Droid icon is stored at `fdroid/repo/icons/icon.png`.

If custom fonts are added, place them under:

```text
app/src/main/res/font/
```

## Notes

Do not port the old native C or Little One code. BattDeck is an Android-native application.

Private signing keys, passwords, local Gradle state, and generated build directories must never be committed.
