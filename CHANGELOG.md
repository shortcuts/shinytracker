# Changelog

## [0.2.0](https://github.com/shortcuts/shinytracker/compare/v0.1.0...v0.2.0) (2026-08-03)


### Features

* **app:** add nav drawer with hamburger icon shell ([1cf7efd](https://github.com/shortcuts/shinytracker/commit/1cf7efdfe7e1dd6a3c976c30baa7b8a81dc9f6db))
* **app:** add settings screen for post-onboarding preferences ([2fc6ae6](https://github.com/shortcuts/shinytracker/commit/2fc6ae63b36b5259ba72c835277860190f094be0))
* **app:** gate app behind accessibility + overlay onboarding ([467b92a](https://github.com/shortcuts/shinytracker/commit/467b92abcc17ab540aca1e5bad7ccff4acb01132))
* **app:** make checklist the default home screen ([037d335](https://github.com/shortcuts/shinytracker/commit/037d335d18fe3439dcc757e63a956b7cfb2686d0))
* **app:** match locationjoystick layout for settings/onboarding ([c9b13b8](https://github.com/shortcuts/shinytracker/commit/c9b13b8fb485c8254309ad1cf23321e9f6534a1f))
* **app:** wire checklist sync button on settings screen ([9b46655](https://github.com/shortcuts/shinytracker/commit/9b4665546bfff4341ed38e74ad7a34ef21ff3d22))
* **checklist:** cross-check eligibility against leekduck.com ([5709b4b](https://github.com/shortcuts/shinytracker/commit/5709b4bfe56a6ff8a2b29b0ff48207d32ac861f3))
* **checklist:** fade in sprite tiles on composition ([c6e7356](https://github.com/shortcuts/shinytracker/commit/c6e7356998d7c1e7dbe95c9acd9a90aa8f533b6a))
* **checklist:** owner + shared read-only checklist UI, profile sharing ([e6c857d](https://github.com/shortcuts/shinytracker/commit/e6c857dc57549c08e72f0a34fb28d33b925cedec))
* **checklist:** pin region header with LazyColumn stickyHeader ([57cd75f](https://github.com/shortcuts/shinytracker/commit/57cd75ffcdce72e07fc385d157f1478ec4b28ad4))
* **checklist:** tap tile to toggle caught/uncaught ([0dddf1e](https://github.com/shortcuts/shinytracker/commit/0dddf1e7c492310ad1a685a41b62e9d4944caf60))
* **core:** scaffold core:datastore module ([53838f2](https://github.com/shortcuts/shinytracker/commit/53838f2d443b0c7f937bb51efe191a726044ce81))
* **data:** add delete to CaughtRepository/CaughtDao ([a1db649](https://github.com/shortcuts/shinytracker/commit/a1db649ee128b2f50f2acff99eb6f80be85a5c93))
* **designsystem:** swap teal accent for shiny-yellow ([8e615e8](https://github.com/shortcuts/shinytracker/commit/8e615e8d7655766d02f60d673c030892d9570398))
* **m1:** scaffold app, core:model, core:designsystem shells ([607e585](https://github.com/shortcuts/shinytracker/commit/607e58541b9784e5dbce118e895468a09c3111f1))
* **onboarding:** add language pick + optional scanner gating ([7b983b9](https://github.com/shortcuts/shinytracker/commit/7b983b96957bd646e3c43e14969fdf5df38c1009))
* **scan:** add accessibility service for box screenshot capture ([88412e1](https://github.com/shortcuts/shinytracker/commit/88412e1e89a07134341af790e61ac41bf5afe29c))
* **scan:** add floating widget for screenshot + automated grid scan ([5685bcf](https://github.com/shortcuts/shinytracker/commit/5685bcf89784d40500c8f2ff581744a549e0445b))
* **scan:** sprite matching + caught tracking + full scan loop ([4c372a9](https://github.com/shortcuts/shinytracker/commit/4c372a9211bbb7d663ccc97b28692fc276e20806))
* **sprites,data:** full sprite sync + checklist eligibility source ([b9e3a26](https://github.com/shortcuts/shinytracker/commit/b9e3a2612e192432c31d9a67d3f407623f313650))
* **sprites:** add confusable-pairs diagnostic tool ([30ca935](https://github.com/shortcuts/shinytracker/commit/30ca9350f60cb7b9aac2a529f721e020d06d7f2e))
* **sprites:** add weight-search tool for ensemble tuning ([c36c20a](https://github.com/shortcuts/shinytracker/commit/c36c20a84f7b444dc666b7de385c4e96896bcbbe))
* **sprites:** explode checklist into form/costume variants ([906a682](https://github.com/shortcuts/shinytracker/commit/906a6825e12778a7cd936eb98a62a99f2554a293))
* **sprites:** index DexEntry with real Pokemon dex data ([35034e4](https://github.com/shortcuts/shinytracker/commit/35034e4c8ed502965ab7c9c2b052b6593d2fbbf1))
* **sprites:** precomputed multi-feature descriptor ensemble matching ([ab5a62d](https://github.com/shortcuts/shinytracker/commit/ab5a62d0acc48ac336f9fe6150ddcac627b058d4))
* **ui:** redesign scan + checklist screens, real sprites, advanced filters ([e7aef02](https://github.com/shortcuts/shinytracker/commit/e7aef0287530a8a9e7a01172b8e512f993b505e1))


### Bug Fixes

* **checklist:** drop per-tile fade-in causing scroll jank ([ddd4df7](https://github.com/shortcuts/shinytracker/commit/ddd4df7b2c94a785dd73cf3b8b1bb71049a37910))
* **checklist:** fix sprite grid to 3 columns per row ([4ddabb0](https://github.com/shortcuts/shinytracker/commit/4ddabb0c71e0a16ab7cee70c51ba366a98ffd569))
* **checklist:** spread grid tiles across row width ([34afdcc](https://github.com/shortcuts/shinytracker/commit/34afdcc4a1addc7c30ec18134800d4c2cfe7d452))
* **checklist:** use non-deprecated hiltViewModel import ([1deed23](https://github.com/shortcuts/shinytracker/commit/1deed23e36753709a05d6471e41c543404968783))
* **checklist:** virtualize sprite tile rows in LazyColumn ([6473423](https://github.com/shortcuts/shinytracker/commit/6473423169728b17e3b41e5910c55ab6e8f90359))
* **data:** match caught records by full dex identity ([78b1ae3](https://github.com/shortcuts/shinytracker/commit/78b1ae3815bf949618d2258f0a28f96eba315dc8))
* **di:** target @ApplicationContext at param to silence KT-73255 ([9c19e6e](https://github.com/shortcuts/shinytracker/commit/9c19e6e76ccd0484823727dec41b707f95722030))
* **onboarding:** gate permissions step on scannerEnabledPref ([a63ebf3](https://github.com/shortcuts/shinytracker/commit/a63ebf36098278f681838cf0f7135571ba0c43a0))
