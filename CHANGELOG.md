# Changelog

All notable changes to this project are documented here.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [0.2.0] - 2026-10-08

### Added
- “What to cook” dish chooser:
  - Dishes are colored by how recently they were cooked: red if cooked today, fading out over 14 days.
  - Sorted by days since last cooked (longest ago first), then by tags, then by name.
  - Filter by tags. Dishes must have all selected tags.
  - Confirmation “Did you cook …?” before a dish is logged.
- Import and export of the dish list as JSON. On import, dishes whose name already exists are skipped.
- Settings dialog:
  - Theme: system default, light, or dark.
  - Language: system default, English, or Russian.
  - App version.
- English translation. English is now the default UI language, with Russian also available. User data is never translated.
- App icon (adaptive, with a monochrome layer for themed icons).
- Brand color palette for light and dark themes.
- Empty states with actions on every screen.
- Pencil icon on dishes to make editing discoverable.
- Confirmation before deleting a dish.

### Changed
- The dish list is a plain catalog: name, tags, and note. Dishes are marked as cooked only from the chooser.

### Removed
- Random dish suggestion, replaced by the chooser list.

## [0.1.0] - 2026-10-08

### Added
- Project setup:
  - Kotlin, Jetpack Compose, Room, Koin, Navigation Compose.
  - Version catalog and GitHub Actions CI.
- Dishes with name, tags, and note: add, edit, delete.
- Cooking history grouped by date.
- Random “What should I cook?” suggestion, weighted toward dishes not cooked recently.

[Unreleased]: https://github.com/vezhny/cook_diary_android/compare/v0.2.0...HEAD
[0.2.0]: https://github.com/vezhny/cook_diary_android/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/vezhny/cook_diary_android/releases/tag/v0.1.0
