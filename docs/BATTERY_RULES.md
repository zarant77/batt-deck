# Battery Rules

## Core principle

Voltage is the stored charge value. Percentage is only a visual scale between the configured minimum and maximum voltage.

## Percentage formula

```text
percent = (voltage - minVoltage) / (maxVoltage - minVoltage) * 100
```

The result is clamped to `0..100`:

```text
if percent < 0   -> 0
if percent > 100 -> 100
```

## Default voltage scale

```text
minVoltage = 40.2 V
maxVoltage = 50.2 V
```

Users can change these values in settings. Changing the scale does not modify stored battery voltage; it only changes the calculated percentage.

## Charge colors

Default thresholds:

```text
>= 95%  green
>= 50%  orange
< 50%   red
```

## Statuses

### Ready

```text
percent >= 95
```

Color: green.

### Warning

```text
percent >= 50 && percent < 95
```

Color: orange.

### Danger

```text
percent < 50
```

Color: red or dark red.

## Active battery

- At most one battery may be active.
- Activating a new battery deactivates the previous one.
- Toggling the active battery may leave all batteries inactive.
- Newly created data has no active battery by default.
- The active battery must be clearly highlighted on the main screen.

## Reset battery

Reset means that a pack has been used or is no longer ready.

MVP behavior:

- set `voltage = minVoltage`;
- update `lastUpdatedAt`;
- clear `isActive` if the battery was active.

Possible future behavior:

- a separate `Used` state;
- usage history;
- a dedicated charging workflow.

## Queue

The list order represents the usage queue.

- Packs at 95% or above can be reordered manually.
- Packs below 95% are always grouped at the end.
- Dragging continues until the user lifts their finger.
