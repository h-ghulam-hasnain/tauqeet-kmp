package com.tauqeet.library.prayers

import com.tauqeet.library.prayers.prayerRequest

import com.tauqeet.library.time.dateToJulianDay
import com.tauqeet.library.Tauqeet
import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.Madhab
import kotlin.test.Test
import kotlin.test.assertTrue
import com.tauqeet.library.DateComponents

class MadhabTest {
    @Test
    fun testMadhabConsistency() {
        val lat = 24.8607
        val lng = 67.0011
        val jd = dateToJulianDay(2024, 4, 27.0)
        
        val shafiTimes = Tauqeet().computePrayerTimes(prayerRequest {
            this.latitude = lat
            this.longitude = lng
            this.date = DateComponents(2024, 4, 27)
            calculation {
                method = CalculationMethod.MWL
                madhab = Madhab.SHAFI
            }
        })
        val hanafiTimes = Tauqeet().computePrayerTimes(prayerRequest {
            this.latitude = lat
            this.longitude = lng
            this.date = DateComponents(2024, 4, 27)
            calculation {
                method = CalculationMethod.MWL
                madhab = Madhab.HANAFI
            }
        })
        
        // Asr Hanafi must always be later than Asr Shafi
        assertTrue(hanafiTimes.asr!! > shafiTimes.asr!!, "Hanafi Asr must be later than Shafi Asr")
        
        // Other times should remain identical
        assertTrue(hanafiTimes.fajr == shafiTimes.fajr)
        assertTrue(hanafiTimes.dhuhr == shafiTimes.dhuhr)
        assertTrue(hanafiTimes.maghrib == shafiTimes.maghrib)
    }
}
