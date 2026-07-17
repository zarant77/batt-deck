#!/bin/sh

set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
FDROID_DIR="$ROOT_DIR/fdroid"
APK_SOURCE="$ROOT_DIR/app/build/outputs/apk/release/app-release.apk"
APK_TARGET="$FDROID_DIR/repo/com.catemup.battdeck_3.apk"
REPO_URL="https://zarant77.github.io/batt-deck/fdroid/repo/"

die() {
    printf 'Error: %s\n' "$*" >&2
    exit 1
}

command -v fdroid >/dev/null 2>&1 || die "fdroidserver is not installed. On macOS: brew install fdroidserver"
[ -x "$ROOT_DIR/gradlew" ] || die "gradlew was not found or is not executable"
KEYSTORE_PROPERTIES_FILE=${BATTDECK_KEYSTORE_PROPERTIES:-/Users/zar/Dropbox/Keys/catemup-keystore.properties}
[ -f "$KEYSTORE_PROPERTIES_FILE" ] || die "keystore properties file is required to create a signed release APK: $KEYSTORE_PROPERTIES_FILE"

printf 'Building signed BattDeck release APK...\n'
(cd "$ROOT_DIR" && ./gradlew assembleRelease)
[ -f "$APK_SOURCE" ] || die "signed app-release.apk was not created; check the local keystore configuration"

mkdir -p "$FDROID_DIR/repo"
cp "$APK_SOURCE" "$APK_TARGET"

printf 'Updating F-Droid repository metadata...\n'
(cd "$FDROID_DIR" && fdroid update --create-metadata)

printf '\nF-Droid repository is ready. Publish the project root with GitHub Pages:\n%s\n' "$REPO_URL"
