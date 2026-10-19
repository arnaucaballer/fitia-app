# Fitia

App Android de entreno y alimentación con asistente de IA.

## Requisitos

- JDK 17
- Android SDK (compileSdk 35)
- Dispositivo o emulador Android 8.0+ (API 26)

## Abrir el proyecto

Abre esta carpeta en Android Studio (Ladybug o superior) o usa Gradle desde la terminal:

```bash
./gradlew :app:assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`. Cópialo al teléfono e instálalo (origenes desconocidos activados).

## Clave de IA

No hace falta en este bloque. Cuando el chat esté listo, la clave de OpenRouter se pone en **Ajustes → Clave de API** dentro de la app. No se guarda en el repositorio.

## Estructura

Ver `AGENTS.md` para reglas de arquitectura, ramas y estilo.

## Estado

Bloque actual: base de la app (tema, navegación Entreno / Chat / Alimentación).
