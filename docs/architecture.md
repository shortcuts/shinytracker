# Architecture

## Module Structure

Multi-module, NowInAndroid-style. Feature = `api` (contract) + `impl`. Shared: `:core:*`.

All modules below are `include()`d in `settings.gradle.kts`. All have landed
as real directories with code.

```
feature/*        — UI + ViewModels (Compose screens, no business logic)
  ↓ depends on
core/data        — Repositories (single source of truth)
  ↓
core/database    — Room DB
core/datastore   — DataStore Prefs

core/sprites     — Sprite matching engine, independent of UI
core/model       — Pure Kotlin data classes, no Android deps
```

| Module | Purpose |
|--------|---------|
| `:app` | Entry point, Hilt, nav host |
| `:core:common` | `AppConstants`-equivalent, extensions |
| `:core:model` | Pure Kotlin: `DexEntry`, `ShinyRecord`, `MatchResult`, `CaughtRecord`, `ChecklistEntry`, `Generation`, `DisplayLanguage` |
| `:core:database` | Room: `CaughtEntity`/`CaughtDao` |
| `:core:datastore` | `OnboardingPreferencesDataSource` -- raw Preferences DataStore reads/writes for the onboarding gate's persisted choices |
| `:core:data` | Repositories, single source of truth |
| `:core:designsystem` | Compose theme/tokens (near-copy of locationjoystick's, retinted) |
| `:core:sprites` | `SpriteMatcher`, `SpriteCatalog`, `DescriptorCatalog`, `ShinyChecklistSource` — this app's `:core:routing` equivalent |
| `:core:sprites:descriptors` | Pure JVM: descriptor math (pHash, dHash, histograms, edges, etc.) + the offline precompute tool that bundles `descriptors.json` |
| `:core:testing` | Fake DAOs, shared test utils |
| `:feature:scan:api` / `:impl` | `AccessibilityService`, `ScanOrchestrator`, `IconCropper` |
| `:feature:checklist:api` / `:impl` | Owner + shared read-only checklist UI: search, filter, region grouping, profile export/import |

## MVVM + Repository Pattern

VMs expose `StateFlow`/`SharedFlow`. UI collects via `collectAsStateWithLifecycle()`. Repos = single truth — VMs never touch DAOs/DataStore directly.

Data flow: ViewModel → Repository → DataSource (Room / DataStore / Accessibility Service).

## Dependency Injection

Hilt throughout. VMs: `@HiltViewModel`. Repos: `@Singleton`.

## Reactive Streams

Kotlin Flow everywhere. No RxJava. No LiveData.

## Coroutines

- `viewModelScope` for UI-bound work
- `ServiceScope` (service lifecycle) for background work
- Never `GlobalScope`
- Always `SupervisorJob()` in service scopes
