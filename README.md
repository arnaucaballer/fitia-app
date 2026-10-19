# Fitia

App Android de entreno y alimentación con asistente de IA.

## Requisitos

- JDK 17
- Android SDK (compileSdk 35)
- Dispositivo o emulador Android 8.0+ (API 26)

## Abrir el proyecto

Abre esta carpeta en Android Studio (Ladybug o superior) o usa Gradle desde la terminal:

```bash
export JAVA_HOME=/ruta/a/jdk-17
export ANDROID_HOME=/ruta/a/Android/Sdk
printf 'sdk.dir=%s\n' "$ANDROID_HOME" > local.properties
./gradlew :app:assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`. Cópialo al teléfono e instálalo (orígenes desconocidos activados).

## Clave de IA

En la app: **Chat → icono de Ajustes → Clave de API**.
Proveedor por defecto: OpenRouter. Modelo inicial: `google/gemini-2.5-flash`.
La clave se guarda cifrada en el teléfono. No va al repositorio.

## Estructura

Ver `AGENTS.md` para reglas de arquitectura, ramas y estilo.

## Estado

Entreno, alimentación y chat con tools listos en `main`. APK debug generable con el comando de arriba.
