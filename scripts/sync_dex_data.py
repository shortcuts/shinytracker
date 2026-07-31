#!/usr/bin/env python3
"""Fetch species-level Pokemon metadata (localized names, types, species
flavor text, evolution dex-id links) and write it to
core/sprites/src/main/assets/dexdata.json, bundled like checklist.json so a
fresh install works offline.

Source: Purukitto/pokemon-data.json's pokedex.json
(github.com/Purukitto/pokemon-data.json), 898 entries (dex ids 1-898).
"""
import json
import pathlib
import urllib.request

SOURCE_URL = "https://raw.githubusercontent.com/Purukitto/pokemon-data.json/master/pokedex.json"
DEST_FILE = pathlib.Path(__file__).resolve().parent.parent / "core" / "sprites" / "src" / "main" / "assets" / "dexdata.json"


def main() -> None:
    with urllib.request.urlopen(SOURCE_URL) as response:
        raw = json.load(response)

    entries = sorted(
        (
            {
                "dexId": entry["id"],
                "names": entry["name"],
                "types": [t.upper() for t in entry["type"]],
                "species": entry.get("species"),
                "evolvesFrom": int(entry["evolution"]["prev"][0]) if entry.get("evolution", {}).get("prev") else None,
                "evolvesTo": [int(n[0]) for n in entry.get("evolution", {}).get("next", [])],
            }
            for entry in raw
        ),
        key=lambda e: e["dexId"],
    )

    DEST_FILE.parent.mkdir(parents=True, exist_ok=True)
    DEST_FILE.write_text(json.dumps(entries, indent=2) + "\n")
    print(f"wrote {len(entries)} dex data entries to {DEST_FILE}")


if __name__ == "__main__":
    main()
