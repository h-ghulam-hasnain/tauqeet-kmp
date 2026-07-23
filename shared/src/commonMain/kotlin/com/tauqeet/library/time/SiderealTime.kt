package com.tauqeet.library.time

import com.tauqeet.library.internal.cosd
import com.tauqeet.library.internal.normalizeDegrees

class SiderealTimeResult(
    val gmst: Double,
    val gast: Double
)

fun computeSiderealTime(
    jd: Double,
    t: Double,
    deltaPsi: Double,
    eps: Double
): SiderealTimeResult {
    val gmst = 280.46061837 + 360.98564736629 * (jd - 2451545.0) + t * t * (0.000387933 - t / 38710000.0)
    val gast = gmst + deltaPsi * cosd(eps)
    return SiderealTimeResult(
        gmst = normalizeDegrees(gmst),
        gast = normalizeDegrees(gast)
    )
}
