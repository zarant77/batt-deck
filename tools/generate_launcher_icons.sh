#!/bin/sh

set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
SOURCE="$ROOT_DIR/icon.png"
RES_DIR="$ROOT_DIR/app/src/main/res"
BACKGROUND="#0057B7"

die() {
    printf 'Error: %s\n' "$*" >&2
    exit 1
}

[ -f "$SOURCE" ] || die "source logo not found: $SOURCE"

if ! command -v magick >/dev/null 2>&1; then
    die "ImageMagick is not installed. On macOS, install it with: brew install imagemagick"
fi

generate_icon() {
    density=$1
    legacy_size=$2
    foreground_size=$3
    directory="$RES_DIR/mipmap-$density"
    legacy_inner=$((legacy_size * 84 / 100))
    foreground_inner=$((foreground_size * 66 / 100))

    mkdir -p "$directory"

    magick "$SOURCE" \
        -background none -alpha on \
        -resize "${legacy_inner}x${legacy_inner}" \
        -gravity center -extent "${legacy_size}x${legacy_size}" \
        -strip "PNG32:$directory/ic_launcher.png"

    cp "$directory/ic_launcher.png" "$directory/ic_launcher_round.png"

    magick "$SOURCE" \
        -background none -alpha on \
        -resize "${foreground_inner}x${foreground_inner}" \
        -gravity center -extent "${foreground_size}x${foreground_size}" \
        -strip "PNG32:$directory/ic_launcher_foreground.png"
}

printf 'Generating launcher icons from %s...\n' "$SOURCE"

generate_icon mdpi 48 108
generate_icon hdpi 72 162
generate_icon xhdpi 96 216
generate_icon xxhdpi 144 324
generate_icon xxxhdpi 192 432

mkdir -p "$RES_DIR/mipmap-anydpi-v26" "$RES_DIR/values"

cat > "$RES_DIR/mipmap-anydpi-v26/ic_launcher.xml" <<'EOF'
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@mipmap/ic_launcher_foreground" />
</adaptive-icon>
EOF

cat > "$RES_DIR/mipmap-anydpi-v26/ic_launcher_round.xml" <<'EOF'
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@mipmap/ic_launcher_foreground" />
</adaptive-icon>
EOF

cat > "$RES_DIR/values/ic_launcher_background.xml" <<EOF
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_launcher_background">$BACKGROUND</color>
</resources>
EOF

printf 'Launcher icons generated in %s.\n' "$RES_DIR"
