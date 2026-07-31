# shinytracker

Android-only Pokemon GO shiny box scanner. Reads a player's shiny box via
Accessibility Service screenshots, matches sprites against a bundled
checklist, and tracks which shinies have been caught. Sideloaded APK
(GitHub Releases), no accounts, no backend.

See `AGENTS.md` for the full architecture, constants, testing, and
contribution reference.

## Features

| Feature | Doc |
|---------|-----|
| Box scan (screenshot → crop → match → record) | `docs/features/scan.md` |
| Checklist (owner + shared read-only, search/filter, export/import) | `docs/features/checklist.md` |
| Onboarding gate (block app until accessibility + overlay granted, re-trigger on revoke) | `docs/features/onboarding.md` |

## Modules

| Module | Purpose |
|--------|---------|
| `:app` | Entry point, Hilt, nav host |
| `:core:common` | `AppConstants`-equivalent, extensions |
| `:core:model` | Pure Kotlin: `DexEntry`, `ShinyRecord`, `MatchResult`, `CaughtRecord`, `ChecklistEntry`, `Generation`, `PokemonType` |
| `:core:database` | Room: `CaughtEntity`/`CaughtDao` |
| `:core:datastore` | DataStore prefs (planned, not yet used) |
| `:core:data` | Repositories, single source of truth |
| `:core:designsystem` | Compose theme/tokens |
| `:core:sprites` | `SpriteMatcher`, `SpriteCatalog`, `DescriptorCatalog`, `ShinyChecklistSource`, `PokemonDexDataSource` |
| `:core:sprites:descriptors` | Sprite descriptor math + offline precompute tool (pure JVM) |
| `:core:testing` | Fake DAOs, shared test utils |
| `:feature:scan:api` / `:impl` | `AccessibilityService`, `ScanOrchestrator`, `IconCropper` |
| `:feature:checklist:api` / `:impl` | Owner + shared read-only checklist UI |
