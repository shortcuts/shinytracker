#!/usr/bin/env python3
"""Fetch the "which shinies exist" eligibility checklist and write it to
core/sprites/src/main/assets/checklist.json, bundled like the sprite PNGs so
a fresh install works offline.

Two sources, combined:
- pogoapi.net's shiny_pokemon.json: which species have a confirmed live
  shiny release (maintained, bot-updated, LeekDuck-derived data -- not raw
  datamined client assets). Defines the species-level eligibility set and
  each species' display name. Same source/rationale as before this script
  started also reading pogo_assets.
- PokeMiners/pogo_assets' Git Trees API: enumerates every vendored shiny
  sprite filename (`pokemon_icon_DDD_FF[_CC]_shiny.png`) to explode each
  already-eligible species into its individual form/costume variants -- a
  costume or form that visibly changes the sprite is its own catchable
  shiny entry, not a duplicate of the base species. Variant triples whose
  dexId is NOT in pogoapi.net's eligible set are skipped: pogo_assets ships
  some shiny sprite art for species that aren't confirmed live in-game yet,
  and eligibility here still means "pogoapi.net confirms this is live",
  same rule as before -- pogo_assets only adds variant granularity within
  that set, it does not widen it.
"""
import json
import pathlib
import re
import urllib.request

SHINY_SOURCE_URL = "https://pogoapi.net/api/v1/shiny_pokemon.json"
TREE_API_URL = "https://api.github.com/repos/PokeMiners/pogo_assets/git/trees/master?recursive=1"
TREE_PATH_PREFIX = "Images/Pokemon/"
DEST_FILE = pathlib.Path(__file__).resolve().parent.parent / "core" / "sprites" / "src" / "main" / "assets" / "checklist.json"

FILENAME_RE = re.compile(r"^pokemon_icon_(\d{3})_(\d{2})(?:_(\d{2}))?_shiny\.png$")


def fetch_species_names() -> dict[int, str]:
    # pogoapi.net returns 403 to urllib's default "Python-urllib/x.y" User-Agent.
    request = urllib.request.Request(SHINY_SOURCE_URL, headers={"User-Agent": "shinytracker-sync-checklist"})
    with urllib.request.urlopen(request) as response:
        raw = json.load(response)
    return {int(entry["id"]): entry["name"] for entry in raw.values()}


def fetch_variant_triples() -> set[tuple[int, int, int]]:
    """Git Trees API recursive=1 has no per-call pagination limit (unlike
    the Contents API's 1000-entries-per-call cap), so one request covers the
    whole tree -- confirmed non-truncated at plan time. Unauthenticated
    GitHub API calls are capped at 60/hour per IP -- this is a manually-run
    maintainer tool, not a CI job, so that cap is not a real obstacle; no
    retry/backoff/auth-token handling is built for it (same precedent as
    scripts/sync_sprites.py)."""
    with urllib.request.urlopen(TREE_API_URL, timeout=30) as response:
        tree = json.load(response)["tree"]

    triples = set()
    for entry in tree:
        path = entry["path"]
        if entry["type"] != "blob" or not path.startswith(TREE_PATH_PREFIX):
            continue
        filename = path[len(TREE_PATH_PREFIX):]
        match = FILENAME_RE.match(filename)
        if not match:
            continue
        dex_id, form_id, costume_id = match.groups()
        triples.add((int(dex_id), int(form_id), int(costume_id) if costume_id else 0))
    return triples


def main() -> None:
    species_names = fetch_species_names()
    variant_triples = fetch_variant_triples()

    entries = sorted(
        (
            {"dexId": dex_id, "formId": form_id, "costumeId": costume_id, "name": species_names[dex_id]}
            for dex_id, form_id, costume_id in variant_triples
            if dex_id in species_names
        ),
        key=lambda e: (e["dexId"], e["formId"], e["costumeId"]),
    )

    DEST_FILE.parent.mkdir(parents=True, exist_ok=True)
    DEST_FILE.write_text(json.dumps(entries, indent=2) + "\n")
    print(f"wrote {len(entries)} checklist entries ({len(species_names)} eligible species) to {DEST_FILE}")


if __name__ == "__main__":
    main()
