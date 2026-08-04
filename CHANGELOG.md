# Changelog

All notable changes to this project are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/) and follows [Semantic Versioning](https://semver.org/).

## [0.2.0] - 2026-08-04

### Added
- Introduced a stricter public API validation layer with `TauqeetException` for latitude, longitude, date, and environmental inputs.
- Added the unified request-based API with `PrayerRequest` and `PrayerCalculationParameters` to group date, location, timezone, and calculation settings in a single object.
- Added dedicated internal solver components: `IterativeSolver`, `SunriseSunsetSolver`, `AsrSolver`, and `HighLatitudeResolver`.
- Added benchmark coverage using `kotlin.time.measureTime` for the core prayer-time loop, VSOP87 solar coordinate evaluation, and WGS-84 Qibla geodesics.
- Added regression coverage for reference cities and polar-edge scenarios, including Mecca, London, Tokyo, Oslo, New York, and Svalbard.
- Added `RobustnessAndSafetyTest.kt` to exercise invalid coordinate inputs, date boundaries, timezone edge cases, and 10,000-iteration stress/fuzz scenarios.
- Added expanded documentation across [README.md](README.md), [docs/API.md](docs/API.md), [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md), [docs/CALCULATION_METHODS.md](docs/CALCULATION_METHODS.md), [docs/DATA_TYPES.md](docs/DATA_TYPES.md), [docs/EXAMPLES.md](docs/EXAMPLES.md), [docs/PRAYER_TIMES.md](docs/PRAYER_TIMES.md), [docs/QIBLA.md](docs/QIBLA.md), and [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md).

### Changed
- `timezoneOffset` is now fully optional. When omitted, the engine defaults to pure UTC output (`0.0`).
- The request-based overload is now the preferred public entry point for `Tauqeet.computePrayerTimes`, while the existing convenience overloads remain available for backward compatibility.
- Prayer-time resolution metadata now reflects high-latitude and polar-day/polar-night outcomes more accurately.

### Fixed
- Resolved negative millisecond values caused by signed remainder behavior when applying timezone offsets across UTC boundaries. Prayer-time fields now use safe double-modulo normalization.
- Corrected Gregorian-transition handling in `JulianDate` so historical dates before 15 October 1582 are converted accurately.
- Fixed leap-year validation for pre-Gregorian dates, including Julian-calendar leap-day handling.
- Corrected fractional timezone-offset precision by switching from `.toLong()` to `.roundToLong()`.
- Hardened high-latitude fallback behavior and improved solver error propagation so unresolved twilight-hour angles no longer produce misleading downstream values.
- Improved polar-day / polar-night classification through `sameHemisphere` logic when sunrise or sunset values are unavailable.
- Fixed Maghrib fallback and flag propagation for high-latitude cases.
- Hardened `Haversine` distance calculations by clamping the intermediate `a` term to `[0.0, 1.0]`, preventing `NaN` values in antipodal or floating-point edge cases.
- Stabilized Qibla fallback behavior at antipodal conditions so the spherical-law fallback returns a finite bearing and distance.
- Corrected `asTimeParts()` rollover so seconds and minutes are carried into higher units cleanly.
- Corrected `normalizeMeridianAngle()` to wrap values robustly outside the `[-180, 180]` range.
- Corrected `normalizeTime()` so exact `24.0` UTC values roll into the next day.
- Improved `Refraction` calculations by clamping altitude and guarding against `tan(0)` division issues.
- Improved millisecond conversion precision by switching decimal-hour conversions to `.roundToLong()`.
- Resolved CI-blocking compilation issues in the solver and validation paths.
- Removed conflicting GitHub Actions and added the missing Node.js publication-step support required for Kotlin/JS publishing targets.
- Removed deprecated Gradle JVM arguments that caused daemon crashes on JDK 21.

### Refactored
- Reorganized the prayer engine into smaller, more testable internal units: `IterativeSolver`, `SunriseSunsetSolver`, `AsrSolver`, and `HighLatitudeResolver`.
- Simplified solver-result handling so unresolved twilight states are represented explicitly without short-circuiting the broader prayer calculation.
- Refactored high-latitude resolution logic to compute fallback timings and flags from raw solver results.

### Breaking Changes
- None. Existing convenience overloads remain available, but the request-based DSL is now the recommended public entry point.

### Security
- No security vulnerabilities were identified in this release.

### Verification
- Verified locally with `./gradlew :shared:jvmTest`.
- Result: `BUILD SUCCESSFUL`.

### Audit Summary
- Applied a comprehensive corrective audit across the core prayer-time and calendrical logic.

| # | Category | File & Line(s) | Issue Summary | Root Cause | Fix Implemented |
|---|----------|----------------|---------------|------------|-----------------|
| 1️⃣ | Logical / Timestamp Math | `Tauqeet.kt` — lines 191–197 | Negative millisecond values could be produced when `tzOffsetMs + rawTime` was below `-msPerDay`. | Kotlin’s `%` returns a negative remainder for negative operands. | Wrapped the offset addition in a double-modulo expression: `(((it + tzOffsetMs) % msPerDay) + msPerDay) % msPerDay` for all seven prayer-time fields. |
| 2️⃣ | Mathematical / Julian-Day Conversion | `JulianDate.kt` — lines 53–58 | Gregorian correction (`α`) was applied unconditionally, corrupting dates prior to 15 October 1582. | The Meeus algorithm requires the correction only for Julian Day values at or above 2299161. | Added a conditional branch to bypass the Gregorian correction for pre-transition dates. |
| 3️⃣ | Data Handling / Leap-Year Validation | `Validation.kt` — lines 44–46 | Leap-year logic used the Gregorian rule for all years, rejecting valid Julian-calendar leap days such as 29 February 1500. | There was no distinction between Gregorian and Julian calendar rules. | Implemented dual leap-year logic: Gregorian rules for years 1582 and later, and simple `year % 4 == 0` for earlier years. |
| 4️⃣ | API Compatibility / Deprecated Overloads | `Tauqeet.kt` — lines 117–127 and 155–165 | Deprecated `computePrayerTimes` overloads built a `PrayerRequest` without copying the enclosing `Tauqeet` configuration. | Legacy overloads ignored the enclosing instance’s `method`, `madhab`, and related settings. | Added a `calculation { ... }` block that copies the enclosing configuration into the request. |
| 5️⃣ | Timezone-Offset Precision | `Tauqeet.kt` — line 187 | Fractional offsets such as `+5.5` hours were truncated via `.toLong()`, losing the 30-minute component. | `Double * 3600000.0` followed by `Long` truncation. | Switched to `.roundToLong()` and added the corresponding import. |
| 6️⃣ | Iterative Solver Error Propagation | `PrayerSolvers.kt` — lines 48–52 and the surrounding class definition | When `solveHourAngle` returned `null`, the solver still returned a result object with an error flag, potentially feeding bogus values downstream. | `SolverResult.hours` was non-nullable. | Made `hours` nullable and returned `SolverResult(null, ..., error = true)` when the hour-angle solve fails. |
| 7️⃣ | High-Latitude Polar Logic | `PrayerSolvers.kt` — lines 113–154 | Polar-day/night detection relied only on null checks and misclassified some high-latitude cases. | There was no check to determine whether latitude and solar declination were in the same hemisphere. | Added a `sameHemisphere` test and set `isPolarDay` / `isPolarNight` accordingly when sunrise or sunset values are missing. |
| 8️⃣ | Maghrib Fallback & Flags | `PrayerTimes.kt` — lines 225–233 and 235–236 | `finalMaghrib` fell back to `finalSunset` without using the interval fallback value, and high-latitude flags were not set correctly. | The fallback logic and `fallbackApplied` checks used already-corrected values. | Recomputed fallback state from raw solver results and applied explicit Maghrib fallback rules. |
| 9️⃣ | Millisecond Conversion Precision | `PrayerTimes.kt` — lines 355–362 | Conversion from decimal hours to milliseconds used `.toLong()`, truncating fractional values. | Loss of sub-millisecond precision in the final conversion step. | Replaced each `.toLong()` call with `.roundToLong()`. |
| 🔟 | Refraction Boundary Guard | `Refraction.kt` — lines 12–28 | `getRefractionDegrees()` allowed values that could cause division by zero (`tan(0)`) or behave poorly at extreme negative altitudes. | There was no clamping or safety check before applying Bennett’s formula. | Imported `kotlin.math.abs`, clamped altitude to `[-2.0, 89.9]`, and returned `0` when the tangent is effectively zero. |
| 1️⃣1️⃣ | Import Management for New Utilities | `Tauqeet.kt` and `PrayerTimes.kt` | New `.roundToLong()` logic introduced missing imports and caused compilation errors. | The new utility calls were introduced without the corresponding imports. | Added `import kotlin.math.roundToLong` to the affected files. |

## [0.1.0] - 2026-07-23

### Added
- Implemented full VSOP87D periodic terms natively in Kotlin Multiplatform to generate high-precision solar ephemeris coordinates.
- Added cross-platform support for Android, iOS, JVM Desktop, and JavaScript.
- Added iterative prayer-time solvers for Fajr, Sunrise, Dhuhr, Asr, Maghrib, and Isha.
- Added Dhahwa Kubra extraction, high-latitude fallback rules, atmospheric refraction handling, semidiameter support, and WGS-84 geodesic Qibla calculations.
- Added calculation-method presets and Madhhab support.
- Added dual-tier metadata support for UI consumption and advanced astronomical debugging.

### Performance
- Removed boxed-object allocations from the core solar-position loop.
- Preserved millisecond-precision prayer-time results as explicit UTC-based offsets.
- Achieved parity with the reference JavaScript implementation for the covered scenarios.