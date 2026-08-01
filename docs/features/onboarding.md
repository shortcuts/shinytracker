# Feature: Onboarding Gate

## What it does

First-run setup with two parts:

1. **Setup step (always required).** The user picks a display-language
   preference (English/Japanese/Chinese/French -- controls which key of
   `DexEntry.localizedNames` the checklist shows species names in, not the
   app's own UI language) and optionally checks "Enable Scanner." Both are
   persisted to `:core:datastore` when "Continue" is tapped.
2. **Permission step (only if "Enable Scanner" was checked).** Blocks app
   usage until both `BIND_ACCESSIBILITY_SERVICE` and `SYSTEM_ALERT_WINDOW`
   (overlay) are granted. Skipped entirely if the user left "Enable
   Scanner" unchecked -- that user can use the app as a manual/regular
   shiny tracker and is never prompted for either permission.

## Flow

1. `MainActivity` reads both persisted preferences
   (`OnboardingPreferencesRepository.displayLanguageChoice`/
   `.scannerEnabled`) and both permission states in `setContent`, computing
   `onboardingSatisfied = displayLanguageChoice != null && (!scannerEnabledPref
   || (accessibilityGranted && overlayGranted))`.
2. `OnboardingScreen` renders the setup step while
   `displayLanguageChoice == null`, otherwise the permission step (only
   reachable when `scannerEnabledPref` is true and a permission is still
   missing).
3. Tapping "Continue" on the setup step calls
   `OnboardingPreferencesRepository.completeSetup(language, scannerEnabled)`,
   persisting both choices in one DataStore transaction.
4. `MainActivity.onResume()` re-checks both *permission* states every time
   the app returns to the foreground (unchanged from before). The two
   DataStore preferences don't need a resume-time re-check -- they only
   change via this screen's own writes, never externally.
5. A `LaunchedEffect` keyed on the permission booleans and the two
   preference values reacts to every change: `onboardingSatisfied` becomes
   true while on `ONBOARDING_ROUTE` -> navigate to `ChecklistRoute.OWNER`;
   becomes false while on any other route -> navigate back to
   `ONBOARDING_ROUTE`. Both directions clear the back stack. This is what
   re-triggers the gate if a scanner-enabled user later revokes a
   permission -- unchanged from before.
6. The shared read-only checklist route (`ChecklistRoute.SHARED_PATTERN`,
   reached only via an incoming file `Intent` -- see
   `docs/features/checklist.md`) is exempt from this whole gate: viewing a
   friend's exported list needs neither a completed setup step nor either
   permission. It still respects the *display-language* preference if one
   has been set (defaults to English otherwise) -- see
   `docs/domain-models.md`'s `DisplayLanguage` entry.

## Out of scope

- No settings screen to change the display language or scanner preference
  after onboarding -- both are set once, during setup.
- No full app UI localization (`values-xx/strings.xml`,
  `locale_config.xml`, per-app-language APIs). The onboarding screen's own
  chrome and every other screen's text stays in English regardless of the
  chosen display language.
