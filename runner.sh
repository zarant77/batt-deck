#!/bin/sh

set -eu

APP_ID="com.catemup.battdeck"
MAIN_ACTIVITY=".MainActivity"
ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLEW="$ROOT_DIR/gradlew"
SOURCE_APK="$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk"
APK="$ROOT_DIR/build/BattDeck-debug.apk"

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

install_app() {
    [ -f "$APK" ] || build
    select_device
    info "Installing the app on ${DEVICE}..."
    "$ADB" -s "$DEVICE" install -r "$APK"
}

run_app() {
    select_device
    info "Starting BattDeck on ${DEVICE}..."
    "$ADB" -s "$DEVICE" shell am start -n "$APP_ID/$MAIN_ACTIVITY"
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
  test         run unit tests
  lint         run Android lint
  install      install the exported APK (build it first if missing)
  run          start the installed app
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
        printf '  2) Build APK\n'
        printf '  3) Start app\n'
        printf '  4) Run tests\n'
        printf '  5) Android lint\n'
        printf '  6) Connected devices\n'
        printf '  7) App logs\n'
        printf '  8) Clean project\n'
        printf '  9) Deep clean\n'
        printf ' 10) Check environment\n'
        printf '  0) Exit\n> '
        read -r CHOICE || exit 0
        case "$CHOICE" in
            1) build_run ;;
            2) build ;;
            3) run_app ;;
            4) gradle test ;;
            5) gradle lintDebug ;;
            6) devices ;;
            7) logs ;;
            8) clean ;;
            9) deep_clean ;;
            10) doctor ;;
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
    test) gradle test ;;
    lint) gradle lintDebug ;;
    install) install_app ;;
    run) run_app ;;
    build-run) build_run ;;
    logs) logs ;;
    uninstall) uninstall_app ;;
    clean) clean ;;
    deep-clean) deep_clean ;;
    help|-h|--help) usage ;;
    *) usage >&2; die "unknown command: $COMMAND" ;;
esac
