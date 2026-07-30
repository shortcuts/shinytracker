# Feature: Box scan

## What it does

Scans the player's Pokemon GO shiny box end-to-end: captures a screenshot,
finds each icon's on-screen bounds via the accessibility node tree, crops
each icon, matches it against a bundled ~20-sprite reference catalog,
records confident matches, and routes low-confidence crops to a review
queue instead of guessing.

## Flow

1. `ScanOrchestrator.runFullScan()` reads the current icon bounds via
   `BoxScanBridge.getIconSlotBounds()`.
2. If bounds are empty, or identical to the previous scroll step, the scan
   stops — this is the end-of-scroll signal.
3. Otherwise: capture a screenshot, crop each icon (`IconCropper`), match
   each crop (`SpriteMatcher`).
4. Matches at or above `AppConstants.ScanConstants.MATCH_CONFIDENCE_THRESHOLD`
   get recorded via `CaughtRepository.recordIfAbsent()` (Room's
   insert-only-if-absent on the `(dexId, formId, costumeId, shiny)` primary
   key gives "new since last scan" for free). Below-threshold matches go to
   `ScanOrchestrator.reviewQueue` instead. `PendingReview` carries the
   crop plus the top-N `candidates` from `SpriteMatcher.matchCandidates()`
   (not a single best guess), so the validation panel can offer
   alternate-candidate chips.
5. Scroll down (`BoxScanBridge.scrollBoxDown()`) and repeat.

## Current UI

A floating overlay widget (`ScanWidgetOverlayService`), toggled from
`:app`'s `MainActivity`/`ShinyApp` (only shown once the accessibility
service is enabled) via a "Enable scan widget"/"Disable scan widget"
button. The widget is a draggable pill with two action buttons:

- **Screenshot**: `ScanOrchestrator.captureAndDetect()` — a single
  capture, detects every slot on screen (one or many, same code path),
  and sends every detected entry to the validation panel. Nothing is
  auto-recorded regardless of confidence.
- **Automated scan**: `ScanOrchestrator.runFullScan()` — the existing
  scroll-loop; confident matches auto-record as before, and the
  low-confidence `reviewQueue` is opened in the same validation panel
  once the loop finishes.

The validation panel (`ScanValidationScreen`, driven by
`ScanValidationPresenter`) is its own `WindowManager` overlay hosted by
`ScanWidgetOverlayService` — not a `MainActivity` nav-graph screen — so it
works while Pokemon GO, not shinytracker, is in the foreground. Per entry:
crop thumbnail, top candidate with confidence, alternate-candidate chips,
and Confirm/Reject actions.

`:app`'s `MainActivity`/`ShinyApp` also keeps the debug "Capture
screenshot"/"Scroll box down"/"Run full scan" buttons and counters, plus
the "View checklist" button into `:feature:checklist` (see
`docs/features/checklist.md`).

## Sprite catalog

`:core:sprites`'s `SpriteCatalog` lists every PNG vendored under
`core/sprites/src/main/assets/sprites/` at runtime (no hardcoded list) and
parses each filename back into a `ShinyRecord`. `scripts/pull_reference_sprites.py`
(M3) vendors a fixed ~20-sprite starter subset from `PokeMiners/pogo_assets`;
`scripts/sync_sprites.py` (M4) incrementally syncs the full set from the same
repo into the same directory.

Matching no longer decodes and describes every vendored PNG on-device.
`:core:sprites:descriptors`'s offline precompute tool computes each sprite's
multi-feature descriptor (pHash, dHash, HSV/LAB histograms, dominant colors,
edge signature, alpha mask + bounding box, plus the original grid-RGB
descriptor) ahead of time and bundles the result as
`core/sprites/src/main/assets/sprites/descriptors.json`. `:core:sprites`'s
`DescriptorCatalog` reads that file at runtime and `SpriteMatcher` joins it
against `SpriteCatalog`'s filename-parsed records; only the single cropped
box-slot icon is described on-device per match. Maintainer flow after
vendoring new sprites: `make sync-sprites` then `make precompute-descriptors`.
`SpriteMatcher.matchCandidates()` returns the top-N candidates by ensemble
score instead of just the single best match, for a future validation-list UI.
