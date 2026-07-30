# Feature: Checklist

## What it does

Shows every shiny-eligible species (from `ShinyChecklistSource`) grouped by
generation, cross-referenced against caught records. Two modes:

- **Owner mode** — the device owner's own caught list. Editable indirectly
  (caught state comes from scanning, see `docs/features/scan.md`). Supports
  search, a Caught/Not caught/All filter, and exporting the list to share.
- **Shared read-only mode** — someone else's exported list, imported via a
  file `Intent`. No filter chips, no edit affordance, just search and a
  banner naming whose profile is showing.

## Flow

1. `ChecklistViewModel` (owner) combines `ChecklistRepository.observeChecklist()`
   with local search text and filter state into `ChecklistUiState`.
2. `SharedProfileViewModel` (shared) reads the imported file's `Uri` from the
   nav arg (`ChecklistRoute.SHARED_ARG_KEY`), imports it via
   `ProfileShareRepository.importProfile()`, and cross-references the result
   against `ShinyChecklistSource`'s full species list -- **never**
   `CaughtRepository`, since an imported list is not the device owner's own
   progress.
3. `ChecklistScreen` (owner) / `SharedProfileScreen` (shared) both render
   through a shared `ChecklistScaffold`: top app bar with search, a progress
   header (`caught / total`), a `LazyColumn` with one sticky header per
   generation followed by that generation's species as a wrapping icon grid.

## Sharing

`ProfileShareRepository` (`:core:data`) exports the owner's `CaughtRepository`
list to a JSON file (`{schemaVersion, entries: [{dexId, formId, costumeId,
shiny, name, caughtAt}]}`) under `context.cacheDir/shared-profiles/`, handed
out as a `content://` URI via `:app`'s `FileProvider`
(`res/xml/file_paths.xml`) for `Intent.ACTION_SEND`. Import happens through
`MainActivity`'s `ACTION_VIEW`/`ACTION_SEND` (`application/json`)
intent-filter, parsed by `ChecklistRoute.parseSharedProfileArg` into the
`checklist/shared/{profileUri}` route.

## Sprite art

Species tiles currently render as a placeholder (two-letter name abbreviation
+ dex number, caught state as background color + checkmark), not real sprite
images -- no image-loading library (e.g. Coil) is wired up yet. Swapping in
real sprite bitmaps from `:core:sprites`' vendored PNGs is future work.

## Eligibility data

`ShinyChecklistSource` (`:core:sprites`) loads a bundled
`core/sprites/src/main/assets/checklist.json` (written by
`scripts/sync_checklist.py` from pogoapi.net's `shiny_pokemon.json`) by
default, so a fresh install works offline. `refresh()` re-fetches the same
source over the network and overwrites only the on-disk cache
(`filesDir/checklist.json`), never the bundled asset; on failure the
previously-loaded list is left untouched.
