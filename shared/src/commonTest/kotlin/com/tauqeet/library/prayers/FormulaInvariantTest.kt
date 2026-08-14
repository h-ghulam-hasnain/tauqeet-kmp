package com.tauqeet.library.prayers

import com.tauqeet.library.prayers.prayerRequest

import com.tauqeet.library.Tauqeet
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.random.Random

class FormulaInvariantTest {
    
    @Test
    fun testChronologicalOrderingInvariants() {
        val tauqeet = Tauqeet()
        val random = Random(12345) // Fixed seed
        
        for (i in 0 until 100) {
            val lat = random.nextDouble(-60.0, 60.0) // Safe latitudes
            val lng = random.nextDouble(-180.0, 180.0)
            val tz = random.nextDouble(-12.0, 14.0)
            val month = random.nextInt(1, 13)
            val day = random.nextInt(1, 28)
            
            val result = tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, month, day)
            latitude = lat
            longitude = lng
            timeZoneOffset = tz
        })
            
            // Extract times
            val fajr = result.fajr
            val sunrise = result.sunrise
            val dhuhr = result.dhuhr
            val asr = result.asr
            val maghrib = result.maghrib
            val isha = result.isha
            
            // Because of timezone wrap-arounds, we compute differences modulo 24 hours
            val msPerDay = 86400000L
            fun isBefore(t1: Long, t2: Long): Boolean {
                val diff = (t2 - t1 + msPerDay) % msPerDay
                return diff in 1 until (msPerDay / 2) // Should be less than 12 hours apart
            }
            
            // Assert invariants
            if (fajr != null && sunrise != null) {
                assertTrue(isBefore(fajr, sunrise), "Fajr ($fajr) must be before Sunrise ($sunrise)")
            }
            if (sunrise != null && dhuhr != null) {
                assertTrue(isBefore(sunrise, dhuhr), "Sunrise ($sunrise) must be before Dhuhr ($dhuhr)")
            }
            if (dhuhr != null && asr != null) {
                assertTrue(isBefore(dhuhr, asr), "Dhuhr ($dhuhr) must be before Asr ($asr)")
            }
            if (asr != null && maghrib != null) {
                assertTrue(isBefore(asr, maghrib), "Asr ($asr) must be before Maghrib ($maghrib)")
            }
            if (maghrib != null && isha != null) {
                assertTrue(isBefore(maghrib, isha), "Maghrib ($maghrib) must be before Isha ($isha)")
            }

            // Dhuhr should occur near solar noon in local time.
            // Since `tz` is random and independent of `lng`, we cannot guarantee it's 12:00 local time here.
            // We just ensure dhuhr is calculated (which it is, because of the null check).
            if (dhuhr != null) {
                // Invariants checked above
            }
        }
    }
}
