# 🚀 Complete API Examples

This document provides exhaustive, real-world Kotlin code examples covering every input parameter, all geographic scenarios, and all possible edge cases documented in [PRAYER_TIMES.md](PRAYER_TIMES.md).

---

## Quick Reference: Required vs Optional Inputs

```kotlin
val tauqeet = Tauqeet(
    // --- ALL OPTIONAL (Engine Configuration) ---
    method             = CalculationMethod.KARACHI,    // Default: KARACHI
    madhab             = Madhab.HANAFI,                // Default: HANAFI
    highLatitudeRule   = HighLatitudeRule.MIDDLE_OF_NIGHT, // Default: MIDDLE_OF_NIGHT
    elevationMeters    = 0.0,                          // Default: 0.0
    temperatureC       = 12.714,                       // Default: 12.714
    pressureMbar       = 1010.0,                       // Default: 1010.0
    customMethodParams = null                          // Default: null
)

val times = tauqeet.computePrayerTimes(
    // --- REQUIRED (Computation) ---
    year  = 2026,       // Int
    month = 8,          // Int (1-12)
    day   = 1,          // Int (1-31)
    lat   = 24.8607,    // Double (-90.0 to 90.0)
    lng   = 67.0011,    // Double (-180.0 to 180.0)
    // --- OPTIONAL (Computation) ---
    timezoneOffset        = 5.0,  // Double, Default: 0.0 (UTC)
    includeAdvancedMetadata = false // Boolean, Default: false
)
```

---

## Scenario 1: Normal Days (Karachi, Pakistan — All Times Present)

This is the most common case. The sun rises and sets fully, and all twilight angles (for Fajr and Isha) are reached before midnight. All seven time fields are populated as valid `Long` values.

```kotlin
import com.tauqeet.library.Tauqeet
import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.Madhab
import com.tauqeet.library.toTimeString
import com.tauqeet.library.toTimeStringShort

fun prayerTimesNormalDay() {
    val tauqeet = Tauqeet(
        method = CalculationMethod.KARACHI,
        madhab = Madhab.HANAFI
    )

    val times = tauqeet.computePrayerTimes(
        year = 2026, month = 8, day = 1,
        lat = 24.8607, lng = 67.0011,
        timezoneOffset = 5.0 // Pakistan Standard Time (UTC+5)
    )

    // On a normal day, all of these are safe to use without null checks.
    // But it is always best practice to use safe calls (?.)
    println("Fajr:        ${times.fajr?.toTimeStringShort() ?: "N/A"}")
    println("Sunrise:     ${times.sunrise?.toTimeStringShort() ?: "N/A"}")
    println("Dhahwa Kubra:${times.dhahwaKubra?.toTimeStringShort() ?: "N/A"}")
    println("Dhuhr:       ${times.dhuhr?.toTimeStringShort() ?: "N/A"}")
    println("Asr:         ${times.asr?.toTimeStringShort() ?: "N/A"}")
    println("Maghrib:     ${times.maghrib?.toTimeStringShort() ?: "N/A"}")
    println("Isha:        ${times.isha?.toTimeStringShort() ?: "N/A"}")

    // Metadata is always populated.
    println("\nMethod: ${times.metadata?.method}")
    println("Madhab: ${times.metadata?.madhab}")
    println("isPolarDay: ${times.metadata?.isPolarDay}")   // false
    println("isPolarNight: ${times.metadata?.isPolarNight}") // false
}
```

**Expected Output (approximate):**
```
Fajr:         04:21
Sunrise:      05:43
Dhahwa Kubra: 12:12
Dhuhr:        12:42
Asr:          16:26
Maghrib:      19:42
Isha:         21:01
```

---

## Scenario 2: Continuous Twilight (London, UK — Fajr/Isha Estimated)

In the UK during summer, the sun *does* set, but it never dips below 18° below the horizon. This means true astronomical Fajr and Isha do not occur. The engine detects this (`CONTINUOUS_TWILIGHT` status) and applies your chosen `highLatitudeRule` to gracefully estimate them. You can verify this via `astronomicalMetadata`.

```kotlin
import com.tauqeet.library.Tauqeet
import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.Madhab
import com.tauqeet.library.prayers.HighLatitudeRule
import com.tauqeet.library.prayers.PrayerStatus
import com.tauqeet.library.toTimeStringShort

fun prayerTimesContinuousTwilight() {
    val tauqeet = Tauqeet(
        method = CalculationMethod.MWL,   // 18° Fajr / 17° Isha
        madhab = Madhab.SHAFI,
        highLatitudeRule = HighLatitudeRule.SEVENTH_OF_NIGHT
    )

    // London in late July — sun sets but never reaches full darkness
    val times = tauqeet.computePrayerTimes(
        year = 2026, month = 7, day = 25,
        lat = 51.5072, lng = -0.1276,
        timezoneOffset = 1.0, // British Summer Time (UTC+1)
        includeAdvancedMetadata = true // Enable to see CONTINUOUS_TWILIGHT status
    )

    // Fajr and Isha are ESTIMATED values, not exact astronomical ones.
    // Always handle them safely.
    println("Fajr (estimated): ${times.fajr?.toTimeStringShort() ?: "Unavailable"}")
    println("Isha (estimated): ${times.isha?.toTimeStringShort() ?: "Unavailable"}")
    println("Sunrise: ${times.sunrise?.toTimeStringShort()}")
    println("Maghrib: ${times.maghrib?.toTimeStringShort()}")

    // Verify that the engine flagged this as a fallback calculation
    val fajrStatus = times.astronomicalMetadata?.fajr?.status
    if (fajrStatus == PrayerStatus.CONTINUOUS_TWILIGHT) {
        println("\n⚠️ UI Note: Fajr angle was never reached. Time is a SEVENTH_OF_NIGHT estimate.")
    }
    println("Fajr solver status: $fajrStatus") // CONTINUOUS_TWILIGHT
}
```

---

## Scenario 3: Polar Day / Midnight Sun (Tromsø, Norway — No Sunset)

Above the Arctic Circle in summer, the sun never sets. `isPolarDay` is `true`. Sunrise and Sunset do not occur as distinct events. The engine falls back to estimating all night-dependent times using `highLatitudeRule`.

```kotlin
import com.tauqeet.library.Tauqeet
import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.Madhab
import com.tauqeet.library.prayers.HighLatitudeRule
import com.tauqeet.library.toTimeStringShort

fun prayerTimesPolarDay() {
    val tauqeet = Tauqeet(
        method = CalculationMethod.MWL,
        madhab = Madhab.SHAFI,
        highLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT // Splits assumed night in half
    )

    // Tromsø, Norway — Summer Solstice (The sun never sets)
    val times = tauqeet.computePrayerTimes(
        year = 2026, month = 6, day = 21,
        lat = 69.6492, lng = 18.9553,
        timezoneOffset = 2.0 // Central European Summer Time (UTC+2)
    )

    // ALWAYS check isPolarDay before rendering times.
    if (times.metadata?.isPolarDay == true) {
        println("🌞 POLAR DAY: The sun does not set today.")
        println("Times shown are estimated using ${times.metadata?.highLatitudeRule} rule.\n")
    }

    // sunrise and maghrib will likely be null or estimated
    println("Fajr (estimated):   ${times.fajr?.toTimeStringShort() ?: "N/A — Sun never sets"}")
    println("Sunrise:            ${times.sunrise?.toTimeStringShort() ?: "N/A — Sun stays above horizon"}")
    println("Dhuhr:              ${times.dhuhr?.toTimeStringShort()}") // Always computable
    println("Asr:                ${times.asr?.toTimeStringShort()}")   // Always computable
    println("Maghrib (estimated):${times.maghrib?.toTimeStringShort() ?: "N/A — Sun never sets"}")
    println("Isha (estimated):   ${times.isha?.toTimeStringShort() ?: "N/A"}")
}
```

---

## Scenario 4: Polar Night (Tromsø, Norway — No Sunrise)

In the depths of winter, the sun never rises above the horizon. `isPolarNight` is `true`. Times like Dhuhr, Asr, Sunrise, and Maghrib which depend on sunlight are returned as `null`. Your app should fall back to a regional fatwa or nearest-city times.

```kotlin
import com.tauqeet.library.Tauqeet
import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.Madhab
import com.tauqeet.library.prayers.HighLatitudeRule
import com.tauqeet.library.toTimeStringShort

fun prayerTimesPolarNight() {
    val tauqeet = Tauqeet(
        method = CalculationMethod.MWL,
        madhab = Madhab.SHAFI,
        highLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT
    )

    // Tromsø, Norway — Winter (The sun does not rise)
    val times = tauqeet.computePrayerTimes(
        year = 2026, month = 12, day = 21,
        lat = 69.6492, lng = 18.9553,
        timezoneOffset = 1.0 // CET (UTC+1)
    )

    // ALWAYS check isPolarNight before rendering
    if (times.metadata?.isPolarNight == true) {
        println("🌑 POLAR NIGHT: The sun does not rise today.")
        println("Showing null for sun-dependent prayers. Fall back to regional times.\n")
    }

    // Times reliant on the sun being above the horizon are null
    println("Fajr:    ${times.fajr?.toTimeStringShort() ?: "N/A — No sunrise"}")
    println("Sunrise: ${times.sunrise?.toTimeStringShort() ?: "null ← expected"}")
    println("Dhuhr:   ${times.dhuhr?.toTimeStringShort() ?: "null ← expected"}")
    println("Asr:     ${times.asr?.toTimeStringShort() ?: "null ← expected"}")
    println("Maghrib: ${times.maghrib?.toTimeStringShort() ?: "null ← expected"}")
    println("Isha:    ${times.isha?.toTimeStringShort() ?: "N/A — No sunset"}")

    // Recommended: UI fallback logic
    if (times.metadata?.isPolarNight == true) {
        // Show a banner and fallback to nearest-city or Mecca times
        showFallbackBanner("Polar Night detected. Displaying Mecca prayer times.")
    }
}

fun showFallbackBanner(message: String) {
    println("⚠️ $message")
}
```

---

## Scenario 5: High Altitude & Atmospheric Refraction (Mount Everest)

At high altitudes, the geometric "dip" of the horizon means the sun rises earlier and sets later than at sea level. Cold, low-pressure air also changes the refraction of sunlight. Supply real environmental data to calculate the most accurate possible times.

```kotlin
import com.tauqeet.library.Tauqeet
import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.toTimeStringShort

fun prayerTimesHighAltitude() {
    val tauqeet = Tauqeet(
        method = CalculationMethod.MAKKAH,
        elevationMeters = 8848.0,  // Summit of Mount Everest (meters)
        temperatureC    = -30.0,   // Extreme cold (°C)
        pressureMbar    = 300.0    // Very low atmospheric pressure (mbar)
    )

    val times = tauqeet.computePrayerTimes(
        year = 2026, month = 4, day = 15,
        lat = 27.9881, lng = 86.9250,
        timezoneOffset = 5.75 // Nepal Standard Time (UTC+5:45)
    )

    // Sunrise will be several minutes EARLIER than sea-level Kathmandu
    println("Everest Sunrise: ${times.sunrise?.toTimeStringShort()}")
    println("Everest Sunset:  ${times.maghrib?.toTimeStringShort()}")

    // Compare to a sea-level observer at the same coordinates
    val seaLevel = Tauqeet(method = CalculationMethod.MAKKAH)
    val seaTimes = seaLevel.computePrayerTimes(
        year = 2026, month = 4, day = 15,
        lat = 27.9881, lng = 86.9250, timezoneOffset = 5.75
    )
    println("Sea-level Sunrise: ${seaTimes.sunrise?.toTimeStringShort()}")
}
```

---

## Scenario 6: Fractional Timezone Offsets (India & Nepal)

The `timezoneOffset` parameter is a `Double`, supporting half- and quarter-hour offsets used in countries like India (UTC+5:30) and Nepal (UTC+5:45).

```kotlin
fun prayerTimesIndia() {
    val tauqeet = Tauqeet(method = CalculationMethod.KARACHI, madhab = Madhab.HANAFI)
    val times = tauqeet.computePrayerTimes(
        year = 2026, month = 8, day = 1,
        lat = 28.6139, lng = 77.2090,   // New Delhi
        timezoneOffset = 5.5            // IST = UTC+5:30 = 5.5 hours
    )
    println("Delhi Fajr: ${times.fajr?.toTimeStringShort()}")
}

fun prayerTimesNepal() {
    val tauqeet = Tauqeet(method = CalculationMethod.KARACHI, madhab = Madhab.HANAFI)
    val times = tauqeet.computePrayerTimes(
        year = 2026, month = 8, day = 1,
        lat = 27.7172, lng = 85.3240,   // Kathmandu
        timezoneOffset = 5.75           // NPT = UTC+5:45 = 5.75 hours
    )
    println("Kathmandu Fajr: ${times.fajr?.toTimeStringShort()}")
}
```

---

## Scenario 7: Custom Calculation Method Parameters

If your app needs a method not in the built-in list (e.g., a regional religious authority with unique angles), you can define fully custom `fajrAngle`, `ishaAngle`, and `ishaInterval` values.

```kotlin
import com.tauqeet.library.Tauqeet
import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.CalculationMethodParameters

fun prayerTimesCustomMethod() {
    val customParams = CalculationMethodParameters(
        fajrAngle   = 15.5,  // Custom Fajr depression angle in degrees
        ishaAngle   = 15.0,  // Custom Isha depression angle in degrees
        ishaInterval = 0,    // 0 = use angle-based Isha (not fixed-minutes interval)
        maghribAngle = 0.0   // 0 = use Sunset as Maghrib
    )

    val tauqeet = Tauqeet(
        method = CalculationMethod.CUSTOM,
        customMethodParams = customParams
    )

    val times = tauqeet.computePrayerTimes(
        year = 2026, month = 8, day = 1,
        lat = 24.8607, lng = 67.0011, timezoneOffset = 5.0
    )
    println("Custom Fajr: ${times.fajr?.toTimeStringShort()}")
}
```

---

## Scenario 8: Retrieving Scientific Astronomical Data (VSOP87 Metadata)

Set `includeAdvancedMetadata = true` to get the raw internal VSOP87 solar variables at the exact moment of every prayer event. Designed for astronomy apps, scientific logging, or high-latitude debugging.

```kotlin
import com.tauqeet.library.Tauqeet
import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.PrayerStatus

fun prayerTimesWithMetadata() {
    val tauqeet = Tauqeet(method = CalculationMethod.MWL)

    val times = tauqeet.computePrayerTimes(
        year = 2024, month = 3, day = 20, // Spring Equinox
        lat = 40.7128, lng = -74.0060,    // New York City
        timezoneOffset = -4.0,            // EDT (UTC-4)
        includeAdvancedMetadata = true
    )

    // --- Fajr Metadata (TwilightMetadata) ---
    times.astronomicalMetadata?.fajr?.let { fajr ->
        println("Fajr Status: ${fajr.status}")             // SUCCESS, CONTINUOUS_TWILIGHT, POLAR_DAY, etc.
        println("Fajr Solar Declination: ${fajr.DEC_deg}°") // Sun's angle relative to celestial equator
        println("Fajr Equation of Time: ${fajr.EOT_min} min") // Difference between solar noon and clock noon
        println("Fajr Twilight Angle: ${fajr.angle_deg}°")    // The depression angle used (e.g. -18.0°)
        println("Solver Iterations: ${fajr.iterations}")        // Convergence speed (usually 3-6)
    }

    // --- Sunrise / Maghrib Metadata (SunriseSunsetMetadata) ---
    times.astronomicalMetadata?.sunrise?.let { sr ->
        println("\nSunrise Status: ${sr.status}")
        println("Solar Horizontal Parallax: ${sr.HP_arcmin} arcmin") // Tiny Earth-Sun distance correction
        println("Solar Semidiameter: ${sr.SD_arcmin} arcmin")       // Half the angular size of the sun disc
        println("Atmospheric Refraction: ${sr.refraction_deg}°")    // Bending of light near horizon
        println("Elevation Applied: ${sr.elevationMeters}m")        // Horizon dip correction
    }

    // --- Asr Metadata (AsrMetadata) ---
    times.astronomicalMetadata?.asr?.let { asr ->
        println("\nAsr Status: ${asr.status}")
        println("Sun Dec at Dhuhr: ${asr.DEC_of_Dhuhr_deg}°")
        println("Sun Dec at Asr: ${asr.DEC_of_Asr_deg}°")
        println("Asr Zenith Angle: ${asr.asrAngle_deg}°")
    }

    // --- Dhuhr Metadata (DhuhrMetadata) ---
    times.astronomicalMetadata?.dhuhr?.let { dhuhr ->
        println("\nDhuhr Status: ${dhuhr.status}")
        println("Equation of Time at Transit: ${dhuhr.EOT_min} min")
        println("Solver Iterations: ${dhuhr.iterations}")
    }
}
```

---

## Scenario 9: Using the `DateComponents` Overload

If your app has a date-picking UI and stores dates as an object rather than three separate integers, use the `DateComponents` convenience overload.

```kotlin
import com.tauqeet.library.Tauqeet
import com.tauqeet.library.DateComponents

fun prayerTimesWithDateComponents() {
    val tauqeet = Tauqeet()
    val today = DateComponents(year = 2026, month = 8, day = 1)

    val times = tauqeet.computePrayerTimes(
        date = today,
        lat = 24.8607, lng = 67.0011,
        timezoneOffset = 5.0
    )
    println("Fajr: ${times.fajr?.toTimeStringShort()}")
}
```

---

## Scenario 10: Formatting Times in Different Formats

The library provides multiple `Long` extension functions to format the raw millisecond values:

```kotlin
val fajrMs: Long? = times.fajr // e.g. 15780000L (= 04:23:00 UTC)

// "HH:mm:ss" — most common for prayer displays
println(fajrMs?.toTimeString())          // → "04:23:00"

// "HH:mm" — compact version, no seconds
println(fajrMs?.toTimeStringShort())     // → "04:23"

// "HH:mm:ss.SSS" — full precision with milliseconds
println(fajrMs?.toISOTimeStringWithMillis()) // → "04:23:00.000"

// Null-safe UI display pattern — never show a blank
val display = fajrMs?.toTimeStringShort() ?: "--:--"
println(display) // → "--:--" during Polar Night
```

---

## Defensive UI Pattern (Recommended)

This is the recommended pattern for building a complete, crash-proof prayer times screen:

```kotlin
fun renderPrayerTimesUI(lat: Double, lng: Double, tz: Double) {
    val tauqeet = Tauqeet(
        method = CalculationMethod.KARACHI,
        madhab = Madhab.HANAFI,
        highLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT
    )

    val times = tauqeet.computePrayerTimes(
        year = 2026, month = 8, day = 1,
        lat = lat, lng = lng, timezoneOffset = tz
    )

    val meta = times.metadata

    // 1. Check for extreme polar conditions first
    when {
        meta?.isPolarNight == true -> {
            showWarning("Polar Night: Sun does not rise. Showing estimated times.")
        }
        meta?.isPolarDay == true -> {
            showWarning("Polar Day: Sun does not set. Times estimated via ${meta.highLatitudeRule}.")
        }
    }

    // 2. Render all times safely — never force-unwrap (!!)
    renderRow("Fajr",    times.fajr?.toTimeStringShort() ?: "--:--")
    renderRow("Sunrise", times.sunrise?.toTimeStringShort() ?: "--:--")
    renderRow("Dhuhr",   times.dhuhr?.toTimeStringShort() ?: "--:--")
    renderRow("Asr",     times.asr?.toTimeStringShort() ?: "--:--")
    renderRow("Maghrib", times.maghrib?.toTimeStringShort() ?: "--:--")
    renderRow("Isha",    times.isha?.toTimeStringShort() ?: "--:--")
}

fun renderRow(name: String, time: String) = println("$name: $time")
fun showWarning(msg: String) = println("⚠️ $msg")
```
