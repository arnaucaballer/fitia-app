# Fitia

App Android de entreno y alimentación con asistente de IA.

## Requisitos

- JDK 17
- Android SDK (compileSdk 35)
- Teléfono Android 8.0 o superior (API 26)

## Abrir el proyecto

Abre esta carpeta en Android Studio (Ladybug o superior) o usa Gradle desde la terminal:

```bash
export JAVA_HOME=/ruta/a/jdk-17
export ANDROID_HOME=/ruta/a/Android/Sdk
printf 'sdk.dir=%s\n' "$ANDROID_HOME" > local.properties
./gradlew :app:assembleDebug
```

El APK de entrega queda en `apk/fitia-<versión>.apk` (ahora `apk/fitia-0.1.56.apk`). Al subir `versionName` se genera un archivo nuevo y el anterior se queda. Cópialo al teléfono e instálalo (orígenes desconocidos activados). Este APK usa el identificador `app.fitia`: no sustituye a una instalación anterior con otro identificador.

## Clave de IA

En la app: **Chat → icono de Ajustes → Clave de API**.
Proveedor por defecto: OpenRouter. Modelo inicial: `google/gemini-2.5-flash`.
La clave se guarda cifrada en el teléfono. No va al repositorio, ni a copias de seguridad, ni a las peticiones como dato identificable.

## Publicar

La firma de release lee `keystore.properties` en la raíz (no se sube a git). Copia `keystore.properties.example`, crea el almacén fuera del repositorio y rellena la ruta y las contraseñas en local. Sin ese archivo, `assembleRelease` genera un APK sin firmar.

## Estructura

Ver `AGENTS.md` para reglas de arquitectura, ramas y estilo.

## Estado

Entreno, alimentación y chat con tools listos en `main`. APK debug generable con el comando de arriba.
