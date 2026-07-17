# UI Specification

BattDeck uses a dark Material 3 interface with a modern technical style. It relies on standard scaffolds, top app bars, cards, buttons, chips, text fields, progress indicators, and Android system pickers.

## Main screen

The header shows the launcher logo, a colored `BattDeck` title, the current app version from `BuildConfig.VERSION_NAME`, a short subtitle, and icon actions for Help and Settings. `Batt` is blue, `Deck` is yellow, and the smaller version text is gray.

Each compact battery card uses two rows:

- a full-height marking-color drag handle;
- battery name;
- voltage and calculated percentage;
- last-update text;
- a thin horizontal charge indicator.

Tap a card to open charge editing. Swipe left or right to toggle active state; the card follows the gesture and reveals action feedback. Packs at 95% or above can be reordered by vertically dragging the color handle. Packs below 95% remain grouped at the end.

## Charge editing

The screen shows the pack name, marking color, current voltage, calculated percentage, and a large vertical battery indicator. A vertical swipe over the indicator changes charge. Saving updates `lastUpdatedAt` only when voltage actually changes.

The Settings button opens the battery settings screen. Cancel and Save remain available at the bottom.

## Battery settings

The title includes the battery name. Users can edit:

- the required battery name, up to 32 characters;
- the charge update date in validated `DD.MM.YYYY` format;
- the marking selected from the global list.

The screen also provides Remove, Cancel, and Save actions. Saving a manually entered date must preserve that date when charge has not changed.

## App settings

Sections appear in this order:

1. Language.
2. Battery count.
3. Voltage range.
4. Markings.
5. Data import and export.

Battery count is restricted to `1..50`; minimum voltage must be lower than maximum voltage. Markings have editable names and colors. A marking that is used by a battery cannot be deleted. Changing one marking updates every battery that references its list index.

Export opens the Android share sheet. Import opens the Android document picker, validates the selected JSON, and displays a preview before replacing local data.

## Help and accessibility

Help describes the current fields and gestures. Standard Material components provide comfortable touch targets, high-contrast text, and familiar Android behavior. The app remains dark-mode only and is locked to portrait orientation.

## Localization

All user-facing text is stored in Android string resources. Ukrainian in `values/strings.xml` is the fallback language; English is provided in `values-en/strings.xml`. The initial language follows the phone locale, using Ukrainian for unsupported locales. Settings provide an immediate Ukrainian/English selector; Cancel or Back restores the previously saved language.
