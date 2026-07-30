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
