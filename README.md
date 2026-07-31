# MVI KMP Example

A Kotlin Multiplatform (Android + iOS) example app demonstrating the **Model-View-Intent (MVI)** architecture pattern with Jetpack Compose.

The app shows a list of posts fetched from the network and cached in a local database:

- **Ktor** → `GET https://jsonplaceholder.typicode.com/posts`
- **SQLDelight** → posts are persisted in a local SQLite database and served as offline cache

## Tech stack

| Layer    | Technology                                        |
|----------|---------------------------------------------------|
| UI       | Compose Multiplatform (Material 3)                |
| Language | Kotlin 2.1 / Kotlin Multiplatform                 |
| Networking | Ktor 3 (OkHttp on Android, Darwin on iOS)       |
| Database | SQLDelight 2 (SQLite)                             |
| Architecture | MVI (State / Intent / Effect)                 |

## Requirements

- JDK 17+
- Android SDK (API 35)
- Xcode 15+ (for the iOS app)

## Architecture

```
UI (Compose) ──onIntent()──▶ ViewModel ──▶ Repository ──▶ Network (Ktor)
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

## License

This project is licensed under the [MIT License](LICENSE).

## Run

### Android
```
./gradlew :composeApp:assembleDebug
```
or open the project in Android Studio and run the `composeApp` configuration.

### iOS
Open `iosApp/iosApp.xcodeproj` in Xcode, select a simulator, and run.
The `ComposeApp` Kotlin framework is built automatically by the `Compile Kotlin Framework` build phase.
