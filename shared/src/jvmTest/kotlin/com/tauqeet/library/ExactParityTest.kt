package com.tauqeet.library

import com.tauqeet.library.prayers.*
import org.junit.Test
import kotlin.math.*

class ExactParityTest {
    @Test
    fun testExact() {
        val tauqeet = Tauqeet(
            method = CalculationMethod.MWL,
            madhab = Madhab.SHAFI,
            highLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT
        )

        val req = prayerRequest {
            this.latitude = 24.8607
            this.longitude = 67.0011
            this.date = DateComponents(2026, 3, 21)
            this.timeZoneOffset = 0.0
            this.includeAdvancedMetadata = true
            calculation {
                method = CalculationMethod.MWL
                madhab = Madhab.SHAFI
                highLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT
            }
        }
        val result = tauqeet.computePrayerTimes(req)
        
        println("=== KMP RESULT ===")
        println("result fajr (ms): " + result.fajr)
        println("result sunrise (ms): " + result.sunrise)
        println("result dhuhr (ms): " + result.dhuhr)
        println("result maghrib (ms): " + result.maghrib)
        
        // Manual verification
        val lat = 24.8607
        val lng = 67.0011
        val jd = com.tauqeet.library.time.dateToJulianDay(2026, 3, 21.0)
        val solver = IterativeSolver(jd, lat, lng)
        val dhuhrRes = solver.solve(0, 12.0 - lng / 15.0) { 0.0 }
        val dhuhrHr = dhuhrRes?.hours ?: (12.0 - lng / 15.0)
        
        val srSsSolver = SunriseSunsetSolver(solver, 0.0, 10.0, 1010.0)
        val sunriseRes = srSsSolver.solveSunrise(dhuhrHr)
        val sunriseHr = sunriseRes?.hours
        
        println("=== Manual solver ===")
        println("dhuhrHr: $dhuhrHr")
        println("sunriseHr: $sunriseHr")
        println("sunriseMs (raw): " + (sunriseHr?.times(3600000.0)))
        
        // Do what applyRounding does
        val hoursToMs = 3600000.0
        val ms = sunriseHr!! * hoursToMs
        val seconds = ms / 1000.0
        val floored = floor(seconds)
        val finalMs = (floored * 1000).toLong()
        println("=== applyRounding logic ===")
        println("ms: $ms")
        println("seconds: $seconds")
        println("floored: $floored")
        println("finalMs: $finalMs")
    }
}
