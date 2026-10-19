#!/usr/bin/env bash
# Shared Android / JDK paths for Fitia local runs (WSL or Linux).

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

export JAVA_HOME="${JAVA_HOME:-$HOME/.local/jdk/jdk-17.0.20.1+1}"
if [[ ! -x "$JAVA_HOME/bin/java" ]]; then
  # Fallback: first jdk-17* under ~/.local/jdk
  JAVA_HOME="$(ls -d "$HOME"/.local/jdk/jdk-17* 2>/dev/null | head -1 || true)"
fi

export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools:$PATH"

export FITIA_AVD="${FITIA_AVD:-Fitia_API34}"
export FITIA_PACKAGE="${FITIA_PACKAGE:-com.arnaucaballer.fitia}"
export FITIA_APK="${FITIA_APK:-$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk}"

if [[ ! -x "$JAVA_HOME/bin/java" ]]; then
  echo "JDK 17 no encontrado. Instálalo o exporta JAVA_HOME." >&2
  exit 1
fi
if [[ ! -d "$ANDROID_HOME" ]]; then
  echo "Android SDK no encontrado en $ANDROID_HOME. Exporta ANDROID_HOME." >&2
  exit 1
fi

# Qt / X11 extras for the emulator window on WSL (local extract and/or apt).
EXTRA_LIBS=()
if [[ -d "$HOME/.local/android-libs" ]]; then
  EXTRA_LIBS+=("$HOME/.local/android-libs")
fi
if [[ -d "$ANDROID_HOME/emulator/lib64" ]]; then
  EXTRA_LIBS+=("$ANDROID_HOME/emulator/lib64" "$ANDROID_HOME/emulator/lib64/qt/lib")
fi
if ((${#EXTRA_LIBS[@]})); then
  joined="$(IFS=:; echo "${EXTRA_LIBS[*]}")"
  export LD_LIBRARY_PATH="${joined}${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}"
fi
export QT_QPA_PLATFORM="${QT_QPA_PLATFORM:-xcb}"
