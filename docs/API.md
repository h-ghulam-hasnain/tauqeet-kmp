# 📘 Tauqeet KMP — API Guide & Documentation

## 1. Introduction & Quick Overview

Welcome to `tauqeet-kmp`! This library provides mobile and desktop developers with a blazing-fast, scientifically accurate toolkit for computing **Islamic Prayer Times** and **Qibla Direction**. 

Under the hood, it utilizes the WGS-84 Vincenty Inverse equations to calculate the direction to the Kaaba with millimeter-level geodetic accuracy, while driving prayer times using robust VSOP87 solar mechanics. 

### Deep Dive Documentation
- [🕋 Qibla Calculation Deep Dive](QIBLA.md)
- [🕌 Prayer Times Calculation Engine](PRAYER_TIMES.md)
- [🏗️ Architecture & Design Philosophy](ARCHITECTURE.md)
- [📊 I/O Data Types & Error States](DATA_TYPES.md)
- [🚀 Complete API Examples](EXAMPLES.md)
- [🗺️ Calculation Methods Reference](CALCULATION_METHODS.md)
- [❓ Troubleshooting & FAQ](TROUBLESHOOTING.md)

---

## 2. Installation & Dependencies

To add `tauqeet-kmp` to your Kotlin Multiplatform, Android, or JVM project, simply include it in your `commonMain` dependencies block inside `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.tauqeet:tauqeet-kmp:0.2.0")
        }
    }
}
```

---

## 3. 🕋 Qibla Module Usage (How it Works)

Calculating the Qibla is incredibly simple. You just provide your `latitude` and `longitude`, and the library returns exactly where to face and how far away the Kaaba is.

### **The Standard Method (Recommended)**
Use `qiblaDirection()` to retrieve the bearing and distance bundled together in a neat data class.

```kotlin
import com.tauqeet.library.Tauqeet

val lat = 51.5072
val lng = -0.1276

val tauqeet = Tauqeet()
val qibla = tauqeet.qiblaDirection(lat, lng)

if (qibla != null) {
    println("Turn your compass to: ${qibla.bearing}°") 
    println("Distance to Mecca: ${qibla.distanceKm} km")
} else {
    println("You are standing exactly inside the Kaaba!")
}
```

### **The Data Structure**
When you call `qiblaDirection()`, it returns a `QiblaResult?`:
```kotlin
data class QiblaResult(
    val bearing: Double,      // Qibla direction angle from North [0.0° to 360.0°)
    val distanceKm: Double    // Direct surface distance to Kaaba in Kilometers
)
```

### **Legacy / Simple Bearing Method**
If you don't care about the distance and just want a single number, you can use the backward-compatible `qiblaBearing()` method:
```kotlin
val tauqeet = Tauqeet()
val bearing: Double? = tauqeet.qiblaBearing(31.4187, 73.0791)
```

### **Edge Cases Handling (Under the Hood)**
You don't need to write any error-handling loops; the library manages edge cases automatically:
- **Standing at the Kaaba**: The function returns `null`.
- **Standing at the Exact Antipode (Opposite Side of Earth)**: The primary algorithm gracefully falls back to a spherical mean-earth calculation so it always yields a valid bearing without crashing your app.

---

## 4. 🕌 Prayer Times Module Usage

The prayer time engine is robust yet simple to instantiate. 

### **Preferred Request Style (Unified API)**
The recommended public entry point is the unified `PrayerRequest` DSL, which keeps date, location, timezone, and calculation settings together in a single clean request object.

```kotlin
import com.tauqeet.library.DateComponents
import com.tauqeet.library.Tauqeet
import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.HighLatitudeRule
import com.tauqeet.library.prayers.prayerRequest

val tauqeet = Tauqeet(
    method = CalculationMethod.KARACHI,
    highLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT
)

val times = tauqeet.computePrayerTimes(
    prayerRequest {
        latitude = 24.8607
        longitude = 67.0011
        date = DateComponents(2026, 7, 23)
        timeZoneOffset = 5.0
        includeAdvancedMetadata = true
    }
)

println("Fajr: ${times.fajr?.toTimeString()}")
println("Dhuhr: ${times.dhuhr?.toTimeString()}")
println("Resolver: ${times.resolutionInfo?.solver}")
```

> The older flat overloads remain available for compatibility, but they are now deprecated in favor of the request DSL.

### **Resolution Metadata and Solver Routing**
The result now carries structured routing metadata so you can inspect how the engine resolved the day:

```kotlin
val solver = times.resolutionInfo?.solver
val flags = times.flags

println("Solver: $solver")
println("Polar day: ${times.isPolarDay}")
println("Polar night: ${times.isPolarNight}")
println("High-latitude fallback: ${times.isHighLatitudeFallback}")
```

### **The Metadata Flag (`includeAdvancedMetadata`)**
By default, the engine remains lightweight and returns the core UI result plus resolution metadata. If you need deeper astronomical details for debugging, dashboards, or high-latitude analysis, you can enable `includeAdvancedMetadata = true`:

```kotlin
val times = tauqeet.computePrayerTimes(
    prayerRequest {
        latitude = 24.8607
        longitude = 67.0011
        date = DateComponents(2026, 7, 23)
        timeZoneOffset = 5.0
        includeAdvancedMetadata = true
    }
)

val fajrDeclination = times.astronomicalMetadata?.fajr?.DEC_deg
```

---

## 5. 💡 Best Practices for App Developers

### **1. Building a Mobile Compass UI**
When building a Qibla compass on iOS or Android, use the `qibla.bearing` along with your device's magnetometer (hardware compass). 
- Simply subtract the True North heading of your phone from the `qibla.bearing`.
- Rotate your compass needle by that difference!
- *Pro-Tip*: Display the `qibla.distanceKm` below the compass to give users a premium, geographic feel.

### **2. Handling Nullability Cleanly**
Both the `PrayerTimesResult` variables (`fajr`, `sunrise`, etc.) and `qiblaDirection()` are cleanly nullable (`Type?`). 
**Never force-unwrap (`!!`) these variables.** Instead, embrace safe-calls:

```kotlin
// UI Display Example
val fajrText = times.fajr?.toTimeString() ?: "No Fajr Today (Polar Sun)"
```
This guarantees your UI will never crash, even if your user opens the app inside the Arctic Circle during the summer!
