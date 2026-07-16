#!/bin/sh

set -eu

APP_ID="com.catemup.battdeck"
MAIN_ACTIVITY=".MainActivity"
ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLEW="$ROOT_DIR/gradlew"
SOURCE_APK="$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk"
APK="$ROOT_DIR/build/BattDeck-debug.apk"
SOURCE_RELEASE_APK="$ROOT_DIR/app/build/outputs/apk/release/app-release.apk"
SOURCE_RELEASE_AAB="$ROOT_DIR/app/build/outputs/bundle/release/app-release.aab"
RELEASE_APK="$ROOT_DIR/build/BattDeck-release.apk"
RELEASE_AAB="$ROOT_DIR/build/BattDeck-release.aab"

if [ -t 1 ]; then
    BOLD='\033[1m'
    GREEN='\033[32m'
    YELLOW='\033[33m'
    RED='\033[31m'
    RESET='\033[0m'
else
    BOLD=''
    GREEN=''
    YELLOW=''
    RED=''
    RESET=''
fi

info() { printf "%b%s%b\n" "$GREEN" "$*" "$RESET"; }
warn() { printf "%b%s%b\n" "$YELLOW" "$*" "$RESET" >&2; }
die() { printf "%bError: %s%b\n" "$RED" "$*" "$RESET" >&2; exit 1; }

need_gradle() {
    [ -x "$GRADLEW" ] || die "executable gradlew was not found in the project root"
}

find_adb() {
    if command -v adb >/dev/null 2>&1; then
        command -v adb
    elif [ -n "${ANDROID_SDK_ROOT:-}" ] && [ -x "$ANDROID_SDK_ROOT/platform-tools/adb" ]; then
        printf '%s\n' "$ANDROID_SDK_ROOT/platform-tools/adb"
    elif [ -n "${ANDROID_HOME:-}" ] && [ -x "$ANDROID_HOME/platform-tools/adb" ]; then
        printf '%s\n' "$ANDROID_HOME/platform-tools/adb"
    elif [ -n "${HOME:-}" ] && [ -x "$HOME/Library/Android/sdk/platform-tools/adb" ]; then
        printf '%s\n' "$HOME/Library/Android/sdk/platform-tools/adb"
    elif [ -n "${HOME:-}" ] && [ -x "$HOME/Android/Sdk/platform-tools/adb" ]; then
        printf '%s\n' "$HOME/Android/Sdk/platform-tools/adb"
    else
        return 1
    fi
}

need_adb() {
    ADB=$(find_adb) || die "adb was not found. Add Android SDK platform-tools to PATH"
}

connected_devices() {
    need_adb
    "$ADB" devices | awk 'NR > 1 && $2 == "device" { print $1 }'
}

select_device() {
    need_adb
    if [ -n "${ANDROID_SERIAL:-}" ]; then
        DEVICE=$ANDROID_SERIAL
        return
    fi

    DEVICES=$(connected_devices)
    COUNT=$(printf '%s\n' "$DEVICES" | awk 'NF { count++ } END { print count+0 }')
    [ "$COUNT" -gt 0 ] || die "no connected and authorized phone or emulator was found"

    if [ "$COUNT" -eq 1 ]; then
        DEVICE=$DEVICES
        return
    fi

    if [ ! -t 0 ]; then
        die "multiple devices are connected; set ANDROID_SERIAL"
    fi

    printf 'Select a device:\n'
    printf '%s\n' "$DEVICES" | awk '{ printf "  %d) %s\n", NR, $0 }'
    printf '> '
    read -r CHOICE
    DEVICE=$(printf '%s\n' "$DEVICES" | sed -n "${CHOICE}p")
    [ -n "$DEVICE" ] || die "invalid device number"
}

gradle() {
    need_gradle
    (cd "$ROOT_DIR" && "$GRADLEW" "$@")
}

doctor() {
    printf '%bBattDeck - environment check%b\n' "$BOLD" "$RESET"
    printf 'Java: '
    if command -v java >/dev/null 2>&1; then
        java -version 2>&1 | awk 'NR == 1 { print; exit }'
    else
        printf '%bnot found (JDK 17 is required)%b\n' "$RED" "$RESET"
    fi
    printf 'Gradle wrapper: %s\n' "$([ -x "$GRADLEW" ] && printf 'OK' || printf 'not ready')"
    SDK_PATH=${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}
    if [ -z "$SDK_PATH" ] && [ -n "${HOME:-}" ] && [ -d "$HOME/Library/Android/sdk" ]; then
        SDK_PATH="$HOME/Library/Android/sdk"
    elif [ -z "$SDK_PATH" ] && [ -n "${HOME:-}" ] && [ -d "$HOME/Android/Sdk" ]; then
        SDK_PATH="$HOME/Android/Sdk"
    fi
    printf 'Android SDK: %s\n' "${SDK_PATH:-not found}"
    printf 'adb: '
    if ADB_PATH=$(find_adb); then
        printf '%s\n' "$ADB_PATH"
        "$ADB_PATH" devices -l
    else
        printf '%bnot found%b\n' "$RED" "$RESET"
    fi
}

devices() {
    need_adb
    "$ADB" devices -l
}

build() {
    info "Building debug APK..."
    gradle assembleDebug
    [ -f "$SOURCE_APK" ] || die "Gradle finished without producing an APK"
    mkdir -p "$ROOT_DIR/build"
    cp "$SOURCE_APK" "$APK"
    info "Build ready: $APK"
}

build_release() {
    [ -f "$ROOT_DIR/keystore.properties" ] || die "keystore.properties is missing. Copy keystore.properties.example and fill in the signing credentials"
    [ -f "$ROOT_DIR/keystore/battdeck-upload.jks" ] || die "keystore/battdeck-upload.jks was not found"
    command -v keytool >/dev/null 2>&1 || die "keytool was not found. Use the JDK 17 installation configured for Android builds"
    STORE_PASSWORD=$(sed -n 's/^storePassword=//p' "$ROOT_DIR/keystore.properties")
    SIGNING_KEY_ALIAS=$(sed -n 's/^keyAlias=//p' "$ROOT_DIR/keystore.properties")
    [ -n "$STORE_PASSWORD" ] || die "storePassword is empty in keystore.properties"
    [ -n "$SIGNING_KEY_ALIAS" ] || die "keyAlias is empty in keystore.properties"
    keytool -list -keystore "$ROOT_DIR/keystore/battdeck-upload.jks" -storepass "$STORE_PASSWORD" >/dev/null 2>&1 ||
        die "storePassword in keystore.properties is not the password used to create battdeck-upload.jks"
    keytool -list -keystore "$ROOT_DIR/keystore/battdeck-upload.jks" -storepass "$STORE_PASSWORD" -alias "$SIGNING_KEY_ALIAS" >/dev/null 2>&1 ||
        die "keyAlias '$SIGNING_KEY_ALIAS' was not found in battdeck-upload.jks"
    info "Building release APK and AAB..."
    gradle assembleRelease bundleRelease
    [ -f "$SOURCE_RELEASE_APK" ] || die "Gradle finished without producing a release APK"
    [ -f "$SOURCE_RELEASE_AAB" ] || die "Gradle finished without producing a release AAB"
    mkdir -p "$ROOT_DIR/build"
    cp "$SOURCE_RELEASE_APK" "$RELEASE_APK"
    cp "$SOURCE_RELEASE_AAB" "$RELEASE_AAB"
    info "Release APK ready: $RELEASE_APK"
    info "Release AAB ready: $RELEASE_AAB"
    info "Release artifacts are signed with battdeck-upload.jks"
}

install_app() {
    [ -f "$APK" ] || build
    select_device
    info "Installing the app on ${DEVICE}..."
    "$ADB" -s "$DEVICE" install -r "$APK"
}

build_run() {
    build
    install_app
    "$ADB" -s "$DEVICE" shell am start -n "$APP_ID/$MAIN_ACTIVITY"
    info "BattDeck is running on ${DEVICE}"
}

logs() {
    select_device
    PID=$("$ADB" -s "$DEVICE" shell pidof "$APP_ID" 2>/dev/null || true)
    [ -n "$PID" ] || die "BattDeck is not currently running"
    info "BattDeck logs (press Ctrl+C to stop)..."
    "$ADB" -s "$DEVICE" logcat --pid="$PID"
}

uninstall_app() {
    select_device
    warn "Uninstalling BattDeck and its local data from ${DEVICE}..."
    "$ADB" -s "$DEVICE" uninstall "$APP_ID"
}

clean() {
    info "Cleaning build outputs..."
    gradle clean
    info "Clean complete"
}

deep_clean() {
    clean
    warn "Removing the local project Gradle cache and build directories..."
    rm -rf -- "$ROOT_DIR/.gradle" "$ROOT_DIR/build" "$ROOT_DIR/app/build"
    info "Deep clean complete"
}

usage() {
    cat <<EOF
Usage: ./runner.sh [command]

  menu         open the interactive menu (default)
  doctor       check Java, Android SDK, adb, and connected devices
  devices      list connected devices
  build        build the debug APK and copy it to build/
  release      build the release APK and AAB and copy them to build/
  icons        generate Android launcher icons from logo.png
  test         run unit tests
  lint         run Android lint
  install      install the exported APK (build it first if missing)
  build-run    build, install, and start the app on a device
  logs         stream logs from the running app
  uninstall    uninstall the app and its data from the device
  clean        run the standard Gradle clean task
  deep-clean   also remove build/ and the local .gradle/ cache
  help         show this help

If multiple devices are connected, the runner will ask you to select one.
For automation, set ANDROID_SERIAL=<serial number>.
EOF
}

menu() {
    while :; do
        printf '\n%bBattDeck runner%b\n' "$BOLD" "$RESET"
        printf '  1) Build, install, and run\n'
        printf '  2) Build APK + AAB\n'
        printf '  3) Run tests\n'
        printf '  4) Android lint\n'
        printf '  5) Connected devices\n'
        printf '  6) App logs\n'
        printf '  7) Clean project\n'
        printf '  8) Deep clean\n'
        printf '  9) Check environment\n'
        printf ' 10) Generate launcher icons\n'
        printf '  0) Exit\n> '
        read -r CHOICE || exit 0
        case "$CHOICE" in
            1) build_run ;;
            2) build_release ;;
            3) gradle test ;;
            4) gradle lintDebug ;;
            5) devices ;;
            6) logs ;;
            7) clean ;;
            8) deep_clean ;;
            9) doctor ;;
            10) "$ROOT_DIR/tools/generate_launcher_icons.sh" ;;
            0) exit 0 ;;
            *) warn "Select an option from 0 to 10" ;;
        esac
    done
}

COMMAND=${1:-menu}
case "$COMMAND" in
    menu) menu ;;
    doctor) doctor ;;
    devices) devices ;;
    build) build ;;
    release) build_release ;;
    icons) "$ROOT_DIR/tools/generate_launcher_icons.sh" ;;
    test) gradle test ;;
    lint) gradle lintDebug ;;
    install) install_app ;;
    build-run) build_run ;;
    logs) logs ;;
    uninstall) uninstall_app ;;
    clean) clean ;;
    deep-clean) deep_clean ;;
    help|-h|--help) usage ;;
    *) usage >&2; die "unknown command: $COMMAND" ;;
esac
