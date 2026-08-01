# Feature: Settings

## What it does

Reachable from the nav drawer (`ChecklistDrawerContent`). Lets the user
change, after onboarding, the two choices made during setup:

1. Display language (`DisplayLanguage`)
2. The "Enable Scanner" preference

Both write through `OnboardingPreferencesRepository.completeSetup(...)`
immediately on each change -- there is no separate save step or "Continue"
button, unlike the onboarding setup step.

## Turning "Enable Scanner" off

Only flips the persisted `scanner_enabled` DataStore flag. It does not, and
cannot, revoke the OS-level `BIND_ACCESSIBILITY_SERVICE` /
`SYSTEM_ALERT_WINDOW` grants -- Android gives apps no API to revoke a
special permission the user granted through system settings. The onboarding
gate does not re-trigger; see `docs/features/onboarding.md` for the gate
math this relies on.

## Turning "Enable Scanner" on without both permissions granted

Routes the user into `OnboardingScreen`'s existing permission step, via the
same unchanged `onboardingSatisfied` gate described in
`docs/features/onboarding.md`. No dedicated "grant permissions" UI exists in
Settings.
