package com.tauqeet.library.internal

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Converts a decimal degree value into a human-readable DMS (Degrees, Minutes, Seconds) string.
 * Example: 23.44.toDMS() -> "23° 26' 24\""
 */
fun Double.toDMS(): String {
    val d = abs(this)
    val degrees = d.toInt()
    val minutesDouble = (d - degrees) * 60
    val minutes = minutesDouble.toInt()
    val seconds = ((minutesDouble - minutes) * 60).roundToInt()

    // Handle rounding overflow (e.g. 59.5 seconds rounds to 60)
    var finalSeconds = seconds
    var finalMinutes = minutes
    var finalDegrees = degrees

    if (finalSeconds == 60) {
        finalSeconds = 0
        finalMinutes += 1
    }
    if (finalMinutes == 60) {
        finalMinutes = 0
        finalDegrees += 1
    }

    val sign = if (this < 0) "-" else ""
    return "\$sign\${finalDegrees}° \${finalMinutes}' \${finalSeconds}\""
}
