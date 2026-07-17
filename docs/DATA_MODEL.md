# Data Model

## Battery

`Battery` represents one battery pack. Its user-visible identifier is a text `name`, not a numeric counter.

```kotlin
data class Battery(
    val id: Long,
    val name: String,
    val markingIndex: Int,
    val voltage: Double,
    val sortOrder: Int,
    val isActive: Boolean,
    val isRemoved: Boolean,
    val lastUpdatedAt: Long,
    val createdAt: Long,
)
```

Name rules:

- required after trimming leading and trailing spaces;
- maximum 32 characters;
- names do not have to be unique;
- default packs are named `01`, `02`, `03`, and so on.

`markingIndex` is the zero-based position of an editable `BatteryMarking` in app settings. A marking contains only an editable `name` and ARGB `color`; it has no user-visible or persisted ID. The default markings are blue and black, but users can add, rename, recolor, and remove unused markings. `BatteryStatus` is derived and is not persisted.

## Settings

Defaults are `batteryCount = 10`, `minVoltage = 40.2`, and `maxVoltage = 50.2`. Count is limited to `1..50`; minimum voltage must be lower than maximum voltage.

## Storage and migration

All state is stored offline in the private UTF-8 `battdeck.json` file and written atomically through Android `AtomicFile`. The current JSON schema is version 3 and stores battery names, marking references, and editable marking definitions.

The app performs non-destructive compatibility migrations while reading older data: numeric `number` becomes a two-digit `name`, and legacy `type: BLUE/BLACK` or `markingId: blue/black` becomes `markingIndex: 0/1`. Language is stored as `UK` or `EN`. The current JSON schema is version 5. No Room or DataStore database is currently used by this project.

Voltage, settings, names, active state, removal state, order, and timestamps are persisted. Percentage and status are always derived from domain rules.

## Portable JSON export

The user-facing backup format is separate from the private storage schema and currently has `schemaVersion: 1`. It contains `app: "BattDeck"`, `exportedAt`, settings (including language code and the single ordered list of editable markings), and batteries without local database IDs. Each battery contains only `markingIndex`, never a duplicated marking object, plus its name, voltage, order, state flags, and timestamps.

Import validates the complete document before writing anything. Version 1 is supported. Battery names are trimmed and limited to 32 characters, voltage must be finite, count must be in `1..50`, and minimum voltage must be below maximum voltage. Imported batteries receive new sequential local IDs. If several batteries are active, only the first by `sortOrder` remains active. If none is active, all batteries remain inactive. Newly created app data also starts without an active battery.
