# Permissions

## Matrix

| Permission | Type | When Required | Manifest |
|---|---|---|---|
| `BIND_ACCESSIBILITY_SERVICE` | Special (AppOps-style) | Reading the shiny box via screenshots + gestures | Yes |
| `POST_NOTIFICATIONS` | Runtime (API 33+) | Showing the `IMPORTANCE_LOW` "box scan active" notification while the accessibility service runs | Yes |
| `INTERNET` | Normal | Sprite/checklist download (M4 only) | Yes |
| `SYSTEM_ALERT_WINDOW` | Special (AppOps-style) | Scan-mode floating widget (screenshot + automated grid scan) | Yes |

## Notes

- `BIND_ACCESSIBILITY_SERVICE` is not requested via `requestPermissions`. The
  user enables it manually in system Accessibility Settings; Android grants
  it there, no runtime prompt.
- `POST_NOTIFICATIONS` is declared in the manifest (required by Android lint
  for `NotificationManager.notify()` calls when targeting API 33+). No
  runtime request flow exists yet — `BoxScanAccessibilityService`'s
  notification is best-effort status only, not required for the scan loop
  to function, so a missing grant degrades silently rather than blocking
  anything.
- `INTERNET` is only needed once the M4 sprite/checklist sync ships. M1–M3's
  core scan loop runs against a locally-bundled small sprite set and needs
  no network access.
- `SYSTEM_ALERT_WINDOW` is not requested via `requestPermissions` either. The
  user grants it via `Settings.ACTION_MANAGE_OVERLAY_PERMISSION` — the same
  "special permission" shape as `BIND_ACCESSIBILITY_SERVICE` — needed only to
  show the scan-mode floating widget (`ScanWidgetOverlayService`).
- This app requests **no location permission**.
- Both `BIND_ACCESSIBILITY_SERVICE` and `SYSTEM_ALERT_WINDOW` are gated by
  an onboarding screen (`docs/features/onboarding.md`): the app blocks
  every other screen until both are granted, and re-checks on every
  resume so revoking either one later re-triggers the gate.
