# Release Notes — v0.2.2

**Released:** 2026-11-18
**Compatibility:** Drop-in replacement for 0.2.1 — no API changes, no behavior changes.

## 📚 Documentation (primary change)

- Comprehensive KDoc added to all 11 public API files
- Every class, function, parameter, and return value is now documented
- Android Studio autocomplete shows full descriptions
- Dokka-generated Javadoc on Maven Central is now complete
- JS exports have code examples for browser and Node.js consumers

## 🛠️ Build & CI

- Enabled Gradle configuration cache (6–10s saved per build)
- Enabled Gradle build cache
- Scoped `build.yml` to JVM/JS only — iOS compiles on release tags
- Added `~/.konan` cache for CI

## 🛡️ Compatibility

- **Zero API changes** — all existing code continues to compile and run
- **Zero math logic changes** — algorithm parity with 0.2.1 maintained
- Version bump only — safe to upgrade

## Migration

Replace `0.2.1` with `0.2.2` in your `build.gradle.kts` or `package.json`. No code changes required.
