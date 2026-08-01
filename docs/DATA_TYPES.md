# 📊 I/O Data Types & Error States

This document details all data types returned by `tauqeet-kmp`, what they mean, and their edge cases (error states).

## `QiblaResult`
Returned by `tauqeetQibla()` and `Tauqeet().qiblaDirection()`.

```kotlin
data class QiblaResult(
    val bearing: Double,      // Bearing in degrees from True North (0 to <360)
    val distanceKm: Double    // Distance to the Kaaba in kilometers
)
```

### Possible Outcomes (Qibla)
- **Normal Usage**: Returns a `QiblaResult` with exact `bearing` and `distanceKm`.
- **At the Antipode**: If standing exactly opposite to Mecca, Vincenty will fail, but the library automatically falls back to Haversine/Spherical formulas and still returns a valid `QiblaResult`.
- **Exactly at the Kaaba**: If the distance to the Kaaba is less than 1 meter, all directions face the Kaaba. The function returns `null`.

## `PrayerTimesResult`
Returned by `Tauqeet().computePrayerTimes()`.

```kotlin
data class PrayerTimesResult(
    val fajr: Long?,
    val sunrise: Long?,
    val dhahwaKubra: Long?,
    val dhuhr: Long?,
    val asr: Long?,
    val maghrib: Long?,
    val isha: Long?,
    val metadata: PrayerTimesMetadata? = null,
    val astronomicalMetadata: AstronomicalMetadata? = null,
    val flags: Int = 0,
    val resolutionInfo: ResolutionInfo? = null
)
```
*Note: All time values (`Long`) are returned as milliseconds since midnight local time.*

### Possible Outcomes (Prayer Times)
- **Normal Usage**: All time properties (`fajr` to `isha`) return a `Long` value representing milliseconds since midnight.
- **High Latitude / Polar Day / Polar Night**: In places where the sun never sets or doesn't reach the twilight angle:
  - The engine now classifies the route through `resolutionInfo.solver` as `NORMAL`, `HIGH_LATITUDE`, `POLAR_DAY`, or `POLAR_NIGHT`.
  - The companion `flags` bitmask exposes the same branch information without requiring the caller to infer it from `null`s alone.
  - If a specific prayer time cannot occur astronomically, the chosen `HighLatitudeRule` is applied to produce a stable result when possible.
- **Recommendation**: Always use safe-calls (`?.`) and handle nulls in your UI (e.g. `times.fajr?.toTimeString() ?: "--:--"`).

### Formatter Extensions
The library provides convenient extensions on `Long` to format these millisecond values into strings:
- `toTimeString()`: e.g., `"05:14:32"`
- `toTimeStringShort()`: e.g., `"05:14"`
- `toISOTimeStringWithMillis()`: e.g., `"05:14:32.412"`

## `PrayerTimesMetadata`
Included inside `PrayerTimesResult` by default (if the calculation engine ran).

```kotlin
data class PrayerTimesMetadata(
    val method: String,
    val madhab: String,
    val highLatitudeRule: String,
    val isPolarDay: Boolean,     // True if the sun does not set
    val isPolarNight: Boolean    // True if the sun does not rise
)
```

## `ResolutionInfo`
The solver-routing outcome is exposed separately so the caller can inspect the branch taken by the engine.

```kotlin
enum class SolverKind { NORMAL, HIGH_LATITUDE, POLAR_DAY, POLAR_NIGHT }

data class ResolutionInfo(
    val solver: SolverKind,
    val ruleApplied: HighLatitudeRule? = null
)
```

## `AstronomicalMetadata` (Opt-in)
You must pass `includeAdvancedMetadata = true` to receive this. It is highly detailed and meant for specialized scientific/astronomical displays.

```kotlin
data class AstronomicalMetadata(
    val fajr: TwilightMetadata?,
    val sunrise: SunriseSunsetMetadata?,
    val dhahwaKubra: DhahwaKubraMetadata?,
    val dhuhr: DhuhrMetadata?,
    val asr: AsrMetadata?,
    val maghrib: SunriseSunsetMetadata?,
    val isha: TwilightMetadata?
)
```

These metadata structs expose internal details like:
- `status`: A `PrayerStatus` enum (`SUCCESS`, `POLAR_DAY`, `POLAR_NIGHT`, etc.)
- `DEC_deg`: Solar declination at that exact moment.
- `EOT_min`: Equation of time at that exact moment.
- `iterations`: How many loops the iterative solver took to converge.
- `refraction_deg`: The calculated atmospheric refraction in degrees.
