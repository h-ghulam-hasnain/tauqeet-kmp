package com.tauqeet.library.prayers

import com.tauqeet.library.astronomy.SolarPositionResult
import com.tauqeet.library.astronomy.computeSolarPosition
import com.tauqeet.library.internal.acosd
import com.tauqeet.library.internal.atand
import com.tauqeet.library.internal.cosd
import com.tauqeet.library.internal.sind
import com.tauqeet.library.internal.tand
import kotlin.math.abs

/**
 * Calculates the hour angle (in degrees) for a target zenith.
 */
internal fun solveHourAngle(targetZenith: Double, lat: Double, dec: Double): Double? {
    val cosH = (cosd(targetZenith) - sind(lat) * sind(dec)) / (cosd(lat) * cosd(dec))
    if (cosH < -1.0 || cosH > 1.0) return null
    return acosd(cosH)
}

internal class SolverResult(val hours: Double, val sp: SolarPositionResult, val iterations: Int, val targetZenith: Double, val error: Boolean = false)

internal class IterativeSolver(val jd: Double, val lat: Double, val lng: Double) {
    fun solve(side: Int, initialHour: Double, targetZenith: (SolarPositionResult) -> Double): SolverResult? {
        var currentHours = initialHour
        var prevHours = currentHours
        var lastSp: SolarPositionResult? = null
        var lastZenith = 0.0
        var iter = 0

        for (i in 0 until 15) {
            iter++
            val probeJd = jd + currentHours / 24.0
            val approxYear = 2000.0 + (probeJd - 2451545.0) / 365.25
            val deltaT = com.tauqeet.library.time.calculateDeltaT(approxYear)
            
            val j0 = kotlin.math.floor(probeJd - 0.5) + 0.5
            val ut = (probeJd - j0) * 24.0
            val sp = computeSolarPosition(j0, ut, deltaT)
            lastSp = sp
            
            val tz = targetZenith(sp)
            lastZenith = tz
            val transit = 12.0 - lng / 15.0 - sp.equationOfTime / 60.0

            if (side == 0) {
                currentHours = transit
            } else {
                val hDeg = solveHourAngle(tz, lat, sp.declination)
                if (hDeg == null) {
                    return SolverResult(currentHours, sp, iter, tz, error = true)
                }
                val hHours = hDeg / 15.0
                currentHours = if (side < 0) transit - hHours else transit + hHours
            }

            if (abs(currentHours - prevHours) * 3600 < 0.1) break
            prevHours = currentHours
        }
        return lastSp?.let { SolverResult(currentHours, it, iter, lastZenith) }
    }
}

internal class SunriseSunsetSolver(
    private val solver: IterativeSolver, 
    elevationMeters: Double,
    temperatureC: Double,
    pressureMbar: Double
) {
    private val dip = computeDipAngle(elevationMeters)
    private val refraction0 = getRefractionDegrees(0.0, temperatureC, pressureMbar)
    
    val zenithFn: (SolarPositionResult) -> Double = { sp ->
        90.0 + refraction0 + sp.semidiameter / 60.0 - sp.horizontalParallax / 60.0 + dip
    }
    
    fun solveSunrise(dhuhrHr: Double) = solver.solve(-1, dhuhrHr - 6.0, zenithFn)
    fun solveSunset(dhuhrHr: Double) = solver.solve(1, dhuhrHr + 6.0, zenithFn)
}

internal class AsrSolver(
    private val solver: IterativeSolver,
    private val lat: Double,
    private val madhab: Madhab,
    private val temperatureC: Double,
    private val pressureMbar: Double,
    private val transitSp: SolarPositionResult
) {
    fun solve(dhuhrHr: Double): SolverResult? {
        return solver.solve(1, dhuhrHr + 4.0) { sp ->
            val zZuhr = abs(lat - transitSp.declination)
            val sdZuhr = transitSp.semidiameter / 60.0
            val refrZuhr = getRefractionDegrees(90.0 - zZuhr, temperatureC, pressureMbar)
            val zZuhrVisual = zZuhr - refrZuhr - sdZuhr
            
            val zAsrVisual = atand(tand(zZuhrVisual) + madhab.shadowFactor)
            val refrAsr = getRefractionDegrees(90.0 - zAsrVisual, temperatureC, pressureMbar)
            val sdAsr = sp.semidiameter / 60.0
            
            zAsrVisual + refrAsr + sdAsr
        }
    }
}

internal class HighLatitudeResult(
    val fajr: Double?, val sunrise: Double?, val sunset: Double?, val isha: Double?, val isPolarDay: Boolean, val isPolarNight: Boolean
)

internal class HighLatitudeResolver(
    private val highLatRule: HighLatitudeRule,
    private val methodParams: CalculationMethodParameters
) {
    fun resolve(
        fajrHr: Double?, sunriseHr: Double?, sunsetHr: Double?, ishaHr: Double?, dhuhrHr: Double
    ): HighLatitudeResult {
        var finalFajr: Double? = fajrHr ?: (dhuhrHr - 8.0)
        var finalSunrise: Double? = sunriseHr ?: (dhuhrHr - 6.0)
        var finalSunset: Double? = sunsetHr ?: (dhuhrHr + 6.0)
        var finalIsha: Double? = ishaHr ?: (dhuhrHr + 8.0)
        var isPolarDay = false
        var isPolarNight = false

        if (finalSunrise != null && finalSunset != null && sunriseHr != null && sunsetHr != null) {
            var nightDuration = if (finalSunrise < finalSunset) {
                24.0 - (finalSunset - finalSunrise)
            } else {
                (finalSunrise - finalSunset)
            }
            if (nightDuration < 0.001) nightDuration = 0.001

            if (fajrHr == null || ishaHr == null) {
                isPolarDay = true
                when (highLatRule) {
                    HighLatitudeRule.MIDDLE_OF_NIGHT -> {
                        val halfNight = nightDuration / 2.0
                        if (fajrHr == null) finalFajr = finalSunrise - halfNight
                        if (ishaHr == null) finalIsha = finalSunset + halfNight
                    }
                    HighLatitudeRule.SEVENTH_OF_NIGHT -> {
                        val seventhNight = nightDuration / 7.0
                        if (fajrHr == null) finalFajr = finalSunrise - seventhNight
                        if (ishaHr == null) finalIsha = finalSunset + seventhNight
                    }
                    HighLatitudeRule.TWILIGHT_ANGLE -> {
                        val fajrProportion = methodParams.fajrAngle / 60.0
                        val ishaProportion = methodParams.ishaAngle / 60.0
                        if (fajrHr == null) finalFajr = finalSunrise - nightDuration * fajrProportion
                        if (ishaHr == null) finalIsha = finalSunset + nightDuration * ishaProportion
                    }
                }
            }
        } else {
            isPolarNight = true
        }
        
        return HighLatitudeResult(finalFajr, finalSunrise, finalSunset, finalIsha, isPolarDay, isPolarNight)
    }
}
