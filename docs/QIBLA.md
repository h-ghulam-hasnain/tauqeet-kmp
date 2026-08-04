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

### `Tauqeet.qiblaDirection(lat: Double, lng: Double): QiblaResult?`
This is the preferred public API. It returns both the bearing (in degrees from true north) and the distance (in kilometers). If the provided coordinates are extremely close to the Kaaba (distance < 1 meter), the function returns `null` because all directions face the Kaaba.

### `Tauqeet.qiblaBearing(lat: Double, lng: Double): Double?`
A backward-compatible convenience method that returns just the bearing. Internally, it delegates to the same underlying geodetic calculation and discards the distance component.

### `tauqeetQibla(lat: Double, lng: Double): QiblaResult?`
A lower-level free function that exposes the same calculation logic for advanced callers and internal use.

## Best Practices
- Remember that the bearing returned is relative to **True North**, not Magnetic North. If you are building a compass app, you must account for magnetic declination based on the user's location to point the hardware compass correctly.
- Always handle the nullable return type cleanly. Do not use force unwrap (`!!`).
