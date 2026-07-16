# Data Model

## Battery

`Battery` represents one battery pack. Its user-visible identifier is a text `name`, not a numeric counter.

```kotlin
data class Battery(
    val id: Long,
    val name: String,
    val markingId: String,
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

`markingId` references an editable `BatteryMarking` stored in app settings. A marking has a stable `id`, editable `name`, and ARGB `color`. The default markings are blue and black, but users can add, rename, recolor, and remove unused markings. `BatteryStatus` is derived and is not persisted.

## Settings

Defaults are `batteryCount = 10`, `minVoltage = 40.2`, and `maxVoltage = 50.2`. Count is limited to `1..50`; minimum voltage must be lower than maximum voltage.

## Storage and migration

All state is stored offline in the private UTF-8 `battdeck.json` file and written atomically through Android `AtomicFile`. The current JSON schema is version 3 and stores battery names, marking references, and editable marking definitions.

The app performs non-destructive compatibility migrations while reading older data: numeric `number` becomes a two-digit `name`, and legacy `type: BLUE/BLACK` becomes `markingId: blue/black`. The current JSON schema is version 3. No Room or DataStore database is currently used by this project.

Voltage, settings, names, active state, removal state, order, and timestamps are persisted. Percentage and status are always derived from domain rules.
