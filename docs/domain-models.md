# Domain Models

All in `:core:model`, pure Kotlin, no Android dependencies.

## DexEntry

Identity of one Pokemon variant.

| Field | Type | Notes |
|---|---|---|
| dexId | Int | Pokedex number |
| formId | Int | Form variant; 0 for the base/only form |
| costumeId | Int | 0 = no costume (default); matches PogoAssets' filename convention of omitting the costume segment for the base form |
| name | String | Species display name |
| types | List\<PokemonType\> | Sourced from `dexdata.json` (`:core:sprites`' `PokemonDexDataSource`), merged in by `ShinyChecklistSource`. Empty for dex ids >898, see `docs/features/checklist.md`'s type filter data coverage |
| localizedNames | Map\<String, String\> | Display names by source language key (`"english"`, `"japanese"`, `"chinese"`, `"french"`); empty for dex ids >898 |
| species | String? | Species flavor text (e.g. "Seed Pokémon"); null for dex ids >898 |
| evolvesFrom | Int? | Dex id this species evolves from; null if it has no pre-evolution or is outside `dexdata.json`'s coverage |
| evolvesTo | List\<Int\> | Dex ids this species evolves into; empty if it has none or is outside `dexdata.json`'s coverage |

## PokemonType

Elemental type enum (`NORMAL`..`FAIRY`, 18 values), used by the checklist's
type filter. Not yet populated per-species -- see `DexEntry.types`.

## ShinyRecord

One bundled reference sprite in the local matching catalog (`:core:sprites`'s `SpriteCatalog`).

| Field | Type | Notes |
|---|---|---|
| dexEntry | DexEntry | |
| shiny | Boolean | |
| assetPath | String | Relative to `core/sprites/src/main/assets/sprites/` |

## MatchResult

`SpriteMatcher`'s output for one cropped box-slot icon.

| Field | Type | Notes |
|---|---|---|
| dexEntry | DexEntry | |
| shiny | Boolean | |
| confidence | Float | 0..1, similarity score against the winning catalog entry |

## CaughtRecord

Domain-level "the user has caught this" fact, mapped from `CaughtEntity` (`:core:database`).

| Field | Type | Notes |
|---|---|---|
| dexEntry | DexEntry | |
| shiny | Boolean | |
| caughtAt | Long | Epoch millis |

## Generation

Pokemon generation, derived from dex id. Ranges per Bulbapedia's National Pokedex.

| Field | Type | Notes |
|---|---|---|
| dexRange | IntRange | One of KANTO..PALDEA; `fromDexId(dexId)` falls back to the last generation for unknown ids |

## ChecklistEntry

One eligible-shiny species (from `ShinyChecklistSource`) cross-referenced against caught records. Built by `ChecklistRepository` (owner mode) or inline in `SharedProfileViewModel` (shared read-only mode, against an imported profile instead of `CaughtRepository`).

| Field | Type | Notes |
|---|---|---|
| dexEntry | DexEntry | |
| caught | Boolean | |
| caughtAt | Long? | Null if not caught |
| generation | Generation | |
