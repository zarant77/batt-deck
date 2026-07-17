# Purpose

## What it is

**BattDeck** is an Android app for manually tracking UAV battery packs.

It helps an operator or crew quickly see:

- which batteries are charged;
- which battery is currently in use;
- which batteries have already been reset or discharged;
- which pack should be used next;
- when the charge was last updated;
- which marking is assigned to each pack.

## Why it exists

Battery packs are easy to mix up in field conditions, especially when there are many packs, several people are working in parallel, and time is limited.

The app replaces scattered notes, memory, scraps of paper, physical markers, and vague statements such as “this one should be fine.”

## What the app does not do

The MVP does not include:

- a server component;
- cloud synchronization;
- telemetry;
- Bluetooth battery reading;
- automatic voltage measurement;
- warehouse inventory management;
- user authentication.

All values are entered manually. This is a deliberate choice for simplicity, reliability, and speed.

## Main workflow

1. Open the app.
2. Review the battery list.
3. Take the first ready battery from the queue.
4. Mark it as active.
5. After use, reset it or update its charge.
6. Move on to the next battery.

## Core value

The app should reduce the risk of mistakes when working with batteries.

Most importantly, an operator should not accidentally select a discharged or unready pack.
