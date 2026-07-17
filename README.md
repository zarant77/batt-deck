# BattDeck

<img src="icon.png" width="128">

**BattDeck** is an Android application for managing UAV battery sets.

Its purpose is simple: quickly see which batteries are charged, which battery is currently active, which sets have already been used, and the order in which they should be taken.

This is not a complex ERP system or a “smart cloud service.” It is a compact offline tool designed for field operations.

BattDeck works entirely offline using a local-first approach. Data is stored only on the device. Manual JSON import and export through the standard Android file picker and share sheet are available for transferring data between devices.

## Core Concept

- a list of battery sets;
- a number for each set;
- editable set labels and colors;
- current voltage;
- charge percentage calculated from the voltage range;
- date of the last charge update;
- active battery;
- manual charge adjustment;
- quick reset of a used battery;
- queue order management;
- local JSON import and export through the system file picker and share menu.

## Technology Stack

Baseline recommendation for the new version:

- Kotlin;
- Jetpack Compose;
- a local JSON file;
- Material 3 as the technical foundation, with a custom tactical/pixel UI.

## Documentation

- [Purpose](docs/PURPOSE.md)
- [Product Specification](docs/PRODUCT_SPEC.md)
- [User Interface](docs/UI_SPEC.md)
- [Data Model](docs/DATA_MODEL.md)
- [Battery Rules](docs/BATTERY_RULES.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Project Structure](docs/STRUCTURE.md)
- [Roadmap](docs/ROADMAP.md)
- [Codex Notes](docs/CODEX_NOTES.md)

## Principle

The application must be fast, simple, offline, and understandable at a glance.

The operator should not have to think about where to tap. Open the app, check the battery status, and take the correct set.

## Privacy

BattDeck does not use:

- accounts or authentication;
- the internet or a remote server;
- cloud synchronization;
- analytics or tracking SDKs;
- advertising or in-app purchases.

The application does not request the `INTERNET` permission, broad storage permissions, or access to the list of installed applications. All operational data remains in Android's private local storage until the user explicitly exports a JSON file.

## Build and Run

Android Studio with JDK 17 and Android SDK 35 is required.

The easiest option is to launch the interactive runner:

```bash
./runner.sh
```

Or run a specific action without the menu:

```bash
./runner.sh doctor      # Check the environment and device connection
./runner.sh build-run   # Build, install, and launch on the phone
./runner.sh release     # Build release APK and AAB files in build/
./runner.sh test        # Run unit tests
./runner.sh clean       # Remove build outputs
./runner.sh deep-clean  # Also remove the local Gradle cache
./runner.sh help        # Show all available commands
```

On the phone, enable **Developer options** → **USB debugging** and confirm access for this computer. If multiple devices are connected, the runner will prompt you to select one.

Run Gradle commands directly:

```bash
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew test
```

The debug APK will be created in `app/build/outputs/apk/debug/`. Without `keystore.properties`, the release command creates an unsigned APK suitable for F-Droid signing. To run the project, open the repository root in Android Studio, wait for Gradle synchronization to finish, and launch the `app` configuration on a device or emulator running Android 7.0 or newer.

### Signing the Release Build

Release APK and AAB files are signed with `keystore/battdeck-upload.jks`. Copy the local template and enter the actual key credentials:

```bash
cp keystore.properties.example keystore.properties
```

Fill in `storePassword`, `keyAlias`, and `keyPassword`, then run `./runner.sh release`. The `keystore.properties` file and all keystore files are excluded from Git. Signed outputs will be created as `build/BattDeck-release.apk` and `build/BattDeck-release.aab`.

## Distribution

Signed APK files can be distributed directly through GitHub Releases or another trusted channel. Users should verify the source and APK signature before installation.

The repository is prepared for F-Droid: a draft recipe is located at `metadata/com.catemup.battdeck.yml`, and localized descriptions are stored in `fastlane/metadata/android/`. Official inclusion in the F-Droid catalog requires a public tag, an accessible source archive, and a separate merge request to `fdroiddata`.

## F-Droid Repository

BattDeck can be installed from its own F-Droid repository:

```text
https://zarant77.github.io/batt-deck/fdroid/repo/
```

Instructions for creating, signing, and publishing the repository are available in [docs/FDROID_REPO.md](docs/FDROID_REPO.md).

## Application Icon

The `icon.png` file in the repository root is the single source for Android launcher icons and the F-Droid repository icon. To generate all icons without Android Studio, install ImageMagick and run the script:

```bash
brew install imagemagick
./tools/generate_launcher_icons.sh
```

You can also select `Generate launcher icons` in the interactive `./runner.sh` menu.

## Implemented in v0.3.0

- five MVP screens in Ukrainian;
- local storage of all data in a single JSON file;
- battery charge, label, and name editing;
- activation and reset using swipe gestures;
- safe adjustment of the number of battery sets and the voltage range;
- transfer of backup JSON files between devices without cloud services or accounts.

## License

BattDeck is licensed under the Apache License 2.0. See [LICENSE](LICENSE).
