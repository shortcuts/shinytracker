#!/usr/bin/env python3
"""Incrementally sync the full PokeMiners/pogo_assets sprite set into
core/sprites/src/main/assets/sprites/. Extends pull_reference_sprites.py's
approach (M3's fixed ~20-sprite bootstrap) to the full catalog: list what's
upstream, diff against what's already vendored locally, download only the
difference. Safe to re-run -- a second run with nothing new upstream
downloads zero files.

Rate limit note: unauthenticated GitHub API calls are capped at 60/hour per
IP. This is a manually-run maintainer tool, not a CI job, so that cap is not
a real obstacle -- no retry/backoff/auth-token handling is built for it.
"""
import json
import pathlib
import re
import urllib.request

REPO_RAW_BASE = "https://raw.githubusercontent.com/PokeMiners/pogo_assets/master/Images/Pokemon"
TREE_API_URL = "https://api.github.com/repos/PokeMiners/pogo_assets/git/trees/master?recursive=1"
DEST_DIR = pathlib.Path(__file__).resolve().parent.parent / "core" / "sprites" / "src" / "main" / "assets" / "sprites"

FILENAME_RE = re.compile(r"^pokemon_icon_\d{3}_\d{2}(_\d{2})?(_shiny)?\.png$")
TREE_PATH_PREFIX = "Images/Pokemon/"


def list_upstream_filenames() -> list[str]:
    """Git Trees API recursive=1 has no per-call pagination limit (unlike
    the Contents API's 1000-entries-per-call cap), so one request covers the
    whole tree."""
    with urllib.request.urlopen(TREE_API_URL, timeout=30) as response:
        tree = json.load(response)["tree"]
    filenames = []
    for entry in tree:
        path = entry["path"]
        if entry["type"] != "blob" or not path.startswith(TREE_PATH_PREFIX):
            continue
        filename = path[len(TREE_PATH_PREFIX):]
        if FILENAME_RE.match(filename):
            filenames.append(filename)
    return filenames


def main() -> None:
    DEST_DIR.mkdir(parents=True, exist_ok=True)
    local_filenames = {p.name for p in DEST_DIR.glob("*.png")}
    upstream_filenames = list_upstream_filenames()
    missing = sorted(set(upstream_filenames) - local_filenames)

    if not missing:
        print(f"up to date: {len(local_filenames)} sprites, 0 new downloads")
        return

    for filename in missing:
        url = f"{REPO_RAW_BASE}/{filename}"
        print(f"fetching: {url}")
        dest = DEST_DIR / filename
        with urllib.request.urlopen(url, timeout=30) as response:
            dest.write_bytes(response.read())
    print(f"downloaded {len(missing)} new sprites")


if __name__ == "__main__":
    main()
