package com.tauqeet.library.internal

import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Great-circle distance between two geographic points using the Haversine formula.
 * @returns Distance in kilometres.
 */
fun haversineDistance(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double,
    radiusKm: Double = 6371.0
): Double {
    val phi1 = degreesToRadians(lat1)
    val phi2 = degreesToRadians(lat2)
    val deltaPhi = degreesToRadians(lat2 - lat1)
    val deltaLambda = degreesToRadians(lon2 - lon1)

    val a = sin(deltaPhi / 2.0).pow(2) + cos(phi1) * cos(phi2) * sin(deltaLambda / 2.0).pow(2)
    val clampedA = a.coerceIn(0.0, 1.0)

    return radiusKm * 2.0 * atan2(sqrt(clampedA), sqrt(1.0 - clampedA))
}

/**
 * Initial bearing from point 1 to point 2 using the Spherical Law of Cosines.
 * @returns Bearing in degrees, 0..360 (clockwise from true north).
 */
fun sphericalLawOfCosinesBearing(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val phi1 = degreesToRadians(lat1)
    val phi2 = degreesToRadians(lat2)
    val deltaLambda = degreesToRadians(lon2 - lon1)

    val y = sin(deltaLambda) * cos(phi2)
    val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)

    return ((radiansToDegrees(atan2(y, x)) % 360.0) + 360.0) % 360.0
}

/**
 * Rhumb-line (loxodromic) bearing from point 1 to point 2.
 *
 * @remarks
 * deltaLambda is normalized to [-PI, +PI] to correctly handle anti-meridian crossings
 * (e.g., from Hawaii or the Western Pacific towards Makkah). Without this
 * normalization the bearing wraps the "long way" around the globe.
 *
 * @returns Bearing in degrees, 0..360.
 */
fun rhumbLineBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val phi1 = degreesToRadians(lat1)
    val phi2 = degreesToRadians(lat2)

    val deltaPhi = ln(tan(PI / 4.0 + phi2 / 2.0) / tan(PI / 4.0 + phi1 / 2.0))

    // Normalize deltaLambda to [-PI, +PI] so anti-meridian crossings take the shorter arc.
    var deltaLambda = degreesToRadians(lon2 - lon1)
    if (abs(deltaLambda) > PI) {
        deltaLambda = if (deltaLambda > 0) -(2.0 * PI - deltaLambda) else (2.0 * PI + deltaLambda)
    }

    return ((radiansToDegrees(atan2(deltaLambda, deltaPhi)) % 360.0) + 360.0) % 360.0
}

data class VincentyResult(val bearing: Double, val distanceKm: Double)

/**
 * Vincenty Inverse formula — initial forward azimuth (bearing) and distance on the WGS-84
 * ellipsoid from point 1 to point 2.
 *
 * @remarks
 * Vincenty's iterative method converges to sub-millimetre accuracy for
 * virtually all coordinate pairs on Earth. The single known failure mode is the
 * exact antipodal case, where the algorithm oscillates and never converges.
 * This implementation detects non-convergence (> 100 iterations or lambda > PI)
 * and returns `null` so callers can apply a safe fallback without any exception
 * being thrown or the event-loop hanging.
 *
 * @param lat1 - Observer latitude in decimal degrees.
 * @param lon1 - Observer longitude in decimal degrees.
 * @param lat2 - Target latitude in decimal degrees.
 * @param lon2 - Target longitude in decimal degrees.
 * @returns A VincentyResult containing bearing and distance, or `null` if the
 *   algorithm fails to converge (antipodal or co-incident points).
 */
fun calculateVincentyInverse(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): VincentyResult? {
    val a = 6378137.0
    val b = 6356752.314245
    val f = 1.0 / 298.257_223_563

    // Reduced (parametric) latitudes on the auxiliary sphere
    val u1 = atan((1.0 - f) * tan(degreesToRadians(lat1)))
    val u2 = atan((1.0 - f) * tan(degreesToRadians(lat2)))

    val sinU1 = sin(u1)
    val cosU1 = cos(u1)
    val sinU2 = sin(u2)
    val cosU2 = cos(u2)

    // Difference of longitudes on the auxiliary sphere; starts at geodetic deltaLambda
    val L = degreesToRadians(lon2 - lon1)
    var lambda = L

    var sinLambda = 0.0
    var cosLambda = 0.0
    var sinSigma = 0.0
    var cosSigma = 0.0
    var sigma = 0.0
    var sinAlpha = 0.0
    var cosSqAlpha = 0.0
    var cos2SigmaM = 0.0

    val maxIter = 100
    val tolerance = 1e-12

    var iter = 0
    var lambdaPrev = 0.0

    do {
        lambdaPrev = lambda
        sinLambda = sin(lambda)
        cosLambda = cos(lambda)

        val sinSigmaTerm1 = cosU2 * sinLambda
        val sinSigmaTerm2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda

        sinSigma = sqrt(sinSigmaTerm1 * sinSigmaTerm1 + sinSigmaTerm2 * sinSigmaTerm2)

        // Co-incident points — every direction is valid, bearing undefined.
        if (sinSigma == 0.0) return null

        cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda
        sigma = atan2(sinSigma, cosSigma)
        sinAlpha = (cosU1 * cosU2 * sinLambda) / sinSigma
        cosSqAlpha = 1.0 - sinAlpha * sinAlpha

        // Equatorial line: cosSqAlpha = 0 -> cos2SigmaM defined as 0
        cos2SigmaM = if (cosSqAlpha == 0.0) {
            0.0
        } else {
            cosSigma - (2.0 * sinU1 * sinU2) / cosSqAlpha
        }

        val C = (f / 16.0) * cosSqAlpha * (4.0 + f * (4.0 - 3.0 * cosSqAlpha))

        lambda = L + (1.0 - C) * f * sinAlpha * (
            sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM))
        )

        // Antipodal guard: lambda past ±PI means no geodesic can be determined.
        if (abs(lambda) > PI) return null

    } while (abs(lambda - lambdaPrev) > tolerance && ++iter < maxIter)

    // Maximum iterations exceeded — antipodal / near-antipodal non-convergence.
    if (iter >= maxIter) return null

    // -- Final calculation --
    val uSq = cosSqAlpha * (a * a - b * b) / (b * b)
    val A = 1.0 + uSq / 16384.0 * (4096.0 + uSq * (-768.0 + uSq * (320.0 - 175.0 * uSq)))
    val B = uSq / 1024.0 * (256.0 + uSq * (-128.0 + uSq * (74.0 - 47.0 * uSq)))
    val deltaSigma = B * sinSigma * (cos2SigmaM + B / 4.0 * (cosSigma * (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM) - B / 6.0 * cos2SigmaM * (-3.0 + 4.0 * sinSigma * sinSigma) * (-3.0 + 4.0 * cos2SigmaM * cos2SigmaM)))
    
    val s = b * A * (sigma - deltaSigma)
    val distanceKm = s / 1000.0

    val sinLambdaFinal = sin(lambda)
    val cosLambdaFinal = cos(lambda)

    val fwdAzimuthRad = atan2(
        cosU2 * sinLambdaFinal,
        cosU1 * sinU2 - sinU1 * cosU2 * cosLambdaFinal
    )

    val bearing = ((radiansToDegrees(fwdAzimuthRad) % 360.0) + 360.0) % 360.0

    return VincentyResult(bearing, distanceKm)
}
