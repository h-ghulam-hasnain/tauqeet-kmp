# Tauqeet KMP

[![CI](https://github.com/tauqeet/tauqeet-kmp/actions/workflows/build.yml/badge.svg)](https://github.com/tauqeet/tauqeet-kmp/actions/workflows/build.yml)

A high-precision Islamic prayer times and Qibla calculation library for Kotlin Multiplatform (Android, iOS, JVM, JS).
Ported from the robust [tauqeet-js](https://github.com/tauqeet/tauqeet-js) library, maintaining 1:1 mathematical precision based on VSOP87 algorithms.

## Features
- **Highly Accurate**: Based on exact solar positions rather than rough approximations.
- **Cross-Platform**: Supports Android, iOS, Web (Node.js/Browser), and desktop JVM apps.
- **Customizable**: Multiple built-in Islamic methods and Madhab configurations.
- **Extreme Latitudes**: Robust fallback strategies (e.g. Tromsø in midnight sun).

## Installation

### Kotlin Multiplatform / Android / JVM (Gradle)
Add the Maven Central repository and the dependency to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.tauqeet:tauqeet-kmp:0.1.0")
}
```

### JS / Node.js (npm)
Install the package via npm:

```bash
npm install tauqeet-kmp
```

### iOS (CocoaPods)
If using CocoaPods, include the shared framework in your Podfile (assuming integration via KMP plugin).

## Usage Example

```kotlin
import com.tauqeet.library.Tauqeet
import com.tauqeet.library.qiblaBearing
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
    val times = tauqeet.computePrayerTimes(
        year = 2024, 
        month = 6, 
        day = 21, 
        lat = 51.5072, 
        lng = -0.1276, 
        timezoneOffset = 1.0
    )

    println("Fajr: ${times.fajr.toTimeString()}")
    println("Sunrise: ${times.sunrise.toTimeString()}")
    println("Dhahwa Kubra: ${times.dhahwaKubra.toTimeString()}")
    println("Dhuhr: ${times.dhuhr.toTimeString()}")
    println("Asr: ${times.asr.toTimeString()}")
    println("Maghrib: ${times.maghrib.toTimeString()}")
    println("Isha: ${times.isha.toTimeString()}")

    // Alternatively, you can format the times as ISO 8601 strings (HH:mm:ss) to match the JS library output
    val isoTimes = times.toISOTimes()
    println("Fajr (ISO): ${isoTimes.fajr}")
    
    // The raw times are returned as Long (milliseconds since midnight) for custom formatting
    println("Raw Sunrise ms: ${times.sunrise}")

    // Metadata details
    println("Method Used: ${times.metadata?.method}")
    println("High Lat Rule: ${times.metadata?.highLatitudeRule}")

    // Qibla Direction
    val qibla = qiblaBearing(51.5072, -0.1276)
    println("Qibla Bearing: $qibla degrees")
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
- And more (Gulf, Kuwait, Qatar, Singapore, France, Turkey, Russia)

## Testing & Quality Assurance

`tauqeet-kmp` maintains **100% mathematical parity** with the original TypeScript `tauqeet-js` suite.

The test suite explicitly uses property-based testing and strict invariant checks across diverse global conditions, including:
- **Chronological Stability**: Continuous invariant loops asserting `Fajr < Sunrise < Dhuhr < Asr < Maghrib < Isha` even across chaotic midnight boundaries.
- **Polar Resilience**: Extensive high-latitude testing using multiple strategies (e.g. `MIDDLE_OF_NIGHT`, `SEVENTH_OF_NIGHT`) above the Arctic Circle (e.g. Tromsø) during the midnight sun.
- **Precision Validation**: Tested continuously against known values from classical texts like Meeus (VSOP87 periodic accuracy, strictly positive $\Delta T$ values bounds, and precise Qibla bearing normalization).

All tests run continuously against both native JVM configurations and Javascript environments on CI to assure behavioral unity across target ecosystems.
