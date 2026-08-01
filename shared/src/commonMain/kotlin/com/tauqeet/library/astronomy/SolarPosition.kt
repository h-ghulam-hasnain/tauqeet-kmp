package com.tauqeet.library.astronomy

import com.tauqeet.library.internal.asind
import com.tauqeet.library.internal.atand2
import com.tauqeet.library.internal.cosd
import com.tauqeet.library.internal.normalizeDegrees
import com.tauqeet.library.internal.radiansToDegrees
import com.tauqeet.library.internal.sind
import com.tauqeet.library.internal.tand
import com.tauqeet.library.time.TimeArgument
import com.tauqeet.library.time.timeArguments

class SolarPositionResult(
    val declination: Double,
    val equationOfTime: Double,
    val semidiameter: Double,
    val horizontalParallax: Double,
    val distanceAU: Double
)

/**
 * Internal solar ephemeris engine.
 * Computes strictly the high-precision DEC, EoT, SD, and HP parameters.
 */
class SolarEphemeris(
    val j: Double,
    val ut: Double,
    val deltaT: Double
) {
    val timeArgs: TimeArgument by lazy(LazyThreadSafetyMode.NONE) { timeArguments(j, ut, deltaT) }
    val earthState: EarthHeliocentricState by lazy(LazyThreadSafetyMode.NONE) { computeEarthHeliocentricState(timeArgs.tau) }
    val nutation: NutationResult by lazy(LazyThreadSafetyMode.NONE) { computeNutation(timeArgs.jd, ut, deltaT) }
    val aberration: Double by lazy(LazyThreadSafetyMode.NONE) { computeSolarAberration(earthState.radius) }
    
    val LSun: Double by lazy(LazyThreadSafetyMode.NONE) {
        val ldd = radiansToDegrees(earthState.longitude)
        normalizeDegrees(ldd + 180.0)
    }

    val BetaSun: Double by lazy(LazyThreadSafetyMode.NONE) {
        -radiansToDegrees(earthState.latitude)
    }

    val LPrime: Double by lazy(LazyThreadSafetyMode.NONE) {
        normalizeDegrees(LSun - timeArgs.te * (1.397 + 0.00031 * timeArgs.te))
    }

    val DeltaB: Double by lazy(LazyThreadSafetyMode.NONE) {
        (0.03916 * (cosd(LPrime) - sind(LPrime))) / 3600.0
    }

    val BCorr: Double by lazy(LazyThreadSafetyMode.NONE) {
        BetaSun + DeltaB
    }

    val DeltaL: Double by lazy(LazyThreadSafetyMode.NONE) {
        (-0.09033 + 0.03916 * (cosd(LPrime) + sind(LPrime)) * tand(BCorr)) / 3600.0
    }

    val LCorr: Double by lazy(LazyThreadSafetyMode.NONE) {
        LSun + DeltaL
    }

    val apparentLongitude: Double by lazy(LazyThreadSafetyMode.NONE) {
        LCorr + nutation.deltaPsi + aberration
    }

    val declination: Double by lazy(LazyThreadSafetyMode.NONE) {
        asind(
            sind(BCorr) * cosd(nutation.eps) +
                    cosd(BCorr) * sind(nutation.eps) * sind(apparentLongitude)
        )
    }

    val equationOfTime: Double by lazy(LazyThreadSafetyMode.NONE) {
        val t = timeArgs.t
        val L0 = ((280.46646 + 36000.76983 * t) % 360.0 + 360.0) % 360.0

        val rightAscension = normalizeDegrees(
            atand2(
                sind(apparentLongitude) * cosd(nutation.eps) -
                        tand(BCorr) * sind(nutation.eps),
                cosd(apparentLongitude)
            )
        )

        var eotDeg = L0 - rightAscension
        if (eotDeg > 180.0) eotDeg -= 360.0
        if (eotDeg < -180.0) eotDeg += 360.0

        eotDeg * 4.0
    }

    val semidiameter: Double by lazy(LazyThreadSafetyMode.NONE) {
        SOLAR_SEMIDIAMETER_SECONDS / earthState.radius / 60.0
    }

    val horizontalParallax: Double by lazy(LazyThreadSafetyMode.NONE) {
        8.794 / earthState.radius / 60.0
    }
}

fun computeSolarPosition(j: Double, ut: Double, deltaT: Double): SolarPositionResult {
    val engine = SolarEphemeris(j, ut, deltaT)
    return SolarPositionResult(
        declination = engine.declination,
        equationOfTime = engine.equationOfTime,
        semidiameter = engine.semidiameter,
        horizontalParallax = engine.horizontalParallax,
        distanceAU = engine.earthState.radius
    )
}
