#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=env.sh
source "$SCRIPT_DIR/env.sh"

ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

adb devices | awk 'NR>1 && $1 ~ /^emulator-/ && $2=="device" {print $1}' | while read -r serial; do
  echo "Parando $serial…"
  adb -s "$serial" emu kill || true
done

if [[ -f "$ROOT_DIR/.emulator.pid" ]]; then
  pid="$(cat "$ROOT_DIR/.emulator.pid")"
  if kill -0 "$pid" 2>/dev/null; then
    kill "$pid" 2>/dev/null || true
  fi
  rm -f "$ROOT_DIR/.emulator.pid"
fi

echo "Emulador detenido."
