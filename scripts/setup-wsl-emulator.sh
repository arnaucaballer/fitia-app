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
  >/dev/null

if [[ -e /dev/kvm ]]; then
  echo "==> Añadiendo a $USER al grupo kvm…"
  sudo groupadd -r kvm 2>/dev/null || true
  sudo gpasswd -a "$USER" kvm
  sudo chown root:kvm /dev/kvm
  sudo chmod 660 /dev/kvm
  echo
  echo "Listo. Cierra esta terminal (o ejecuta: newgrp kvm) y luego:"
  echo "  ./scripts/run-fitia.sh"
else
  echo "No hay /dev/kvm en este WSL. Activa virtualización anidada en .wslconfig o usa un teléfono con adb." >&2
  exit 1
fi
