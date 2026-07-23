package com.tauqeet.library.qibla

import com.tauqeet.library.internal.calculateVincentyInverseBearing

/**
 * Calculates the exact Qibla bearing from the given location to the Kaaba in Mecca.
 *
 * @param lat Observer's latitude in decimal degrees.
 * @param lng Observer's longitude in decimal degrees.
 * @return The initial bearing to Mecca in degrees from true north.
 */
fun bearingToMecca(lat: Double, lng: Double): Double? {
    // Mecca coordinates: Latitude 21.422487, Longitude 39.826206
    return calculateVincentyInverseBearing(lat, lng, 21.422487, 39.826206)
}
