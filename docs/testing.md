# Testing

## Coverage

Coverage via [kotlinx-kover](https://github.com/Kotlin/kotlinx-kover) (v0.8.3). All modules use convention plugins. Root aggregates into merged report.

```bash
make coverage        # generate HTML + XML reports
make coverage-open   # open HTML report in browser
```

Per-module:

```bash
./gradlew :<module>:koverHtmlReport
```

Reports:
- HTML: `build/reports/kover/html/index.html`
- XML (CI): `build/reports/kover/report.xml`

## Smoke Tests (`:app` androidTest)

Not added yet — there is no `:app` navigation to smoke-test. The
`Makefile`'s `screenshot`/`smoke-test` targets will be re-added once M4
gives `:app` real screens to navigate.

## Unit Tests (`:core:*`)

- Repo logic w/ fake DAO (in-memory Room)
- Sprite descriptor matching: known sprite + candidate → assert `MatchResult`
- `CaughtDao` round-trip: insert → query → assert `CaughtEntity` fields
- Checklist filter logic: known `ShinyRecord` list + filter → expected subset

Shared utils in `:core:testing`.

## Integration Tests (`:feature:*`)

- Hilt w/ `@HiltAndroidTest`
- Full scan → match → save-to-caught-list w/ in-memory Room

## UI Tests (Compose)

- `ComposeTestRule` for screen-level tests
- Checklist screen: seeded data, assert list renders and filters

## What NOT to Test

- `AccessibilityService` screenshot capture (requires a real device, per M2's exit criteria — manual on-device testing only)
- `dispatchGesture` (requires a real device)
- Sprite-matching visual accuracy against live game UI (requires a real device)
