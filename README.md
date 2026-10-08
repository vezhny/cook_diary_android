# Cook Diary

A small Android app that answers the everyday question **“What should I cook?”**

Keep a list of the dishes you usually make, mark what you cooked, and when you can't decide, open a list where each dish is colored by how recently you cooked it.

<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="160" alt="Cook Diary icon">
</p>

## Features

- **What to cook.** Tap the button to see all your dishes, coldest first:
  - Dishes cooked today are red. The color fades day by day until the dish fully “cools down” after 14 days.
  - Order: days since last cooked (longest ago first, never-cooked at the top), then tags, then name.
  - Filter by tags. A dish must have *all* selected tags to show up.
  - Tap a dish and confirm “Did you cook …?” to log it in the history.
- **Dishes.** Add, edit, and delete dishes with a name, comma-separated tags, and a note. Deleting asks for confirmation and also removes the dish's history.
- **History.** Cooked dishes grouped by day, newest first. A wrong entry can be removed.
- **Import / export.** Save the dish list to a JSON file or load one through the system file picker. On import, dishes whose name is already in the list are skipped, ignoring case.
- **Theme.** Light, dark, or system default, in a palette taken from the app icon.
- **Language.** English or Russian, or the system default. On Android 13+ the language can also be set in the system's per-app language settings. Only the interface is translated: dish names, tags, and notes stay exactly as you typed them.

## Tech stack

| Area | Library |
|---|---|
| Language | Kotlin 2.4 |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM: `ViewModel` + `StateFlow` |
| Database | Room (KSP), schema exported to `app/schemas/` |
| DI | Koin |
| Navigation | Navigation Compose with type-safe routes (kotlinx.serialization) |
| Serialization | kotlinx.serialization JSON (import/export) |
| Localization | Android resources + AppCompat per-app locales |
| Tests | JUnit 4, Robolectric (Room and Android APIs on the JVM), kotlinx-coroutines-test |

SDK: `minSdk 26`, `targetSdk 37`. Build: Gradle 9.8 wrapper, AGP 9.4, version catalog in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

## Project structure

```
app/src/main/java/com/vezhny/cookdiary/
├── data/        Room entities and DAOs, repositories, settings, language, file I/O
├── domain/      Pure logic: dish “heat”, ordering and filtering, JSON import/export, tag normalization
├── di/          Koin module
└── ui/          Compose screens and ViewModels
    ├── home/       “What to cook” and the dish chooser
    ├── dishes/     Dish list, edit form, import/export
    ├── history/    Cooking history
    ├── settings/   Theme and language dialog
    └── theme/      Color schemes
```

### Data model

Two tables:

- `dish`: `id`, `name`, `tags` (comma-separated, lowercase), `note`.
- `cook_event`: `id`, `dishId`, `cookedAt` (epoch millis). Rows are deleted together with their dish.

“When was this last cooked” is simply `MAX(cookedAt)` per dish, so the history and the chooser both come from these two tables.

## Getting started

Requirements: Android Studio (or JDK 17+ and the Android SDK with platform 37).

```bash
git clone https://github.com/vezhny/cook_diary_android.git
cd cook_diary_android
./gradlew installDebug
```

Or open the project in Android Studio and run the `app` configuration.

### Common tasks

```bash
./gradlew test             # unit tests, including Room DAO tests via Robolectric
./gradlew lint             # Android lint (warnings are worth fixing, errors fail the build)
./gradlew assembleDebug    # debug APK in app/build/outputs/apk/debug/
```

## Continuous integration

[GitHub Actions](.github/workflows/android.yml) runs `./gradlew lint test assembleDebug` on every push and pull request. Lint and test reports are uploaded as build artifacts.

## Import / export format

```json
{
  "version": 1,
  "dishes": [
    { "name": "Borscht", "tags": ["soup", "lunch"], "note": "With sour cream" },
    { "name": "Omelette", "tags": ["breakfast"] }
  ]
}
```

- `tags` and `note` are optional, and unknown fields are ignored.
- Dishes with a blank name are skipped.
- Tags are trimmed, lowercased, and de-duplicated.
- Files with a newer `version` than the app supports are rejected.
- Cooking history is not included.

## Localization

| Locale | Resources |
|---|---|
| English (default and fallback) | `app/src/main/res/values/strings.xml` |
| Russian | `app/src/main/res/values-ru/strings.xml` |

To add a language:

1. Add `values-<lang>/strings.xml` with all strings, including the `plurals`.
2. Add the locale to [`res/xml/locales_config.xml`](app/src/main/res/xml/locales_config.xml) and to `localeFilters` in [`app/build.gradle.kts`](app/build.gradle.kts).
3. Add an entry to `AppLanguage` and a label in `SettingsDialog`. Language names are written in their own language.

## Troubleshooting

**`Unable to establish loopback connection` when running Gradle on Windows.** This is a JDK issue when the temp directory path is in short 8.3 form (e.g. `C:\Users\ABCDEF~1.XYZ\...`). Point the JDK at a plain directory before running Gradle:

```bash
export JAVA_TOOL_OPTIONS="-Djdk.net.unixdomain.tmpdir=$HOME/.gradle/tmp"
```

In PowerShell:

```powershell
$env:JAVA_TOOL_OPTIONS = "-Djdk.net.unixdomain.tmpdir=$HOME\.gradle\tmp"
```

## Roadmap ideas

- Photos and recipes for dishes
- Full backup including cooking history
- Sync between devices
- Compose UI tests
