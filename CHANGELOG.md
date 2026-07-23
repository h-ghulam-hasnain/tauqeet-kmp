# Changelog

All notable changes to this project will be documented in this file.

## [0.1.0] - 2024-07-23

### Added
- **Core Engine**: Implemented full `VSOP87D` periodic terms natively in Kotlin Multiplatform to generate ultra-precise solar ephemeris coordinates.
- **Cross-Platform Compatibility**: Deployed modules for Android, iOS, JVM Desktop, and JS (Node.js/Browser).
- **Prayer Engine**: Iterative hour-angle geometric solvers for Fajr, Sunrise, Dhuhr, Asr, Maghrib, and Isha.
- **Dhahwa Kubra**: Included the Islamic Midday point (Dhahwa Kubra) dynamically extracted from twilight sweeps.
- **High Latitude Rules**: Graceful fallbacks for Polar Days and Midnight Suns including `MIDDLE_OF_NIGHT`, `SEVENTH_OF_NIGHT`, and `TWILIGHT_ANGLE`.
- **Atmospheric Refraction**: Upgraded all twilight boundaries to factor in Bennett's Refraction (1982) dynamically scaling against altitude profiles.
- **Semidiameter**: Sunrise and Sunset targets dynamically map real-time solar disc spread rather than using a static arc-minute radius.
- **Qibla Routing**: Calculates exact geodesic Vincenty formulations for Mecca.
- **Calculation Methods & Madhabs**: Pre-packaged configurations for all major global institutes (MWL, ISNA, Karachi, Makkah, Egypt, etc.), along with Shafii and Hanafi Shadow logic for Asr.
- **Metadata**: Included robust algorithmic configuration reporting per API call directly in `PrayerTimesResult`.

### Performance & Optimization
- **Zero-Allocation Execution**: Completely eliminated boxed object evaluations during the tight 75x recursive sun positioning loops.
- **Long Precision Engine**: Defends against downstream Double floating-point string drift by extracting `PrayerTimesResult` as absolute explicit milliseconds since midnight UTC.
- 100% mathematical parity (<0.019m) compared directly to `tauqeet-js`.
