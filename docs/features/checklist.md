# Feature: Checklist

## What it does

Shows every shiny-eligible species (from `ShinyChecklistSource`) grouped by
generation, cross-referenced against caught records. Two modes:

- **Owner mode** — the app's default/home screen, reached immediately
  after the onboarding gate passes (see `docs/features/onboarding.md`).
  The device owner's own caught list. Editable by scanning
  (see `docs/features/scan.md`) or by tapping a species tile to toggle its
  caught state directly. Supports search (by name or dex #), an advanced
  filter sheet (status, generation, type), and exporting the list to share.
- **Shared read-only mode** — someone else's exported list, imported via a
  file `Intent`. Same search + filter sheet as owner mode, no edit affordance,
  plus a banner naming whose profile is showing.

## Flow

1. `ChecklistViewModel` (owner) and `SharedProfileViewModel` (shared) both
   combine their entry source with local search text and an `AdvancedFilter`
   (`ChecklistFiltering.kt`: status + generation set + type set, AND'd
   together) via `List<ChecklistEntry>.filterEntries()`.
2. `SharedProfileViewModel` reads the imported file's `Uri` from the nav arg
   (`ChecklistRoute.SHARED_ARG_KEY`), imports it via
   `ProfileShareRepository.importProfile()`, and cross-references the result
   against `ShinyChecklistSource`'s full species list -- **never**
   `CaughtRepository`, since an imported list is not the device owner's own
   progress.
3. `ChecklistScreen` (owner) / `SharedProfileScreen` (shared) both render
   through a shared `ChecklistScaffold`: top app bar with search, a filter
   icon (badged when a filter is active) opening a `ModalBottomSheet`, and
   -- owner mode only -- a leading hamburger icon opening a navigation
   drawer, a floating-scan-widget toggle icon and a share icon, a progress
   header (`caught / total`), and a `LazyColumn` with one collapsible
   region section per generation (tap the header to expand/collapse;
   collapse state is `rememberSaveable`, resets on process death) followed
   by that generation's species laid out as fixed-width rows of sprite
   tiles, one `LazyColumn` item per row, so off-screen rows aren't
   composed.

The floating scan widget (`ScanWidgetOverlayService`, see
`docs/features/scan.md`) is enabled/disabled from a `PictureInPictureAlt`
icon button in owner mode's top app bar -- the single entry point into
scanning from this screen.
4. In owner mode, tapping a tile calls `ChecklistViewModel.toggleCaught()`,
   which calls `ChecklistRepository.toggleCaught()` to record or delete
   the species' `CaughtRecord`. Shared read-only mode never passes a
   toggle handler into `ChecklistScaffold`, so its tiles stay non-tappable.

## Sharing

`ProfileShareRepository` (`:core:data`) exports the owner's `CaughtRepository`
list to a JSON file (`{schemaVersion, entries: [{dexId, formId, costumeId,
shiny, name, caughtAt}]}`) under `context.cacheDir/shared-profiles/`, handed
out as a `content://` URI via `:app`'s `FileProvider`
(`res/xml/file_paths.xml`) for `Intent.ACTION_SEND`. Import happens through
`MainActivity`'s `ACTION_VIEW`/`ACTION_SEND` (`application/json`)
intent-filter, parsed by `ChecklistRoute.parseSharedProfileArg` into the
`checklist/shared/{profileUri}` route.

## Navigation drawer

Owner mode's top app bar has a leading hamburger icon (`Icons.Default.Menu`)
that opens a `ModalNavigationDrawer` (`ChecklistDrawerContent`), wired from
`MainActivity`.

The drawer currently has exactly one item, "Home", which closes the drawer
(and re-navigates to the already-current `ChecklistRoute.OWNER` route) --
intentionally inert today, kept ready for a Settings item once that screen
is scoped.

`SharedProfileScreen` does not show the hamburger icon (deep-link-only
screen, no reason to expose the owner's own navigation).

## Sprite art

Species tiles render the real shiny sprite via Coil (`AsyncImage`) loaded
from `:core:sprites`' vendored PNGs (`file:///android_asset/sprites/...`, all
local, no network). Every tile shows the shiny variant, since this app only
tracks shinies. Uncaught species render desaturated + dimmed rather than
hidden, so the grid still shows what's missing.

## Type filter data coverage

`AdvancedFilter.types` and `PokemonType` (`:core:model`) drive the filter
sheet's Type chip grid. `DexEntry.types` is populated from
`core/sprites/src/main/assets/dexdata.json` (written by
`scripts/sync_dex_data.py` from Purukitto's `pokedex.json`), merged into each
`DexEntry` by `ShinyChecklistSource`/`PokemonDexDataSource`. That source only
covers dex ids 1-898, while `checklist.json` (pogoapi.net) covers ids up to
1000 -- species above 898 still get `DexEntry.types == emptyList()` and the
type filter still excludes them. This is a known, narrower gap, not a bug.
The filter sheet's Type section shows a caption noting this limitation.

## Eligibility data

`ShinyChecklistSource` (`:core:sprites`) loads a bundled
`core/sprites/src/main/assets/checklist.json` by default, so a fresh
install works offline. `scripts/sync_checklist.py` writes that file from
two sources: pogoapi.net's `shiny_pokemon.json` for species-level
eligibility and display names, and `PokeMiners/pogo_assets`' shiny sprite
filenames (Git Trees API) to explode each eligible species into its
individual form/costume variants -- each entry is a
`(dexId, formId, costumeId)` triple, not just a species. `refresh()`
re-fetches pogoapi.net's species list over the network and overwrites only
the on-disk cache (`filesDir/checklist.json`), never the bundled asset; on
failure the previously-loaded list is left untouched. (`refresh()` only
re-syncs species-level eligibility, not variant data -- variant data only
changes via re-running `scripts/sync_checklist.py` and shipping a new
build.)
