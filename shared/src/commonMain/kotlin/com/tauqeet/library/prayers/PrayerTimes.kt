package com.tauqeet.library.prayers

import com.tauqeet.library.astronomy.computeSolarPosition
import com.tauqeet.library.internal.acosd
import com.tauqeet.library.internal.atand
import com.tauqeet.library.internal.cosd
import com.tauqeet.library.internal.sind
import com.tauqeet.library.internal.tand
import kotlin.math.abs

data class PrayerTimesMetadata(
    val method: String,
    val madhab: String,
    val highLatitudeRule: String,
    val isPolarDay: Boolean,
    val isPolarNight: Boolean
)

data class PrayerTimesResult(
    val fajr: Long,
    val sunrise: Long,
    val dhahwaKubra: Long,
    val dhuhr: Long,
    val asr: Long,
    val maghrib: Long,
    val isha: Long,
    val metadata: PrayerTimesMetadata? = null
)

/**
 * Calculates the hour angle (in degrees) for a target zenith.
 */
private fun solveHourAngle(targetZenith: Double, lat: Double, dec: Double): Double? {
    val cosH = (cosd(targetZenith) - sind(lat) * sind(dec)) / (cosd(lat) * cosd(dec))
    if (cosH < -1.0 || cosH > 1.0) return null
    return acosd(cosH)
}

/**
 * Computes the exact prayer times for a given date, location, and method.
 * Uses a standard iterative convergence over the day's changing solar position.
 * Returns the times in UTC hours since midnight (0.0 to 24.0).
 * Multiply by 60 for minutes since midnight.
 */
fun computePrayerTimes(
    lat: Double,
    lng: Double,
    jd: Double,
    method: CalculationMethod,
    madhab: Madhab = Madhab.SHAFI,
    highLatRule: HighLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
    elevationMeters: Double = 0.0,
    temperatureC: Double = 10.0,
    pressureMbar: Double = 1010.0
): PrayerTimesResult {
    // A nested function to solve for the exact UTC hour of an event iteratively
    fun solveIteratively(side: Int, targetZenith: (com.tauqeet.library.astronomy.SolarPositionResult) -> Double, initialHour: Double): Double? {
        var currentHours = initialHour
        var prevHours = currentHours

        for (i in 0 until 15) {
            val probeJd = jd + currentHours / 24.0
            // Assuming current year is roughly derived from JD for deltaT calculation
            // Approximation for year: 2000 + (probeJd - 2451545.0) / 365.25
            val approxYear = 2000.0 + (probeJd - 2451545.0) / 365.25
            val deltaT = com.tauqeet.library.time.calculateDeltaT(approxYear)
            
            // J = JD at 0h UT, ut = UT time in hours
            val j0 = kotlin.math.floor(probeJd - 0.5) + 0.5
            val ut = (probeJd - j0) * 24.0
            val sp = computeSolarPosition(j0, ut, deltaT)
            
            val transit = 12.0 - lng / 15.0 - sp.equationOfTime / 60.0

            if (side == 0) {
                currentHours = transit
            } else {
                val hDeg = solveHourAngle(targetZenith(sp), lat, sp.declination) ?: return null
                val hHours = hDeg / 15.0
                currentHours = if (side < 0) transit - hHours else transit + hHours
            }

            if (abs(currentHours - prevHours) * 3600 < 0.1) break
            prevHours = currentHours
        }
        return currentHours
    }

    val initialDhuhr = 12.0 - lng / 15.0
    val dhuhrHr = solveIteratively(0, { 0.0 }, initialDhuhr) ?: initialDhuhr

    // Get SP at transit for Asr calculations
    val dhuhrJd = jd + dhuhrHr / 24.0
    val dhuhrApproxYear = 2000.0 + (dhuhrJd - 2451545.0) / 365.25
    val dhuhrDeltaT = com.tauqeet.library.time.calculateDeltaT(dhuhrApproxYear)
    val dhuhrJ0 = kotlin.math.floor(dhuhrJd - 0.5) + 0.5
    val dhuhrUt = (dhuhrJd - dhuhrJ0) * 24.0
    val transitSp = computeSolarPosition(dhuhrJ0, dhuhrUt, dhuhrDeltaT)

    val dip = computeDipAngle(elevationMeters)
    val refraction0 = getRefractionDegrees(0.0, temperatureC, pressureMbar)
    
    val sunriseSunsetZenithFn: (com.tauqeet.library.astronomy.SolarPositionResult) -> Double = { sp ->
        90.0 + refraction0 + sp.semidiameter / 60.0 - sp.horizontalParallax / 60.0 + dip
    }

    val sunriseHr = solveIteratively(-1, sunriseSunsetZenithFn, dhuhrHr - 6.0)
    val sunsetHr = solveIteratively(1, sunriseSunsetZenithFn, dhuhrHr + 6.0)

    val fajrHr = solveIteratively(-1, { 90.0 + method.params.fajrAngle }, dhuhrHr - 8.0)

    val ishaHr = if (method.params.ishaInterval > 0 && sunsetHr != null) {
        sunsetHr + method.params.ishaInterval / 60.0
    } else {
        solveIteratively(1, { 90.0 + method.params.ishaAngle }, dhuhrHr + 8.0)
    }

    val asrHr = solveIteratively(1, { sp ->
        val zZuhr = abs(lat - transitSp.declination)
        val sdZuhr = transitSp.semidiameter / 60.0
        val refrZuhr = getRefractionDegrees(90.0 - zZuhr, temperatureC, pressureMbar)
        val zZuhrVisual = zZuhr - refrZuhr - sdZuhr
        
        val zAsrVisual = atand(tand(zZuhrVisual) + madhab.shadowFactor)
        val refrAsr = getRefractionDegrees(90.0 - zAsrVisual, temperatureC, pressureMbar)
        val sdAsr = sp.semidiameter / 60.0
        
        zAsrVisual + refrAsr + sdAsr
    }, dhuhrHr + 4.0)

    val maghribHr = if (method.params.maghribInterval > 0 && sunsetHr != null) {
        sunsetHr + method.params.maghribInterval / 60.0
    } else if (method.params.maghribAngle > 0.0) {
        solveIteratively(1, { 90.0 + method.params.maghribAngle }, dhuhrHr + 6.5)
    } else {
        sunsetHr
    }

    // High latitude fallback logic
    var finalFajr = fajrHr ?: (dhuhrHr - 8.0)
    var finalSunrise = sunriseHr ?: (dhuhrHr - 6.0)
    var finalSunset = sunsetHr ?: (dhuhrHr + 6.0)
    var finalMaghrib = maghribHr ?: finalSunset
    var finalIsha = ishaHr ?: (dhuhrHr + 8.0)
    var finalAsr = asrHr ?: (dhuhrHr + 4.0)
    
    var isPolarDay = false
    var isPolarNight = false

    if (sunriseHr != null && sunsetHr != null) {
        var nightDuration = if (finalSunrise < finalSunset) {
            24.0 - (finalSunset - finalSunrise)
        } else {
            (finalSunrise - finalSunset) // shouldn't usually happen with valid data
        }
        
        // Prevent division by zero or NaN issues at polar boundaries
        if (nightDuration < 0.001) {
            nightDuration = 0.001
        }

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
                    val fajrProportion = method.params.fajrAngle / 60.0
                    val ishaProportion = method.params.ishaAngle / 60.0
                    if (fajrHr == null) finalFajr = finalSunrise - nightDuration * fajrProportion
                    if (ishaHr == null) finalIsha = finalSunset + nightDuration * ishaProportion
                }
            }
        }
    } else {
        isPolarNight = true
    }

    val dhahwaKubraHr = (finalFajr + finalSunset) / 2.0

    // Times are calculated in UTC decimal hours. We convert to milliseconds since midnight UTC.
    val hoursToMs = 3600000.0
    return PrayerTimesResult(
        fajr = (finalFajr * hoursToMs).toLong(),
        sunrise = (finalSunrise * hoursToMs).toLong(),
        dhahwaKubra = (dhahwaKubraHr * hoursToMs).toLong(),
        dhuhr = (dhuhrHr * hoursToMs).toLong(),
        asr = (finalAsr * hoursToMs).toLong(),
        maghrib = (finalMaghrib * hoursToMs).toLong(),
        isha = (finalIsha * hoursToMs).toLong(),
        metadata = PrayerTimesMetadata(
            method = method.name,
            madhab = madhab.name,
            highLatitudeRule = highLatRule.name,
            isPolarDay = isPolarDay,
            isPolarNight = isPolarNight
        )
    )
}
