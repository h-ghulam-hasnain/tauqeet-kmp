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
    private var _timeArguments: TimeArgument? = null
    private var _earthState: EarthHeliocentricState? = null
    private var _nutation: NutationResult? = null
    private var _aberration: Double = Double.NaN
    private var _LSun: Double = Double.NaN
    private var _BetaSun: Double = Double.NaN
    private var _LPrime: Double = Double.NaN
    private var _DeltaL: Double = Double.NaN
    private var _DeltaB: Double = Double.NaN
    private var _LCorr: Double = Double.NaN
    private var _BCorr: Double = Double.NaN
    private var _apparentLongitude: Double = Double.NaN
    private var _Dec: Double = Double.NaN
    private var _equationOfTime: Double = Double.NaN
    private var _semidiameter: Double = Double.NaN
    private var _horizontalParallax: Double = Double.NaN

    val timeArgs: TimeArgument
        get() {
            if (_timeArguments == null) {
                _timeArguments = timeArguments(j, ut, deltaT)
            }
            return _timeArguments!!
        }

    val earthState: EarthHeliocentricState
        get() {
            if (_earthState == null) {
                _earthState = computeEarthHeliocentricState(timeArgs.tau)
            }
            return _earthState!!
        }

    val nutation: NutationResult
        get() {
            if (_nutation == null) {
                _nutation = computeNutation(timeArgs.jd, ut, deltaT)
            }
            return _nutation!!
        }

    val aberration: Double
        get() {
            if (_aberration.isNaN()) {
                _aberration = computeSolarAberration(earthState.radius)
            }
            return _aberration
        }

    val LSun: Double
        get() {
            if (_LSun.isNaN()) {
                val ldd = radiansToDegrees(earthState.longitude)
                _LSun = normalizeDegrees(ldd + 180.0)
            }
            return _LSun
        }

    val BetaSun: Double
        get() {
            if (_BetaSun.isNaN()) {
                _BetaSun = -radiansToDegrees(earthState.latitude)
            }
            return _BetaSun
        }

    val LPrime: Double
        get() {
            if (_LPrime.isNaN()) {
                _LPrime = normalizeDegrees(LSun - timeArgs.te * (1.397 + 0.00031 * timeArgs.te))
            }
            return _LPrime
        }

    val DeltaL: Double
        get() {
            if (_DeltaL.isNaN()) {
                _DeltaL = (-0.09033 + 0.03916 * (cosd(LPrime) + sind(LPrime)) * tand(BCorr)) / 3600.0
            }
            return _DeltaL
        }

    val DeltaB: Double
        get() {
            if (_DeltaB.isNaN()) {
                _DeltaB = (0.03916 * (cosd(LPrime) - sind(LPrime))) / 3600.0
            }
            return _DeltaB
        }

    val LCorr: Double
        get() {
            if (_LCorr.isNaN()) {
                _LCorr = LSun + DeltaL
            }
            return _LCorr
        }

    val BCorr: Double
        get() {
            if (_BCorr.isNaN()) {
                _BCorr = BetaSun + DeltaB
            }
            return _BCorr
        }

    val apparentLongitude: Double
        get() {
            if (_apparentLongitude.isNaN()) {
                _apparentLongitude = LCorr + nutation.deltaPsi + aberration
            }
            return _apparentLongitude
        }

    val declination: Double
        get() {
            if (_Dec.isNaN()) {
                _Dec = asind(
                    sind(BCorr) * cosd(nutation.eps) +
                            cosd(BCorr) * sind(nutation.eps) * sind(apparentLongitude)
                )
            }
            return _Dec
        }

    val equationOfTime: Double
        get() {
            if (_equationOfTime.isNaN()) {
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

                _equationOfTime = eotDeg * 4.0
            }
            return _equationOfTime
        }

    val semidiameter: Double
        get() {
            if (_semidiameter.isNaN()) {
                _semidiameter = SOLAR_SEMIDIAMETER_SECONDS / earthState.radius / 60.0
            }
            return _semidiameter
        }

    val horizontalParallax: Double
        get() {
            if (_horizontalParallax.isNaN()) {
                _horizontalParallax = 8.794 / earthState.radius / 60.0
            }
            return _horizontalParallax
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
