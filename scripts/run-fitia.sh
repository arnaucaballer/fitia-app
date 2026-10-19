#!/usr/bin/env bash
# Build (if needed), start the Fitia emulator, install the debug APK, and launch the app.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=env.sh
source "$SCRIPT_DIR/env.sh"

ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$ROOT_DIR"

if [[ ! -f local.properties ]]; then
  printf 'sdk.dir=%s\n' "$ANDROID_HOME" > local.properties
  echo "Creado local.properties → sdk.dir=$ANDROID_HOME"
fi

echo "==> Compilando APK debug…"
./gradlew :app:assembleDebug --quiet

if [[ ! -f "$FITIA_APK" ]]; then
  echo "No está el APK en $FITIA_APK" >&2
  exit 1
fi

device_ready() {
  adb devices | awk 'NR>1 && $2=="device" {print $1; exit}'
}

boot_completed() {
  local serial="$1"
  adb -s "$serial" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' | grep -qx "1"
}

SERIAL="$(device_ready || true)"
if [[ -z "${SERIAL}" ]]; then
  if [[ ! -e /dev/kvm ]]; then
    echo "No hay /dev/kvm. En WSL2 hace falta virtualización anidada, o conecta un teléfono con adb." >&2
    exit 1
  fi
  if [[ ! -r /dev/kvm || ! -w /dev/kvm ]]; then
    echo "Sin permiso sobre /dev/kvm." >&2
    echo "Ejecuta:  ./scripts/setup-wsl-emulator.sh" >&2
    echo "Luego:    ./scripts/run-fitia.sh" >&2
    echo "Alternativa: teléfono con depuración USB + adb." >&2
    exit 1
  fi

  if ! avdmanager list avd 2>/dev/null | grep -q "Name: $FITIA_AVD"; then
    echo "==> Creando AVD $FITIA_AVD…"
    echo no | avdmanager create avd \
      -n "$FITIA_AVD" \
      -k "system-images;android-34;google_apis;x86_64" \
      -d pixel_6 \
      --force
  fi

  echo "==> Arrancando emulador $FITIA_AVD (ventana WSLg)…"
  # -no-audio avoids PulseAudio issues on minimal WSL images.
  nohup emulator -avd "$FITIA_AVD" -netdelay none -netspeed full -gpu host -no-audio \
    >"$ROOT_DIR/.emulator.log" 2>&1 &
  echo $! >"$ROOT_DIR/.emulator.pid"
  echo "    log: $ROOT_DIR/.emulator.log"

  echo "==> Esperando dispositivo…"
  adb wait-for-device
  for _ in $(seq 1 90); do
    SERIAL="$(device_ready || true)"
    if [[ -n "${SERIAL}" ]] && boot_completed "$SERIAL"; then
      break
    fi
    sleep 2
  done
  SERIAL="$(device_ready || true)"
  if [[ -z "${SERIAL}" ]]; then
    echo "El emulador no quedó listo. Mira .emulator.log" >&2
    exit 1
  fi
else
  echo "==> Usando dispositivo ya conectado: $SERIAL"
fi

echo "==> Instalando Fitia…"
adb -s "$SERIAL" install -r "$FITIA_APK"

echo "==> Abriendo Fitia…"
adb -s "$SERIAL" shell am start -n "$FITIA_PACKAGE/.MainActivity"

echo "Listo. Emulador: $SERIAL"
echo "Para parar el emulador: adb -s $SERIAL emu kill   (o scripts/stop-emulator.sh)"
