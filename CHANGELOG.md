# Changelog

All notable changes to this project will be documented in this file.

## [0.2.0] - 2026-08-01

### Added
- **Input Validation Layer**: Implemented strict mathematical boundary validations (via `TauqeetException`) for latitude, longitude, and environmental inputs at the public API boundary.
- **Robust Date Validation**: Integrated chronological checks with full leap-year awareness to prevent invalid date processing.
- **API Ergonomics Upgrade**: Introduced `PrayerRequest` and `PrayerCalculationParameters` to encapsulate date, location, timezone, and prayer settings into a single clean request object.
- **Solver Decomposition**: Split the prayer engine into smaller internal units (`IterativeSolver`, `SunriseSunsetSolver`, `AsrSolver`, and `HighLatitudeResolver`) to improve maintainability and testability.
- **Robustness & Safety Audit**: Added clamping for inverse trig domain guards, replaced unsafe nullable unwrapping in `SolarEphemeris` with `lazy` properties, and hardened all double-validation paths to reject `NaN`/`Infinity` cleanly.
- **Benchmark Guardrails**: Added lightweight KMP-friendly benchmark coverage using `kotlin.time.measureTime` for the core prayer calculation loop, VSOP87 solar coordinate evaluations, and WGS-84 Qibla geodesics.
- **CI Parity Hardening**: Added reference-city and polar-edge regression coverage for Mecca, London, Tokyo, Oslo, New York, and Svalbard to strengthen cross-platform parity and edge-case resilience without introducing third-party timezone dependencies.
- **Robustness Test Suite**: Added `RobustnessAndSafetyTest.kt` to exercise invalid coordinate inputs, date boundaries, timezone edge cases, and 10,000-iteration stress/fuzz scenarios without crashes.
- **Documentation Overhaul**: Created comprehensive deep-dive guides (`ARCHITECTURE.md`, `CALCULATION_METHODS.md`, `DATA_TYPES.md`, `EXAMPLES.md`, `PRAYER_TIMES.md`, `QIBLA.md`, `TROUBLESHOOTING.md`) covering all inputs, outputs, edge cases, and integration troubleshooting.

### Changed
- **Timezone API**: `timezoneOffset` is now fully optional. If omitted, the engine correctly defaults to pure UTC output (0.0). Timezone shifting now perfectly scales and module-wraps via `3,600,000` ms increments.
- **Prayer Time API**: `Tauqeet.computePrayerTimes` now supports a cleaner request-object overload while preserving the existing convenience overloads for backward compatibility.

### Fixed
- **Haversine NaN Hardening**: Clamped the intermediate `a` value in `haversineDistance()` to `[0.0, 1.0]` so antipodal / floating-point edge cases no longer produce `NaN` in spherical distance calculations.
- **Qibla Antipodal Fallback Stability**: Ensured the fallback Qibla path now receives a finite haversine distance, allowing the spherical-law fallback to return a stable bearing and distance instead of degenerating at exact antipode conditions.
- **Time Parts Rollover Repair**: Updated `asTimeParts()` to carry seconds into minutes and minutes into hours cleanly, preventing invalid `60`-second rollover states during timestamp decomposition.
- **Meridian Angle Normalization**: Replaced the previous one-sided threshold logic with robust modulo-based normalization in `normalizeMeridianAngle()` for reliable angle wrapping outside the `[-180, 180]` range.
- **UTC Boundary Roll-Over**: Corrected `normalizeTime()` to roll any exact `24.0` UTC interval into the next day with the inclusive `>= 24.0` check.
- **Historical Calendar Compatibility**: Added explicit Gregorian-transition handling in `dateToJulianDay()` so historical dates before the Gregorian adoption boundary are resolved correctly.
- **Solver Routing Metadata**: Wired the real `resolveSolver(...)` branch selection into the final `PrayerTimesResult`, so `resolutionInfo` and the `flags` bitmask now correctly reflect `NORMAL`, `HIGH_LATITUDE`, `POLAR_DAY`, and `POLAR_NIGHT` outcomes.
- **Polar Edge Regression Coverage**: Expanded `UnifiedApiTest.kt` to exercise polar-day, polar-night, and high-latitude fallback scenarios through the unified `PrayerRequest` DSL path.
- **Publishing Pipeline**: Removed conflicting GitHub Actions and added a missing Node.js step to ensure Kotlin/JS multiplatform publishing targets complete successfully.
- **Gradle JVM Args**: Removed deprecated `-XX:+NewRatio=3` from `gradle.properties` that caused Gradle Daemon crashes on JDK 21.

## [0.1.0] - 2026-07-23

### Added
- **Core Engine**: Implemented full `VSOP87D` periodic terms natively in Kotlin Multiplatform to generate ultra-precise solar ephemeris coordinates.
- **Cross-Platform Compatibility**: Deployed modules for Android, iOS, JVM Desktop, and JS (Node.js/Browser).
- **Prayer Engine**: Iterative hour-angle geometric solvers for Fajr, Sunrise, Dhuhr, Asr, Maghrib, and Isha.
- **Dhahwa Kubra**: Included the Islamic Midday point (Dhahwa Kubra) dynamically extracted from twilight sweeps.
- **High Latitude Rules**: Graceful `null` fallbacks for Polar Days and Midnight Suns including `MIDDLE_OF_NIGHT`, `SEVENTH_OF_NIGHT`, and `TWILIGHT_ANGLE`.
- **Atmospheric Refraction**: Upgraded all twilight boundaries to factor in Bennett's Refraction (1982) dynamically scaling against altitude profiles.
- **Semidiameter**: Sunrise and Sunset targets dynamically map real-time solar disc spread rather than using a static arc-minute radius.
- **Geodesic Qibla Routing**: Calculates exact geodesic WGS-84 Vincenty Inverse formulations for Mecca, complete with surface distance measurement (`distanceKm`).
- **Antipodal Qibla Fallback**: Included the Spherical Law of Cosines fallback for edge-case tracking at exact antipode boundaries.
- **Calculation Methods & Madhabs**: Pre-packaged configurations for all major global institutes (MWL, ISNA, Karachi, Makkah, Egypt, etc.), along with Shafii and Hanafi Shadow logic for Asr.
- **Dual-Tier Metadata**: Added robust simple UI `PrayerTimesMetadata` configuration tracking and optional `includeAdvancedMetadata` for deep astronomical debugging per event.

### Performance & Optimization
- **Zero-Allocation Execution**: Completely eliminated boxed object evaluations during the tight 75x recursive sun positioning loops.
- **Long Precision Engine**: Defends against downstream Double floating-point string drift by extracting `PrayerTimesResult` as absolute explicit milliseconds since midnight UTC.
- **Strict Parity**: Achieved absolute 100% mathematical parity (<0.000ms deviation across tests) compared directly to `tauqeet-js`.