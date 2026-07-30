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
