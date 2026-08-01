# Tauqeet KMP

[![Maven Central](https://img.shields.io/maven-central/v/io.github.h-ghulam-hasnain/tauqeet-kmp.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.h-ghulam-hasnain/tauqeet-kmp)
[![npm version](https://img.shields.io/npm/v/tauqeet-js.svg)](https://www.npmjs.com/package/tauqeet-js)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![CI](https://github.com/tauqeet/tauqeet-kmp/actions/workflows/build.yml/badge.svg)](https://github.com/tauqeet/tauqeet-kmp/actions/workflows/build.yml)
A high-precision Islamic prayer times and Qibla calculation library for Kotlin Multiplatform (Android, iOS, JVM, JS).
Ported from the robust [tauqeet-js](https://github.com/tauqeet/tauqeet-js) library, maintaining 1:1 mathematical precision based on VSOP87 algorithms.

## Features
- **Highly Accurate**: Based on exact solar positions rather than rough approximations.
- **Cross-Platform**: Supports Android, iOS, Web (Node.js/Browser), and desktop JVM apps.
- **Customizable**: Multiple built-in Islamic methods and Madhab configurations.
- **Strict Mathematical Parity**: 100% numerical parity with `tauqeet-js`.
- **Geodesic Qibla Engine**: Computes exact WGS-84 distances and precise bearings, complete with mathematically robust antipodal fallbacks.

## High Latitudes & Extreme Scenarios

`tauqeet-kmp` is designed to be mathematically rigorous. Rather than relying on arbitrary approximations (such as faking Asr times with hardcoded fallbacks), the engine natively propagates clean `Long?` (nullable) values when an astronomical event mathematically does not occur (e.g., during Polar Night or the Midnight Sun).
- **Graceful Degradation**: If the sun never reaches the required twilight angle, `null` is returned instead of an invalid time.
- **UI Context**: The `metadata.isPolarDay` and `metadata.isPolarNight` boolean flags explicitly inform the UI layer when the engine has entered a non-convergent state, allowing developers to safely degrade the interface or warn the user.

## Dual-Tier Metadata System

The library exposes metadata in two tiers to support both simple UIs and deep astronomical debugging without sacrificing performance:

1. **Simple UI Metadata (`PrayerTimesMetadata`)**: 
   Attached by default to `PrayerTimesResult.metadata`. It features zero-overhead fields indicating the configuration parameters applied (`method`, `madhab`, `highLatitudeRule`) and extreme state flags (`isPolarDay`, `isPolarNight`).
   
2. **Advanced Astronomical Metadata (`AstronomicalMetadata`)**:
   An opt-in mapping of the exact internal solar mechanics generated during the iterative hour-angle calculations. When `includeAdvancedMetadata = true` is passed to the engine, it populates `astronomicalMetadata` with granular, per-event data classes (`TwilightMetadata`, `SunriseSunsetMetadata`, `DhahwaKubraMetadata`, `DhuhrMetadata`, `AsrMetadata`). 
   To maintain physical precision, fields use explicit unit suffixes:
   - `_deg` (Degrees)
   - `_min` (Minutes of time)
   - `_arcmin` (Arcminutes)
   - `elevationMeters` (Meters)

## Installation

### Kotlin Multiplatform / Android / JVM / iOS (Gradle)
Add the Maven Central repository and the dependency to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.h-ghulam-hasnain:tauqeet-kmp:0.2.0")
}
```

### JavaScript / Node.js / Web (npm)
Install the package via npm:

```bash
npm install tauqeet-js
```

### iOS (CocoaPods)
If using CocoaPods, include the shared framework in your Podfile (assuming integration via KMP plugin).

## Unified API – PrayerRequest DSL

For cleaner call sites and safer request construction, use the unified DSL request object:

```kotlin
import com.tauqeet.library.Tauqeet
import com.tauqeet.library.prayers.prayerRequest
import com.tauqeet.library.prayers.CalculationMethod

val tauqeet = Tauqeet(method = CalculationMethod.MWL)

val times = tauqeet.computePrayerTimes(
    prayerRequest {
        latitude = 51.5072
        longitude = -0.1276
        date = com.tauqeet.library.DateComponents(2026, 7, 23)
        timeZoneOffset = 1.0
        includeAdvancedMetadata = true
        calculation {
            method = CalculationMethod.MWL
            highLatitudeRule = com.tauqeet.library.prayers.HighLatitudeRule.MIDDLE_OF_NIGHT
        }
    }
)
```

The older flat overloads are still supported, but the request DSL is the preferred public entry point.

## Usage Example

```kotlin
import com.tauqeet.library.Tauqeet
import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.Madhab
import com.tauqeet.library.toTimeString
import com.tauqeet.library.prayers.toISOTimes

fun main() {
    val tauqeet = Tauqeet(
        method = CalculationMethod.MWL,
        madhab = Madhab.SHAFI
    )

    // Compute for London, UK (UTC+1 in summer)
    // We explicitly enable `includeAdvancedMetadata` for astronomical logs
    val times = tauqeet.computePrayerTimes(
        year = 2026, 
        month = 7, 
        day = 23, 
        lat = 51.5072, 
        lng = -0.1276, 
        timezoneOffset = 1.0,
        includeAdvancedMetadata = true
    )

    println("Fajr: ${times.fajr?.toTimeString() ?: "N/A"}")
    println("Sunrise: ${times.sunrise?.toTimeString() ?: "N/A"}")
    println("Dhahwa Kubra: ${times.dhahwaKubra?.toTimeString() ?: "N/A"}")
    println("Dhuhr: ${times.dhuhr?.toTimeString() ?: "N/A"}")
    println("Asr: ${times.asr?.toTimeString() ?: "N/A"}")
    println("Maghrib: ${times.maghrib?.toTimeString() ?: "N/A"}")
    println("Isha: ${times.isha?.toTimeString() ?: "N/A"}")

    // The raw times are returned as nullable Long (milliseconds since midnight)
    println("Raw Sunrise ms: ${times.sunrise}")

    // Simple UI Metadata details
    println("Method Used: ${times.metadata?.method}")
    println("High Lat Rule: ${times.metadata?.highLatitudeRule}")
    println("Is Polar Day: ${times.metadata?.isPolarDay}")

    // Advanced Astronomical Metadata (safely access event-specific mechanics)
    times.astronomicalMetadata?.fajr?.let { fajrMeta ->
        println("\nFajr Calculation Status: ${fajrMeta.status}")
        println("Solar Declination: ${fajrMeta.DEC_deg}°")
        println("Equation of Time: ${fajrMeta.EOT_min} mins")
    }

    // Qibla Direction (Bearing & Distance)
    val qibla = tauqeet.qiblaDirection(51.5072, -0.1276)
    println("\nQibla Bearing: ${qibla?.bearing} degrees")
    println("Distance to Mecca: ${qibla?.distanceKm} km")
}
```

## Supported Calculation Methods
- `MWL` (Muslim World League)
- `ISNA` (Islamic Society of North America)
- `EGYPT` (Egyptian General Authority of Survey)
- `MAKKAH` (Umm al-Qura University)
- `KARACHI` (University of Islamic Sciences, Karachi)
- `TEHRAN` (Institute of Geophysics, University of Tehran)
- `JAFARI` (Shia Ithna Ashari)
- `ALGERIA` (18° Fajr, 12° Isha)
- And more (`GULF`, `KUWAIT`, `QATAR`, `SINGAPORE`, `FRANCE`, `TURKEY`, `RUSSIA`)
- **`CUSTOM`**: You can pass your own `CalculationMethodParameters` to `computePrayerTimes` or `Tauqeet` instance for custom angles.

## Supported Madhabs
The library supports all classical schools of thought. `SHAFI` mathematically encompasses `MALIKI`, `HANBALI`, and `JAAFARI` (shadow factor 1), while `HANAFI` uses shadow factor 2. For convenience, aliases (`MALIKI`, `HANBALI`, `JAAFARI`) are included in the enum.

## Testing & Quality Assurance

`tauqeet-kmp` maintains **100% mathematical parity** with the original TypeScript `tauqeet-js` suite.

The test suite explicitly uses property-based testing and strict invariant checks across diverse global conditions, including:
- **Chronological Stability**: Continuous invariant loops asserting `Fajr < Sunrise < Dhuhr < Asr < Maghrib < Isha` even across chaotic midnight boundaries.
- **Polar Resilience**: Extensive high-latitude testing using multiple strategies (e.g. `MIDDLE_OF_NIGHT`, `SEVENTH_OF_NIGHT`) above the Arctic Circle (e.g. Tromsø) during the midnight sun.
- **Precision Validation**: Tested continuously against known values from classical texts like Meeus (VSOP87 periodic accuracy, strictly positive $\Delta T$ values bounds, and precise Qibla bearing normalization).

All tests run continuously against both native JVM configurations and Javascript environments on CI to assure behavioral unity across target ecosystems.
