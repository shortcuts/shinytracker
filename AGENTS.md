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

No features are built yet. The planned module structure lives in
@docs/architecture.md. This section gains a table of
`docs/features/<feature>.md` entries as features land, starting with M1.

---

## Domain Models

No domain models exist yet — `:core:model` is not yet a real module. This
section links to `docs/domain-models.md` once the first models land.

---

## Key Services

Populated as milestones land.

| Service | Module | Type | Purpose |
|---------|--------|------|---------|
| BoxScanBridge | `:feature:scan:api` | `@Singleton` bind/unbind bridge | Exposes `captureScreenshot()`/`scrollBoxDown()` to consumers without a dependency on the concrete `AccessibilityService` |
| BoxScanAccessibilityService | `:feature:scan:impl` | `AccessibilityService` | Captures box screenshots and dispatches the scroll gesture; binds itself into `BoxScanBridge` |

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
