# AGENTS.md — Fitia

Reglas para agentes y personas que toquen este repositorio.

## Qué es Fitia

App Android de entreno y alimentación con un chat que consulta y modifica los mismos datos que la UI. Un solo módulo (`app`), Kotlin, Jetpack Compose, Room en el dispositivo. Sin cuentas ni sincronización en la nube.

## Git

- Rama base: `main`.
- Prefijos de rama, kebab-case:
  - `feat/<ambito>`
  - `fix/<ambito>`
  - `chore/<ambito>`
  - `docs/<ambito>`
- Un bloque de trabajo = una rama = un PR. Push a GitHub; mergear a `main` cuando el bloque esté usable.
- No commits de claves de API, `.env`, ni secretos. La clave vive solo en Ajustes de la app (EncryptedSharedPreferences).

## Arquitectura

```
app/src/main/java/com/arnaucaballer/fitia/
  navigation/       navegación y destinos
  design/           tema, tipografía, color
  core/database/    Room, entidades, DAOs
  core/ai/          cliente Chat Completions, proveedor, bucle de tools
  feature/<nombre>/
    ui/             pantallas Compose + ViewModels
    domain/         casos de uso y modelos de dominio
    data/           repositorios que hablan con Room / red
```

Reglas:

1. Un tipo público principal por archivo (clase, data class, interface, object).
2. La UI Compose solo habla con su ViewModel.
3. El ViewModel solo llama casos de uso (o lee Flows que el dominio/repositorio exponen).
4. Los casos de uso son la única puerta de escritura y lectura de negocio. Las tools del chat y las pantallas usan los mismos casos de uso.
5. Nada de red fuera de `core/ai` y del cliente de Open Food Facts en nutrición.
6. Textos de UI en `res/values/strings.xml` (español).
7. Room con esquema exportado (`exportSchema = true`). Migraciones explícitas; no `fallbackToDestructiveMigration` en builds de entrega.
8. No hardcodear claves, base URLs de secreto ni tokens en el código. Proveedor y clave se configuran en Ajustes.

## UI / UX

- Barra inferior con tres destinos a pantalla completa: Entreno | Chat (centro, inicio) | Alimentación.
- Tema Material 3, Inter empaquetada, acento verde, mucho aire, títulos grandes.
- Interfaz en español. Unidades: kg y cm.
- Foto de comida: estimar con IA, mostrar hoja editable, guardar solo al confirmar.

## IA

- Cliente único compatible con OpenAI Chat Completions: `POST {baseUrl}/chat/completions`.
- Presets: OpenRouter (por defecto) y OpenCode. Clave y modelo editables en Ajustes.
- Tools en local, máximo 5 vueltas por mensaje. Sin streaming en v1.
- Borrar vía tool o UI debe permitir deshacer (snackbar).

## Estilo de código

- Kotlin idiomático. Preferir `data class`, sealed types y Flows.
- Nombres en inglés en código; textos visibles en español.
- Sin lógica de negocio en Composables.
- Tests unitarios para calculadoras y casos de uso críticos (nutrición primero).

## Build y APK

- `applicationId`: `com.arnaucaballer.fitia`
- minSdk 26, compileSdk 35, JDK 17.
- APK de entrega: debug en `app/build/outputs/apk/debug/app-debug.apk`.
- No subir APKs ni carpetas `build/` al repo.
