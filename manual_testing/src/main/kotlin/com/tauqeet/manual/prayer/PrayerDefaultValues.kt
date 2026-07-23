package com.tauqeet.manual.prayer

import com.tauqeet.library.Tauqeet
import com.tauqeet.library.DateComponents
import com.tauqeet.library.toTimeString

fun main() {
    println("\n=============================================================")
    println("🧪 TEST 1: PRAYER TIMES WITH DEFAULT VALUES")
    println("=============================================================")

    val lat = 31.39965
    val lng = 73.02003
    val temperatureC = 12.714
    val pressureMbar = 1010.0
    val timeZone = 5.0

    val tauqeet = Tauqeet(
        temperatureC = temperatureC,
        pressureMbar = pressureMbar
    )

    println("Config inputs:")
    println("Lat: $lat, Lng: $lng")
    println("Timezone: +$timeZone")

    try {
        val date = DateComponents(2027, 1, 1)
        val times = tauqeet.computePrayerTimes(date, lat, lng, timeZone)

        println("\nResults:")
        println("   fajr           : ${times.fajr.toTimeString()}")
        println("   sunrise        : ${times.sunrise.toTimeString()}")
        println("   dhuhr          : ${times.dhuhr.toTimeString()}")
        println("   asr            : ${times.asr.toTimeString()}")
        println("   maghrib        : ${times.maghrib.toTimeString()}")
        println("   isha           : ${times.isha.toTimeString()}")

    } catch (e: Exception) {
        println("Calculation Failed: ${e.message}")
    }

    println("=============================================================\n")
}
