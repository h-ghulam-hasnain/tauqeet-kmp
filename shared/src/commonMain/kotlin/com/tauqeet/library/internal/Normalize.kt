package com.tauqeet.library.internal

import kotlin.math.max
import kotlin.math.min

/**
 * Wraps a longitude value into the range [-180, 180).
 */
fun normalizeLongitude(lon: Double): Double {
    var result = lon % 360.0
    if (result > 180.0) result -= 360.0
    if (result <= -180.0) result += 360.0
    return result
}

/**
 * Clamps a latitude value to [-90, 90].
 */
fun normalizeLatitude(lat: Double): Double {
    return min(90.0, max(-90.0, lat))
}

/**
 * Wraps an angle (in degrees) into [0, 360).
 */
fun normalizeAngle(degrees: Double): Double {
    return ((degrees % 360.0) + 360.0) % 360.0
}
