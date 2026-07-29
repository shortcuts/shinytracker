# Constants

A single `AppConstants`-style object (or per-domain objects nested under it,
e.g. `ScanConstants` for crop thresholds/similarity cutoff, `SyncConstants`
for sprite-sync intervals) so constants don't scatter across files. Lives in
`:core:common`.

Specific constant names/values are not decided yet — they get filled in as
milestones land.

## Rules

- No new top-level/companion constant outside the shared constants object. Add there.
- Modules needing constants: declare `implementation(project(":core:common"))` in `build.gradle.kts`.
- Exception: `:core:model` is pure JVM, cannot depend on `core:common`. Constants only used in `:core:model` stay there.
