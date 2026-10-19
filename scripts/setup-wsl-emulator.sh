#!/usr/bin/env bash
# One-time setup so the Android emulator can run under WSL2.
# Needs sudo for KVM group membership and a few system libraries.
set -euo pipefail

echo "==> Paquetes de sistema para el emulador…"
sudo apt-get update -qq
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y -qq \
  libpulse0 \
  libnss3 \
  libtcmalloc-minimal4t64 \
  libsndfile1 \
  libxkbfile1 \
  libasyncns0 \
  libogg0 \
  unzip \
  util-linux-extra \
  >/dev/null

if [[ -e /dev/kvm ]]; then
  echo "==> Añadiendo a $USER al grupo kvm…"
  sudo groupadd -r kvm 2>/dev/null || true
  sudo gpasswd -a "$USER" kvm
  # En WSL el grupo no se aplica hasta reiniciar la distro; 666 permite usar el
  # emulador en la misma sesión sin newgrp/sg.
  sudo chown root:kvm /dev/kvm
  sudo chmod 666 /dev/kvm
  echo
  echo "Listo. Ya puedes lanzar:"
  echo "  ./scripts/run-fitia.sh"
  echo
  echo "Si más adelante /dev/kvm vuelve a 660, ejecuta de nuevo este script"
  echo "o reinicia WSL desde Windows:  wsl --shutdown"
else
  echo "No hay /dev/kvm en este WSL. Activa virtualización anidada en .wslconfig o usa un teléfono con adb." >&2
  exit 1
fi
