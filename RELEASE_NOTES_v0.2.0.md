# Release v0.2.0 (Stability & API Refinement)

We are pleased to announce the `0.2.0` release of **Tauqeet Kotlin Multiplatform (KMP)**.

This update focuses on a cleaner public API, stronger mathematical validation, better solver routing transparency, and broader release-readiness for Maven Central publishing.

## 🚀 Key Features

* **High-Precision Prayer Times**: Mathematical parity with VSOP87 solar mechanics, ensuring exact hour-angle derivations rather than approximate averages.
* **Geodesic Qibla Engine**: Includes exact WGS-84 coordinate mapping for distance and precise bearings to Makkah, complete with robust antipodal (Spherical Law of Cosines) fallbacks.
* **True Cross-Platform Support**: Natively compiles to:
  * **Android & JVM** (via `io.github.h-ghulam-hasnain:tauqeet-kmp:0.2.0`)
  * **iOS** (Arm64, X64, and Simulator architectures)
  * **JavaScript / Node.js** (via npm package `tauqeet-js`)
* **Extreme Latitude Grace**: Clean handling of Midnight Sun / Polar Night conditions without invalid hardcoded UI fallbacks.
* **Dual-Tier Metadata**: Exposes both simple UI state flags (`isPolarDay`) and advanced internal astronomical metrics (e.g. `TwilightMetadata` and exact Solar Declination angles) for debugging or deep integrations.

## 📦 Installation

**Gradle (Kotlin Multiplatform, Android, JVM, iOS)**
```kotlin
implementation("io.github.h-ghulam-hasnain:tauqeet-kmp:0.2.0")
```

**npm (JavaScript, Node.js, Web)**
```bash
npm install tauqeet-js
```

## 🛠️ Usage Quick Start

```kotlin
import com.tauqeet.library.Tauqeet
import com.tauqeet.library.prayers.CalculationMethod

val tauqeet = Tauqeet(method = CalculationMethod.MAKKAH)

// Calculate for Mecca 
val times = tauqeet.computePrayerTimes(
    year = 2026, month = 7, day = 23, 
    lat = 21.4225, lng = 39.8262, 
    timezoneOffset = 3.0
)

// Geodesic Qibla Calculation
val qibla = tauqeet.qiblaDirection(51.5072, -0.1276)
```

For full documentation and examples, please see the [README](https://github.com/tauqeet/tauqeet-kmp#readme).
