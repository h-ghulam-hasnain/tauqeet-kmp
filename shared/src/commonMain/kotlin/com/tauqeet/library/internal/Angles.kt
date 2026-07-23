package com.tauqeet.library.internal

import kotlin.math.PI

fun degreesToRadians(degrees: Double): Double {
    return (degrees * PI) / 180.0
}

fun radiansToDegrees(radians: Double): Double {
    return (radians * 180.0) / PI
}

fun normalizeDegrees(value: Double): Double {
    val remainder = value % 360.0
    return if (remainder < 0.0) remainder + 360.0 else remainder
}

fun normalizeSignedAngle(value: Double): Double {
    var normalized = value % 360.0
    if (normalized > 180.0) normalized -= 360.0
    if (normalized <= -180.0) normalized += 360.0
    return normalized
}
