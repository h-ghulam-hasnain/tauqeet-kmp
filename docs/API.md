# 📘 Tauqeet KMP — API Guide & Documentation

## 1. Introduction & Quick Overview

Welcome to `tauqeet-kmp`! This library provides mobile and desktop developers with a blazing-fast, scientifically accurate toolkit for computing **Islamic Prayer Times** and **Qibla Direction**. 

Under the hood, it utilizes the WGS-84 Vincenty Inverse equations to calculate the direction to the Kaaba with millimeter-level geodetic accuracy, while driving prayer times using robust VSOP87 solar mechanics. 

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
            implementation("com.tauqeet:tauqeet-kmp:0.1.0")
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

### **Basic Calculation**
Here is a quick snippet to calculate today's prayer times for any city:

```kotlin
import com.tauqeet.library.Tauqeet
import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.Madhab

// 1. Configure your settings
val tauqeet = Tauqeet(
    method = CalculationMethod.KARACHI,
    madhab = Madhab.HANAFI
)

// 2. Compute the times
val times = tauqeet.computePrayerTimes(
    year = 2026, month = 7, day = 23, 
    lat = 24.8607, lng = 67.0011, 
    timezoneOffset = 5.0 // UTC+5 for Pakistan
)

// 3. Print the results!
println("Fajr: ${times.fajr?.toTimeString()}")
println("Dhuhr: ${times.dhuhr?.toTimeString()}")
```

### **The Metadata Flag (`includeAdvancedMetadata`)**
By default, the engine runs completely zero-allocation, returning only standard UI flags (`isPolarDay`, `method`). However, if you are building an advanced dashboard or debugging high-latitude anomalies (like Midnight Sun in Norway), you can opt-in to scientific metadata:

```kotlin
val times = tauqeet.computePrayerTimes(..., includeAdvancedMetadata = true)

// Access granular scientific variables directly:
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
