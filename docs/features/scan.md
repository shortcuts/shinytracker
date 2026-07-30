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
   `ScanOrchestrator.reviewQueue` instead.
5. Scroll down (`BoxScanBridge.scrollBoxDown()`) and repeat.

## Current UI

Debug-only, in `:app`'s `MainActivity`/`ShinyApp` — a "Run full scan"
button and two counters (new/total caught this scan, review-queue size). A
real results screen with confirm/reject for the review queue is
`:feature:checklist` (M4), not built yet.

## Sprite catalog

M3 ships a fixed ~20-sprite subset (`:core:sprites`'s `SpriteCatalog`),
vendored from `PokeMiners/pogo_assets` via `scripts/pull_reference_sprites.py`.
Full catalog + eligibility checklist is M4's `ShinyChecklistSource` +
`scripts/sync_sprites.py`.
