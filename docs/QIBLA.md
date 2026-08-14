# 🕋 Qibla Calculation Deep Dive

The Qibla module in `tauqeet-kmp` is designed to provide highly accurate geodetic calculations, significantly outperforming standard spherical formulas used in many legacy libraries.

## How It Works

By default, the library employs the **WGS-84 Vincenty Inverse Formula**. This is an iterative algorithm that models the Earth as an oblate spheroid, which is far more accurate than assuming a perfect sphere. 

### Why Vincenty?
Standard spherical models (like Haversine or Spherical Law of Cosines) can introduce errors of several kilometers and fractions of degrees due to the Earth's equatorial bulge. Vincenty's formula provides millimeter-level precision for distance and highly accurate bearings, ensuring that the direction pointing to the Kaaba is as exact as mathematically possible.

### Fallback Mechanism
Vincenty's formula is famously susceptible to non-convergence when two points are nearly antipodal (on exactly opposite sides of the Earth). 

If you are standing near the antipodal point of the Kaaba, the iterative Vincenty algorithm may fail to converge. `tauqeet-kmp` seamlessly handles this by falling back to the **Spherical Law of Cosines** for bearing and the **Haversine formula** for distance. This ensures that the application never crashes and always returns a reliable fallback value.

## API Reference

### Primary Entry Point (Recommended)

**`tauqeetQibla(lat: Double, lng: Double): QiblaResult?`**

A top-level free function that provides the most concise API. Returns both bearing and distance to Mecca. This is the recommended choice for most use cases.

```kotlin
import com.tauqeet.library.qibla.tauqeetQibla

val qibla = tauqeetQibla(lat = 51.5072, lng = -0.1276)
when (qibla) {
    null -> println("You are at the Kaaba")
    else -> println("Bearing: ${qibla.bearing}°, Distance: ${qibla.distanceKm} km")
}
```

### Instance Methods (With Environmental Parameters)

If you need to influence the calculation with environmental parameters (elevation, temperature, pressure), use the instance methods on `Tauqeet`:

**`Tauqeet.qiblaDirection(lat: Double, lng: Double): QiblaResult?`**

Returns both bearing and distance, with the environmental parameters configured on the `Tauqeet` instance applied.

```kotlin
val tauqeet = Tauqeet(
    elevationMeters = 1000.0,  // mountain location
    temperatureC = 5.0,
    pressureMbar = 950.0
)
val qibla = tauqeet.qiblaDirection(lat = 51.5072, lng = -0.1276)
```

**`Tauqeet.qiblaBearing(lat: Double, lng: Double): Double?`**

A backward-compatible convenience method that returns just the bearing angle (ignoring distance). Internally delegates to `qiblaDirection()`.

## Best Practices
- Remember that the bearing returned is relative to **True North**, not Magnetic North. If you are building a compass app, you must account for magnetic declination based on the user's location to point the hardware compass correctly.
- Always handle the nullable return type cleanly. Do not use force unwrap (`!!`).
