# Product Specification

## Name

BattDeck

## Platform

Android.

## Application type

Offline utility for tracking battery packs.

## MVP

The first version contains five main screens:

1. Main battery list.
2. Help screen.
3. App settings screen.
4. Charge editing screen.
5. Battery settings screen.

## Core entities

### Battery pack

A battery pack has:

- a local ID;
- a user-editable name;
- a marking index;
- voltage;
- the last charge update date;
- active state;
- a position in the usage queue.

### Battery marking

Markings visually distinguish groups of battery packs. A marking consists of an editable name and color. Batteries reference a marking by its position in the global markings list, so editing a marking updates every battery that uses it.

### Settings

Global settings include:

- battery count;
- minimum voltage;
- maximum voltage;
- app language;
- ordered battery markings.

## Main actions

### Toggle active battery

Swipe left or right on a battery card.

At most one battery can be active. New data starts with all batteries inactive, and users may also leave every battery inactive.

### Reset a battery

Resetting means the pack has been used or is no longer ready. In the MVP, reset sets the voltage to the configured minimum and clears the active state.

### Change charge

Tapping a battery opens the charge screen. The user changes charge with a gesture over the battery indicator, while voltage and percentage are calculated from the configured range.

### Edit a battery

The battery settings screen allows the user to edit its name, marking, and charge update date.

### Change queue order

Ready batteries can be reordered on the main screen by dragging their full-height color handle. Discharged batteries remain grouped at the end.

### Import and export data

Settings allow the user to export packs and configuration to JSON through the standard Android share sheet. Import uses the system document picker, displays a preview, validates the entire file, and replaces local data only after confirmation. No network, server, or account is involved.

## MVP constraints

- No accounts.
- No network dependency.
- No cloud synchronization.
- No analytics or advertising.
- No automatic battery telemetry.
- No change history unless added in a later version.

## Primary requirement

The app must remain simple and very fast to use.
