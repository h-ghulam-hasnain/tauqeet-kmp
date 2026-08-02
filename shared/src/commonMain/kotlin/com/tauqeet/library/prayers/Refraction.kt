package com.tauqeet.library.prayers

import kotlin.math.PI
import kotlin.math.tan
import kotlin.math.sqrt
import kotlin.math.abs

/**
 * Calculates the astronomical atmospheric refraction correction using Bennett's Formula (1982).
 * Returns the refraction correction in degrees.
 */
fun getRefractionDegrees(
    apparentAltitudeDeg: Double,
    temperatureC: Double = 10.0,
    pressureMbar: Double = 1010.0
): Double {
    // Clamp the apparent altitude to avoid instability in Bennett's formula at extreme negatives
    val clampedAltitude = apparentAltitudeDeg.coerceIn(-2.0, 89.9)
    
    val interiorAngleDeg = clampedAltitude + 7.31 / (clampedAltitude + 4.4)
    val interiorAngleRad = interiorAngleDeg * (PI / 180.0)

    val tanVal = tan(interiorAngleRad)
    if (abs(tanVal) < 1e-6) return 0.0

    val baseRefractionArcminutes = 1.0 / tanVal

    val pressureFactor = pressureMbar / 1010.0
    val temperatureFactor = 283.15 / (temperatureC + 273.15)

    return (baseRefractionArcminutes * pressureFactor * temperatureFactor) / 60.0
}

/**
 * Computes the horizon dip angle in degrees based on height above sea level.
 */
fun computeDipAngle(elevationMeters: Double): Double {
    if (elevationMeters <= 0.0) return 0.0
    return 0.02933333 * sqrt(elevationMeters)
}
