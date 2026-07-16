# UI Specification

BattDeck uses a dark Material 3 interface with a modern technical mood. It uses standard `Scaffold`, top app bars, cards, buttons, chips, text fields, and progress indicators. The old pixel/terminal presentation and stepper-based primary inputs are not used.

## Main screen

The top bar shows `BATTDECK`, a short subtitle, Help, and Settings. Each outlined card shows the text battery name, type chip, voltage, calculated percentage, last-update text, and a horizontal charge indicator. An active pack has an `АКТИВНА` chip, highlighted surface, and blue border.

Tap a card for details; tap its charge bar to edit voltage. Swipe left or right to toggle active state; the card follows the finger and reveals an action background. Packs at 95% or above can be reordered by vertically dragging their full-height color handle; all packs below 95% are grouped at the end of the list.

## Charge editing

The screen shows the pack name and type, current voltage, calculated percentage, vertical indicator, and an `OutlinedTextField` labeled `Напруга` with decimal keyboard and `V` suffix. Both `.` and `,` are accepted. Invalid, empty, partial, or out-of-range values show an error and cannot be saved.

## Battery details

The name is editable in a required text field with a 32-character limit. Filter chips select `СИНЯ` or `ЧОРНА`. The screen also shows voltage, percentage, charge indicator, update age, active state, and actions to activate, edit charge, remove, cancel, or save.

## Settings

Outlined numeric fields edit pack count, minimum voltage, and maximum voltage. Count is restricted to `1..50`; minimum must be lower than maximum. Errors are shown in Ukrainian and saving is disabled until the changed values are valid. A scale preview maps minimum to 0% and maximum to 100%.

## Help and accessibility

Help describes the current fields and gestures. Standard Material components provide comfortable touch targets and high-contrast text. The app remains dark-mode only.

## Localization

All user-facing text is stored in Android string resources. Ukrainian in `values/strings.xml` is the fallback language; English is provided in `values-en/strings.xml`. On first launch the app selects English for an English phone locale and Ukrainian otherwise. Settings provide a two-flag segmented selector for Ukrainian and English; the choice is persisted with app settings.
