# Technical Constraints

- Min SDK API 31.
- No `Thread.sleep()`. Use `delay()` in coroutines.
- No empty catch blocks. Every `catch` must log or handle.
- No `GlobalScope`. Use `viewModelScope`, `lifecycleScope`, or scoped `CoroutineScope`.
- No memory leaks. `AccessibilityService` must unregister callbacks and close resources in `onUnbind`/`onDestroy`.
- Battery: any foreground service (e.g. for the scan/accessibility service notification) uses an `IMPORTANCE_LOW` notification channel.
