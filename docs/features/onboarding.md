# Feature: Onboarding Gate

## What it does

Blocks all app usage until both `BIND_ACCESSIBILITY_SERVICE` and
`SYSTEM_ALERT_WINDOW` (overlay) are granted. Keeps the scan/checklist
screens free of permission-setup UI, since they can assume both are
already on.

## Flow

1. `MainActivity` checks both permissions synchronously in `onCreate`,
   before `setContent`, so the nav host's `startDestination` is correct on
   the first frame: both granted -> the checklist screen
   (`ChecklistRoute.OWNER`), otherwise the onboarding screen
   (`ONBOARDING_ROUTE`).
2. `OnboardingScreen` shows one card per permission with its granted
   state and, when missing, a button to
   `Settings.ACTION_ACCESSIBILITY_SETTINGS` or
   `Settings.ACTION_MANAGE_OVERLAY_PERMISSION`.
3. `MainActivity.onResume()` re-checks both permissions every time the app
   returns to the foreground -- including after the user grants or
   revokes one in system Settings.
4. A `LaunchedEffect` keyed on both booleans reacts to every re-check:
   both granted while on `ONBOARDING_ROUTE` -> navigate to
   `ChecklistRoute.OWNER`
   (auto-advance, no manual "Continue" button); either missing while on
   any other route -> navigate back to `ONBOARDING_ROUTE`. Both directions
   clear the back stack. This is what re-triggers the gate if a
   permission is revoked mid-session, not just on first install.
5. The shared read-only checklist route (`ChecklistRoute.SHARED_PATTERN`,
   reached only via an incoming file `Intent` -- see
   `docs/features/checklist.md`) is exempt: viewing a friend's exported
   list needs neither permission.

## Out of scope

No persisted "onboarding complete" flag -- gating is always a live check
of current permission state, which is what makes revocation re-trigger
the gate for free instead of needing separate handling.
