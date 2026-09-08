# MVI KMP Example

A Kotlin Multiplatform (Android + iOS) example app demonstrating the **Model-View-Intent (MVI)** architecture pattern with Jetpack Compose.

The app shows a list of **operaciones** fetched from a backend HTTP (**MviKmpServerExample**) and cached in a local database:

- **Ktor** → `GET http://<host>:8080/operaciones` (own server)
- **SQLDelight** → operaciones are persisted in a local SQLite database and served as offline cache

## Tech stack

| Layer          | Technology                                        |
|----------------|---------------------------------------------------|
| UI             | Compose Multiplatform (Material 3)                |
| Language       | Kotlin 2.1 / Kotlin Multiplatform                 |
| Networking     | Ktor 3 (OkHttp on Android, Darwin on iOS)         |
| Database       | SQLDelight 2 (SQLite)                             |
| Architecture   | MVI (State / Intent / Effect)                     |

## Requirements

- JDK 17+
- Android SDK (API 35)
- Xcode 15+ (for the iOS app)
- **MviKmpServerExample** running on `http://localhost:8080`

## Architecture

```
UI (Compose) ──onIntent()──▶ ViewModel ──▶ Repository ──▶ Server (Ktor/Exposed/SQLite)
     ▲                             │                          │
     │                        MVI flow                        ▼
     └── state (StateFlow) ◀───────┘                    Database (SQLDelight)
```

### MVI contract

Every screen exposes three types (`composeApp/.../mvi`):

| Type   | Purpose                                              |
|--------|------------------------------------------------------|
| State  | Immutable UI state rendered by the screen             |
| Intent | A user/system action dispatched via `viewModel.onIntent()` |
| Effect | One-shot events (e.g. snackbar messages) consumed by the UI |

`MviViewModel` (base class) owns a `StateFlow<State>` and a `Channel` of `Effect`s.

## Modules

```
MviKmpExample/
├── composeApp/   Compose Multiplatform UI, MVI core, feature ViewModels, entry points
├── shared/       Data layer: Ktor client, SQLDelight database, repositories
└── iosApp/       Xcode project for the iOS entry point
```

## Server base URL

| Platform             | URL                                    |
|----------------------|----------------------------------------|
| Android emulator     | `http://10.0.2.2:8080`                 |
| Android física (USB) | `http://localhost:8080` (vía `adb reverse`) |
| iOS simulator        | `http://localhost:8080`                |

Defined per platform in `shared/src/*/data/network/HttpClientFactory.*.kt` (`apiBaseUrl()`).

- `apiBaseUrl()` (Android) detects emulator vs physical device: emulators use `10.0.2.2`, physical devices use `localhost:8080`.
- **Physical device**: the server is reached over USB — run `adb reverse tcp:8080 tcp:8080` after connecting the phone (re-run it if the device reconnects). No Wi-Fi/firewall setup needed.
- Debug APKs are **test-only**: install with `adb install -t -r composeApp/build/outputs/apk/debug/composeApp-debug.apk` (use `outputs/`, not `intermediates/`).

## License

This project is licensed under the [MIT License](LICENSE).

## Run

### Server
```
git clone .../MviKmpServerExample   # project aparte
cd MviKmpServerExample && ./gradlew run
```

### Android
```
./gradlew :composeApp:assembleDebug
```
or open the project in Android Studio and run the `composeApp` configuration.

### iOS
Open `iosApp/iosApp.xcodeproj` in Xcode, select a simulator, and run.
The `ComposeApp` Kotlin framework is built automatically by the `Compile Kotlin Framework` build phase.

> Nota: si ya habías instalado la app con el esquema anterior (`posts`), desinstala o borra los datos de la app para recrear la base de datos (`operaciones`).
