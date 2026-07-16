# Project Structure

Цей файл описує рекомендовану структуру коду. Її можна уточнювати після створення Android-проєкту.

## Actual structure

```text
battdeck/
  README.md
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

      test/java/com/catemup/battdeck/domain/
        BatteryRulesTest.kt
```

`AppViewModel` координує невеликий MVP-стан усіх екранів; правила заряду, нормалізація вводу та валідація залишаються в `domain`, а читання, сумісна міграція та атомарний запис `battdeck.json` — у `data/JsonRepository.kt`. Портативний JSON schema 1 створюють і перевіряють класи в `data/backup`; системні file picker і share sheet запускає UI. Material 3 компоненти й спільні поля вводу знаходяться в `ui/components/Components.kt`.

## Package name

Рекомендовано:

```text
com.catemup.battdeck
```

## Naming rules

- Domain models: чисті назви без `Entity`.
- Persistence models: `AppData` і domain-моделі, що серіалізуються в JSON.
- UI state: immutable data classes such as `AppUiState`.
- ViewModels: names with the `ViewModel` suffix, currently `AppViewModel` for the compact MVP.

## Assets

Можлива структура:

```text
app/src/main/assets/
app/src/main/res/drawable/
app/src/main/res/font/
```

Якщо буде потрібен кастомний шрифт, зберігати його в:

```text
app/src/main/res/font/
```

## Notes

Не треба переносити старий native C/Little One код.

Нова версія повинна бути чистою Android-native реалізацією.

TODO для v0.2: реалізувати drag-and-drop зміну `sortOrder` та окремі ViewModel екранів, якщо їхній стан стане складнішим.
