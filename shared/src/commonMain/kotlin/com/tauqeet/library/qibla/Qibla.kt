package com.tauqeet.library.qibla

import com.tauqeet.library.internal.calculateVincentyInverse
import com.tauqeet.library.internal.sphericalLawOfCosinesBearing
import com.tauqeet.library.internal.haversineDistance

data class QiblaResult(
    val bearing: Double,
    val distanceKm: Double
)

/**
 * Calculates the exact Qibla bearing from the given location to the Kaaba in Mecca.
 *
 * @param lat Observer's latitude in decimal degrees.
 * @param lng Observer's longitude in decimal degrees.
 * @return A QiblaResult containing the initial bearing to Mecca in degrees from true north, and the distance in km.
 */
fun tauqeetQibla(lat: Double, lng: Double): QiblaResult? {
    val meccaLat = 21.422487
    val meccaLng = 39.826206

    // Avoid co-incident issues
    val distHaversine = haversineDistance(lat, lng, meccaLat, meccaLng)
    if (distHaversine < 0.001) {
        return null
    }

    val vincenty = calculateVincentyInverse(lat, lng, meccaLat, meccaLng)
    
    return if (vincenty != null) {
        QiblaResult(vincenty.bearing, vincenty.distanceKm)
    } else {
        val bearing = sphericalLawOfCosinesBearing(lat, lng, meccaLat, meccaLng)
        QiblaResult(bearing, distHaversine)
    }
}

/**
 * Backward compatible top-level function.
 */
fun bearingToMecca(lat: Double, lng: Double): Double? {
    return tauqeetQibla(lat, lng)?.bearing
}
