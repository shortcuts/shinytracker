# Permissions

## Matrix

| Permission | Type | When Required | Manifest |
|---|---|---|---|
| `BIND_ACCESSIBILITY_SERVICE` | Special (AppOps-style) | Reading the shiny box via screenshots + gestures | Yes |
| `INTERNET` | Normal | Sprite/checklist download (M4 only) | Yes |

## Notes

- `BIND_ACCESSIBILITY_SERVICE` is not requested via `requestPermissions`. The
  user enables it manually in system Accessibility Settings; Android grants
  it there, no runtime prompt.
- `INTERNET` is only needed once the M4 sprite/checklist sync ships. M1–M3's
  core scan loop runs against a locally-bundled small sprite set and needs
  no network access.
- This app requests **no location permission** and **no
  `SYSTEM_ALERT_WINDOW`/overlay permission** — unlike locationjoystick, it
  does not do floating overlays.
