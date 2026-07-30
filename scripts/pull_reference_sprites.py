#!/usr/bin/env python3
"""Vendor a fixed ~20-sprite starter subset from PokeMiners/pogo_assets into
core/sprites/src/main/assets/sprites/. Run manually once (or whenever the
fixed set below changes); safe to re-run, skips files already present.

This covers only M3's fixed starter set. Full incremental sync against the
same repo is scripts/sync_sprites.py (M4), which should extend this
approach (list local cache, diff against upstream, download only missing
files) rather than duplicate a second fetch mechanism from scratch.
"""
import pathlib
import urllib.request

REPO_RAW_BASE = "https://raw.githubusercontent.com/PokeMiners/pogo_assets/master/Images/Pokemon"
DEST_DIR = pathlib.Path(__file__).resolve().parent.parent / "core" / "sprites" / "src" / "main" / "assets" / "sprites"

# Fixed ~20-sprite starter set: 9 species (normal + shiny) plus one Pikachu
# costume (normal only) so the catalog has a real shiny/normal/costume
# triplet for the SpriteMatcher unit test.
SPRITE_FILES = [
    "pokemon_icon_001_00.png", "pokemon_icon_001_00_shiny.png",   # Bulbasaur
    "pokemon_icon_004_00.png", "pokemon_icon_004_00_shiny.png",   # Charmander
    "pokemon_icon_007_00.png", "pokemon_icon_007_00_shiny.png",   # Squirtle
    "pokemon_icon_019_00.png", "pokemon_icon_019_00_shiny.png",   # Rattata
    "pokemon_icon_025_00.png", "pokemon_icon_025_00_shiny.png",   # Pikachu
    "pokemon_icon_025_00_01.png",                                 # Pikachu (costume 01)
    "pokemon_icon_066_00.png", "pokemon_icon_066_00_shiny.png",   # Machop
    "pokemon_icon_074_00.png", "pokemon_icon_074_00_shiny.png",   # Geodude
    "pokemon_icon_129_00.png", "pokemon_icon_129_00_shiny.png",   # Magikarp
    "pokemon_icon_133_00.png", "pokemon_icon_133_00_shiny.png",   # Eevee
]


def main() -> None:
    DEST_DIR.mkdir(parents=True, exist_ok=True)
    for filename in SPRITE_FILES:
        dest = DEST_DIR / filename
        if dest.exists():
            print(f"skip (already vendored): {filename}")
            continue
        url = f"{REPO_RAW_BASE}/{filename}"
        print(f"fetching: {url}")
        with urllib.request.urlopen(url) as response:
            dest.write_bytes(response.read())


if __name__ == "__main__":
    main()
