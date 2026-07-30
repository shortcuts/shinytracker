#!/usr/bin/env python3
"""Fetch the "which shinies exist" eligibility checklist and write it to
core/sprites/src/main/assets/checklist.json, bundled like the sprite PNGs so
a fresh install works offline.

Source: pogoapi.net's shiny_pokemon.json (a maintained, bot-updated JSON
API mirroring the same LeekDuck-derived shiny-availability data other
Pokemon GO tools use -- not live HTML scraping). ScrapedDuck
(github.com/bigfoott/ScrapedDuck), this plan's originally-assumed source,
was checked at execution time and does not publish a shiny/Pokemon data
file on its `data` branch (only events/raids/research/eggs/rocket lineups)
-- pogoapi.net is the substitute, same category of source (maintained JSON
mirror), not a scraper shinytracker runs itself.
"""
import json
import pathlib
import urllib.request

SOURCE_URL = "https://pogoapi.net/api/v1/shiny_pokemon.json"
DEST_FILE = pathlib.Path(__file__).resolve().parent.parent / "core" / "sprites" / "src" / "main" / "assets" / "checklist.json"


def main() -> None:
    # pogoapi.net returns 403 to urllib's default "Python-urllib/x.y" User-Agent.
    request = urllib.request.Request(SOURCE_URL, headers={"User-Agent": "shinytracker-sync-checklist"})
    with urllib.request.urlopen(request) as response:
        raw = json.load(response)

    entries = sorted(
        ({"dexId": int(entry["id"]), "name": entry["name"]} for entry in raw.values()),
        key=lambda e: e["dexId"],
    )

    DEST_FILE.parent.mkdir(parents=True, exist_ok=True)
    DEST_FILE.write_text(json.dumps(entries, indent=2) + "\n")
    print(f"wrote {len(entries)} checklist entries to {DEST_FILE}")


if __name__ == "__main__":
    main()
