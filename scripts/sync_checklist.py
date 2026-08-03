#!/usr/bin/env python3
"""Fetch the "which shinies exist" eligibility checklist and write it to
core/sprites/src/main/assets/checklist.json, bundled like the sprite PNGs so
a fresh install works offline.

Three sources, combined:
- pogoapi.net's shiny_pokemon.json: which species have a confirmed live
  shiny release (maintained, bot-updated, LeekDuck-derived data -- not raw
  datamined client assets). Primary species-level eligibility set and each
  species' display name.
- leekduck.com's shiny checklist data (https://leekduck.com/shiny/), read
  from its own two JSON endpoints:
    - https://leekduck.com/shiny/pms.json -- one row per species OR per
      costume/event/Gigantamax/regional variant, carrying (among fields
      this script ignores) `dex`, `family` (evolution-line grouping label),
      `released_date`/`shiny_released` (this script's eligibility signal --
      see leekduck_eligible_dex_ids below, reverse-engineered from
      leekduck's own bundle.js since the API has no docs), and `aa_fn`
      (icon-resolution plumbing, deliberately not consumed here).
    - https://leekduck.com/shiny/name.json?v159 -- dex id -> localized name
      object (`en`, `ja`, `ko`, `zh`, `fr`, `de`, `es`, `it`); used only to
      supply a display name for dexIds pogoapi.net doesn't have one for.
  Used to (a) widen the species-eligibility set for dexIds pogoapi.net
  hasn't listed yet, and (b) backfill each eligible species' `family`
  (evolution-line label) and `releaseDate` (shiny release date, ISO
  YYYY-MM-DD). Does NOT replace pogoapi.net (kept as the primary source, no
  behavior change for species it already covers) and does NOT replace
  pogo_assets for variant/sprite-filename data (see below) -- leekduck's own
  variant codes (`isotope`/`type`) don't map onto pogo_assets' filename
  scheme, so this script never tries to cross the two.
- PokeMiners/pogo_assets' Git Trees API: enumerates every vendored shiny
  sprite filename (`pokemon_icon_DDD_FF[_CC]_shiny.png`) to explode each
  already-eligible species into its individual form/costume variants -- a
  costume or form that visibly changes the sprite is its own catchable
  shiny entry, not a duplicate of the base species. Variant triples whose
  dexId is NOT in the combined eligible set (pogoapi.net UNION leekduck) are
  skipped -- pogo_assets ships some shiny sprite art for species that
  aren't confirmed live in-game yet, and eligibility here still means "a
  live-tracking source confirms this is live", not "asset exists in a
  datamine".
"""
import datetime
import json
import pathlib
import re
import urllib.request

SHINY_SOURCE_URL = "https://pogoapi.net/api/v1/shiny_pokemon.json"
LEEKDUCK_PMS_URL = "https://leekduck.com/shiny/pms.json"
LEEKDUCK_NAME_URL = "https://leekduck.com/shiny/name.json?v159"
TREE_API_URL = "https://api.github.com/repos/PokeMiners/pogo_assets/git/trees/master?recursive=1"
TREE_PATH_PREFIX = "Images/Pokemon/"
DEST_FILE = pathlib.Path(__file__).resolve().parent.parent / "core" / "sprites" / "src" / "main" / "assets" / "checklist.json"

FILENAME_RE = re.compile(r"^pokemon_icon_(\d{3})_(\d{2})(?:_(\d{2}))?_shiny\.png$")
USER_AGENT = "shinytracker-sync-checklist"


def _get_json(url: str):
    # pogoapi.net and leekduck.com both return 403 to urllib's default
    # "Python-urllib/x.y" User-Agent.
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=30) as response:
        return json.load(response)


def fetch_species_names() -> dict[int, str]:
    raw = _get_json(SHINY_SOURCE_URL)
    return {int(entry["id"]): entry["name"] for entry in raw.values()}


def fetch_leekduck_pms() -> list[dict]:
    return _get_json(LEEKDUCK_PMS_URL)


def fetch_leekduck_names() -> dict[int, str]:
    raw = _get_json(LEEKDUCK_NAME_URL)
    return {int(dex_id): entry["en"] for dex_id, entry in raw.items()}


def leekduck_eligible_dex_ids(pms: list[dict]) -> set[int]:
    """Reverse-engineered from leekduck.com/shiny/bundle.js's Ur(): a pm row
    counts as shiny-eligible if it's explicitly flagged, or its
    released_date has already passed."""
    today = datetime.date.today()
    eligible = set()
    for entry in pms:
        released = entry.get("shiny_released", False)
        if not released:
            released_date = entry.get("released_date")
            if released_date:
                try:
                    released = datetime.datetime.strptime(released_date, "%Y/%m/%d").date() < today
                except ValueError:
                    # A handful of leekduck rows carry malformed dates (e.g.
                    # "2020/11/31") -- treat as not-a-signal rather than crash.
                    released = False
        if released:
            eligible.add(entry["dex"])
    return eligible


def leekduck_species_meta(pms: list[dict]) -> dict[int, dict]:
    """One family/releaseDate pair per dexId, species-level (leekduck's
    variant codes don't map onto our (formId, costumeId) scheme, so this is
    never computed per-variant). Canonical row = the dexId's plain row (no
    isotope/type); falls back to the earliest-released_date row, then the
    first row in file order, for dexIds that only ever appear as
    costume/event rows."""
    by_dex: dict[int, list[dict]] = {}
    for entry in pms:
        by_dex.setdefault(entry["dex"], []).append(entry)

    meta = {}
    for dex_id, entries in by_dex.items():
        canonical = next((e for e in entries if "isotope" not in e and "type" not in e), None)
        if canonical is None:
            dated = [e for e in entries if e.get("released_date")]
            canonical = min(dated, key=lambda e: e["released_date"], default=entries[0])
        released_date = canonical.get("released_date")
        meta[dex_id] = {
            "family": canonical.get("family"),
            "releaseDate": released_date.replace("/", "-") if released_date else None,
        }
    return meta


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
    pogoapi_names = fetch_species_names()
    leekduck_pms = fetch_leekduck_pms()
    leekduck_names = fetch_leekduck_names()
    leekduck_eligible = leekduck_eligible_dex_ids(leekduck_pms)
    species_meta = leekduck_species_meta(leekduck_pms)

    combined_species_names = dict(pogoapi_names)
    newly_eligible = leekduck_eligible - pogoapi_names.keys()
    skipped_no_name = []
    for dex_id in newly_eligible:
        name = leekduck_names.get(dex_id)
        if name is None:
            skipped_no_name.append(dex_id)
            continue
        combined_species_names[dex_id] = name
    if skipped_no_name:
        print(f"skipped {len(skipped_no_name)} leekduck-eligible dexIds with no name.json entry: {sorted(skipped_no_name)}")

    variant_triples = fetch_variant_triples()

    entries = sorted(
        (
            {
                "dexId": dex_id,
                "formId": form_id,
                "costumeId": costume_id,
                "name": combined_species_names[dex_id],
                "family": species_meta.get(dex_id, {}).get("family"),
                "releaseDate": species_meta.get(dex_id, {}).get("releaseDate"),
            }
            for dex_id, form_id, costume_id in variant_triples
            if dex_id in combined_species_names
        ),
        key=lambda e: (e["dexId"], e["formId"], e["costumeId"]),
    )

    DEST_FILE.parent.mkdir(parents=True, exist_ok=True)
    DEST_FILE.write_text(json.dumps(entries, indent=2) + "\n")
    added = len(combined_species_names) - len(pogoapi_names)
    print(
        f"wrote {len(entries)} checklist entries ({len(combined_species_names)} eligible species, "
        f"{added} newly added via leekduck.com cross-check) to {DEST_FILE}"
    )


if __name__ == "__main__":
    main()
