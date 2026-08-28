package com.tauqeet.library

import com.tauqeet.library.astronomy.computeSolarPosition
import com.tauqeet.library.internal.cosd
import com.tauqeet.library.internal.sind
import com.tauqeet.library.internal.acosd
import com.tauqeet.library.prayers.getRefractionDegrees
import com.tauqeet.library.prayers.computeDipAngle
import com.tauqeet.library.time.dateToJulianDay
import com.tauqeet.library.time.calculateDeltaT
import org.junit.Test
import kotlin.math.abs
import kotlin.math.floor

class SolverTraceTest {

    private fun solveHA(zenith: Double, lat: Double, dec: Double): Double? {
        val cosH = (cosd(zenith) - sind(lat) * sind(dec)) / (cosd(lat) * cosd(dec))
        if (cosH < -1.0 || cosH > 1.0) return null
        return acosd(cosH)
    }

    @Test
    fun traceSolver() {
        val lat = 78.2232
        val lng = 15.6267
        val baseDateJd = dateToJulianDay(2026, 3, 21.0)

        println("=== KMP Solver Trace for Longyearbyen Sunrise ===")
        println("baseDateJd: $baseDateJd")

        val dip = computeDipAngle(0.0)
        val refraction0 = getRefractionDegrees(0.0, 10.0, 1010.0)
        println("dip: $dip")
        println("refraction0 (degrees): $refraction0")

        val initialEstimate = 6.0 - lng / 15.0
        println("initialEstimate: $initialEstimate")

        var currentHours = initialEstimate
        var prevHours = currentHours

        for (i in 0 until 15) {
            val probeJd = baseDateJd + currentHours / 24.0
            val approxYear = 2000.0 + (probeJd - 2451545.0) / 365.25
            val deltaT = calculateDeltaT(approxYear)
            val j0 = floor(probeJd - 0.5) + 0.5
            val ut = (probeJd - j0) * 24.0
            val sp = computeSolarPosition(j0, ut, deltaT)

            val zenith = 90.0 + refraction0 + sp.semidiameter / 60.0 - sp.horizontalParallax / 60.0 + dip
            val transit = 12.0 - lng / 15.0 - sp.equationOfTime / 60.0
            val hDeg = solveHA(zenith, lat, sp.declination)
            val hHours = if (hDeg != null) hDeg / 15.0 else Double.NaN
            val newHours = transit - hHours

            println()
            println("Iter $i:")
            println("  probeJd=$probeJd")
            println("  j0=$j0, ut=$ut")
            println("  deltaT=$deltaT")
            println("  dec=${sp.declination}")
            println("  eot=${sp.equationOfTime}")
            println("  sd=${sp.semidiameter}, hp=${sp.horizontalParallax}")
            println("  zenith=$zenith")
            println("  transit=$transit")
            println("  hDeg=$hDeg, hHours=$hHours")
            println("  newHours=$newHours")

            currentHours = newHours
            val diffSeconds = abs(currentHours - prevHours) * 3600.0
            println("  diffSeconds=$diffSeconds")
            if (diffSeconds < 0.1) {
                println("  CONVERGED")
                break
            }
            prevHours = currentHours
        }

        println()
        println("Final currentHours: $currentHours")
        println("As ms since midnight: ${currentHours * 3600000.0}")
        println("Floored to seconds*1000: ${floor(currentHours * 3600000.0 / 1000.0).toLong() * 1000}")
    }
}
