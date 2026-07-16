#!/bin/sh

set -eu

APP_ID="com.catemup.battdeck"
MAIN_ACTIVITY=".MainActivity"
ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLEW="$ROOT_DIR/gradlew"
APK="$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk"

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
die() { printf "%bПомилка: %s%b\n" "$RED" "$*" "$RESET" >&2; exit 1; }

need_gradle() {
    [ -x "$GRADLEW" ] || die "не знайдено виконуваний gradlew у корені проєкту"
}

find_adb() {
    if command -v adb >/dev/null 2>&1; then
        command -v adb
    elif [ -n "${ANDROID_SDK_ROOT:-}" ] && [ -x "$ANDROID_SDK_ROOT/platform-tools/adb" ]; then
        printf '%s\n' "$ANDROID_SDK_ROOT/platform-tools/adb"
    elif [ -n "${ANDROID_HOME:-}" ] && [ -x "$ANDROID_HOME/platform-tools/adb" ]; then
        printf '%s\n' "$ANDROID_HOME/platform-tools/adb"
    else
        return 1
    fi
}

need_adb() {
    ADB=$(find_adb) || die "adb не знайдено. Додайте Android SDK platform-tools у PATH"
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
    [ "$COUNT" -gt 0 ] || die "немає підключеного та авторизованого телефона або емулятора"

    if [ "$COUNT" -eq 1 ]; then
        DEVICE=$DEVICES
        return
    fi

    if [ ! -t 0 ]; then
        die "підключено кілька пристроїв; задайте ANDROID_SERIAL"
    fi

    printf 'Оберіть пристрій:\n'
    printf '%s\n' "$DEVICES" | awk '{ printf "  %d) %s\n", NR, $0 }'
    printf '> '
    read -r CHOICE
    DEVICE=$(printf '%s\n' "$DEVICES" | sed -n "${CHOICE}p")
    [ -n "$DEVICE" ] || die "невірний номер пристрою"
}

gradle() {
    need_gradle
    (cd "$ROOT_DIR" && "$GRADLEW" "$@")
}

doctor() {
    printf '%bBattDeck — перевірка середовища%b\n' "$BOLD" "$RESET"
    printf 'Java: '
    if command -v java >/dev/null 2>&1; then
        java -version 2>&1 | awk 'NR == 1 { print; exit }'
    else
        printf '%bне знайдено (потрібен JDK 17)%b\n' "$RED" "$RESET"
    fi
    printf 'Gradle wrapper: %s\n' "$([ -x "$GRADLEW" ] && printf 'OK' || printf 'не готовий')"
    printf 'Android SDK: %s\n' "${ANDROID_SDK_ROOT:-${ANDROID_HOME:-не задано}}"
    printf 'adb: '
    if ADB_PATH=$(find_adb); then
        printf '%s\n' "$ADB_PATH"
        "$ADB_PATH" devices -l
    else
        printf '%bне знайдено%b\n' "$RED" "$RESET"
    fi
}

devices() {
    need_adb
    "$ADB" devices -l
}

build() {
    info "Збираю debug APK…"
    gradle assembleDebug
    [ -f "$APK" ] || die "Gradle завершився без APK"
    info "Готово: $APK"
}

install_app() {
    [ -f "$APK" ] || build
    select_device
    info "Встановлюю застосунок на $DEVICE…"
    "$ADB" -s "$DEVICE" install -r "$APK"
}

run_app() {
    select_device
    info "Запускаю BattDeck на $DEVICE…"
    "$ADB" -s "$DEVICE" shell am start -n "$APP_ID/$MAIN_ACTIVITY"
}

build_run() {
    build
    install_app
    "$ADB" -s "$DEVICE" shell am start -n "$APP_ID/$MAIN_ACTIVITY"
    info "BattDeck запущено на $DEVICE"
}

logs() {
    select_device
    PID=$("$ADB" -s "$DEVICE" shell pidof "$APP_ID" 2>/dev/null || true)
    [ -n "$PID" ] || die "BattDeck зараз не запущено"
    info "Логи BattDeck (Ctrl+C для виходу)…"
    "$ADB" -s "$DEVICE" logcat --pid="$PID"
}

uninstall_app() {
    select_device
    warn "Видаляю BattDeck разом із локальними даними з $DEVICE…"
    "$ADB" -s "$DEVICE" uninstall "$APP_ID"
}

clean() {
    info "Очищаю результати збірки…"
    gradle clean
    info "Готово"
}

deep_clean() {
    clean
    warn "Видаляю локальний Gradle-кеш проєкту та каталоги build…"
    rm -rf -- "$ROOT_DIR/.gradle" "$ROOT_DIR/build" "$ROOT_DIR/app/build"
    info "Глибоке очищення завершено"
}

usage() {
    cat <<EOF
Використання: ./runner.sh [команда]

  menu         інтерактивне меню (за замовчуванням)
  doctor       перевірити Java, Android SDK, adb і пристрої
  devices      показати підключені пристрої
  build        зібрати debug APK
  test         запустити unit-тести
  lint         перевірити Android lint
  install      встановити готовий APK (або спочатку зібрати його)
  run          запустити вже встановлений застосунок
  build-run    зібрати, встановити й запустити на телефоні
  logs         показувати логи запущеного застосунку
  uninstall    видалити застосунок і його дані з телефона
  clean        стандартне очищення через Gradle
  deep-clean   також видалити build/ і локальний .gradle/
  help         показати цю довідку

Якщо підключено кілька пристроїв, скрипт запропонує вибір.
Для автоматизації можна задати ANDROID_SERIAL=<серійний номер>.
EOF
}

menu() {
    while :; do
        printf '\n%bBattDeck runner%b\n' "$BOLD" "$RESET"
        printf '  1) Зібрати, встановити й запустити\n'
        printf '  2) Зібрати APK\n'
        printf '  3) Запустити застосунок\n'
        printf '  4) Тести\n'
        printf '  5) Android lint\n'
        printf '  6) Підключені пристрої\n'
        printf '  7) Логи застосунку\n'
        printf '  8) Очистити проєкт\n'
        printf '  9) Глибоке очищення\n'
        printf ' 10) Перевірити середовище\n'
        printf '  0) Вийти\n> '
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
            *) warn "Оберіть пункт від 0 до 10" ;;
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
    *) usage >&2; die "невідома команда: $COMMAND" ;;
esac
