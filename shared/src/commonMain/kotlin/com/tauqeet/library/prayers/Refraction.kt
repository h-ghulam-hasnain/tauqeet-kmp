package com.tauqeet.library.prayers

import kotlin.math.PI
import kotlin.math.tan
import kotlin.math.sqrt

/**
 * Calculates the astronomical atmospheric refraction correction using Bennett's Formula (1982).
 * Returns the refraction correction in degrees.
 */
fun getRefractionDegrees(
    apparentAltitudeDeg: Double,
    temperatureC: Double = 10.0,
    pressureMbar: Double = 1010.0
): Double {
    if (apparentAltitudeDeg < -1.0 || apparentAltitudeDeg > 89.9) {
        return 0.0
    }

    val interiorAngleDeg = apparentAltitudeDeg + 7.31 / (apparentAltitudeDeg + 4.4)
    val interiorAngleRad = interiorAngleDeg * (PI / 180.0)

    val baseRefractionArcminutes = 1.0 / tan(interiorAngleRad)

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
