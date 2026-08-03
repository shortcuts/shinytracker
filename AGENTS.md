# shinytracker — Agent Reference

> Primary reference for AI coding agents. Read before touching any file.

---

## Project

Android-only Pokemon GO shiny box scanner. Reads a player's shiny box via
Accessibility Service screenshots, matches sprites against a bundled
checklist, and tracks which shinies have been caught.

| Field | Value |
|---|---|
| Package | `com.shinytracker.app` |
| Language | Kotlin |
| UI | Jetpack Compose |
| Min SDK | API 31 |
| Distribution | Sideloaded APK (GitHub Releases) for personal/internal use — no Play Store commitment yet |
| Storage | Room + DataStore |
| Backend | None (sprite/checklist data downloaded from a static source in M4, no accounts) |
| Open source | Yes |

Constraints:

- Offline-first for the core scan loop (M1–M3); network only for optional
  sprite/checklist sync (M4)
- No accounts
- All data on-device in Room + DataStore

---

## Documentation Maintenance Policy

Work is NOT complete until affected docs are updated. These files must stay in sync with the code:

| File | Update when |
|------|-------------|
| `AGENTS.md` (this file) — Key Services table | Adding, removing, or renaming a service or singleton |
| `docs/architecture.md` — module table | Adding or removing a Gradle module |
| `docs/domain-models.md` | Any change to `core/model/` data classes or enums (create this doc when the first domain model lands) |
| `docs/features/<feature>.md` | Behaviour change in the corresponding feature (create per-feature docs as features land, starting M1) |
| `docs/permissions.md` | Any change to the permissions the app requests |
| `docs/constants.md` | Adding a new constants object or changing the constants convention |
| `README.md` — feature table | Adding or removing a user-visible feature |
| `README.md` — module table | Adding or removing a Gradle module |

Rules:
- New feature → create `docs/features/<feature>.md` AND add row to AGENTS.md feature table AND README.md feature table.
- New Gradle module → add row to `docs/architecture.md` module table AND README.md module table.
- New domain model or field → update `docs/domain-models.md`.
- Deleted feature/module → remove from all tables above.
- Doc changes go in the same commit as the code change, not a follow-up.

---

## Pre-Commit Validation Policy

Work is NOT complete until lint and test passes.

```bash
make format
make lint
make test
```

Rules:
- Fix every lint error before declaring done. Warnings acceptable; errors not.
- Run after every set of edits, not just end of session.
- If check fails, fix root cause. Don't suppress unless genuine false positive + inline comment explaining why.
- Never suppress `Errors` category rules. Never batch-suppress with `@file:Suppress`.
- Never add co-authoring or "Claude-Sessions" to the commit

---

## Architecture

→ See @docs/architecture.md

---

## Constants

→ See @docs/constants.md

---

## Feature Specifications

| Feature | Doc |
|---------|-----|
| Box scan (screenshot → crop → match → record) | `docs/features/scan.md` |
| Checklist (owner + shared read-only, search/filter, export/import) | `docs/features/checklist.md` |
| Onboarding gate (language + optional scanner setup, permissions only required if scanner enabled, re-trigger on revoke) | `docs/features/onboarding.md` |
| Settings (revisit language + Enable Scanner choice after onboarding) | `docs/features/settings.md` |

---

## Domain Models

→ See @docs/domain-models.md

---

## Key Services

Populated as milestones land.

| Service | Module | Type | Purpose |
|---------|--------|------|---------|
| BoxScanBridge | `:feature:scan:api` | `@Singleton` bind/unbind bridge | Exposes `captureScreenshot()`/`scrollBoxDown()`/`getIconSlotBounds()` to consumers without a dependency on the concrete `AccessibilityService` |
| BoxScanAccessibilityService | `:feature:scan:impl` | `AccessibilityService` | Captures box screenshots, dispatches the scroll gesture, and walks the node tree for icon bounds; binds itself into `BoxScanBridge` |
| SpriteMatcher | `:core:sprites` | `@Singleton` | Matches a cropped box-slot icon against the bundled sprite catalog, returns a `MatchResult` |
| CaughtRepository | `:core:data` | `@Singleton` | Single source of truth for caught shinies; insert-only-if-absent against `CaughtDao`, plus `delete()` to mark a caught record uncaught again |
| ScanOrchestrator | `:feature:scan:impl` | `@Singleton` | Drives the full scan loop: bounds → screenshot → crop → match → record/review, until scrolling stops changing bounds |
| ShinyChecklistSource | `:core:sprites` | `@Singleton` | Loads the bundled/cached shiny-eligibility checklist (species+variant coverage cross-checked against leekduck.com, each entry also carrying `family`/`releaseDate`); `refresh()` re-fetches over the network without touching the bundled asset |
| PokemonDexDataSource | `:core:sprites` | `@Singleton` | Loads the bundled species-level Pokemon metadata (names, types, species, evolution links) keyed by dex id; merged into DexEntry by ShinyChecklistSource |
| ChecklistRepository | `:core:data` | `@Singleton` | Combines `ShinyChecklistSource` + `CaughtRepository` into `ChecklistEntry` lists for the checklist UI; `toggleCaught()` records/deletes a `CaughtRecord` directly from a tapped checklist tile |
| ProfileShareRepository | `:core:data` | `@Singleton` | Exports the owner's caught list to a shareable file; imports someone else's exported file for read-only viewing (never writes to `CaughtRepository`) |
| ScanWidgetOverlayService | `:feature:scan:impl` | `Service` (`@AndroidEntryPoint`) | Floating overlay widget: screenshot-detect button, automated-scan button, hosts the validation panel |
| ScanValidationPresenter | `:feature:scan:impl` | plain class, one instance per `ScanWidgetOverlayService` | Confirm/reject state + recording for both widget button flows — not a ViewModel (Service is not a `ViewModelStoreOwner`) |
| OnboardingPreferencesDataSource | `:core:datastore` | `@Singleton` | Raw Preferences DataStore reads/writes for the onboarding gate's persisted display-language + scanner-enabled choices |
| OnboardingPreferencesRepository | `:core:data` | `@Singleton` | Maps `OnboardingPreferencesDataSource`'s raw values to/from `DisplayLanguage`; `completeSetup()` persists both onboarding choices in one transaction |

---

## Permissions

→ See @docs/permissions.md

---

## Technical Constraints

→ See @docs/technical-constraints.md

---

## Code Style Rules

→ See @docs/code-style.md

---

## Testing Strategy

→ See @docs/testing.md

```bash
make coverage        # generate HTML + XML reports
make coverage-open   # open HTML report in browser
```
