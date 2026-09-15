#!/bin/sh
set -eu
DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
VERSION=8.9
BASE="$DIR/.gradle-local"
HOME_GRADLE="$BASE/gradle-$VERSION"
ZIP="$BASE/gradle-$VERSION-bin.zip"
mkdir -p "$BASE"
if [ ! -x "$HOME_GRADLE/bin/gradle" ]; then
  if [ ! -f "$ZIP" ]; then
    URL="https://services.gradle.org/distributions/gradle-$VERSION-bin.zip"
    if command -v curl >/dev/null 2>&1; then
      curl -fL "$URL" -o "$ZIP"
    elif command -v wget >/dev/null 2>&1; then
      wget -O "$ZIP" "$URL"
    else
      echo "Gradle is not installed and neither curl nor wget is available." >&2
      exit 1
    fi
  fi
  command -v unzip >/dev/null 2>&1 || { echo "unzip is required to bootstrap Gradle." >&2; exit 1; }
  unzip -q -o "$ZIP" -d "$BASE"
fi
exec "$HOME_GRADLE/bin/gradle" "$@"
