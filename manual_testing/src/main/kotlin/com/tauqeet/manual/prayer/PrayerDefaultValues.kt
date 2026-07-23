package com.tauqeet.manual.prayer

import com.tauqeet.library.Tauqeet
import com.tauqeet.library.DateComponents
import com.tauqeet.library.toTimeString

import java.time.LocalDate

fun main() {
    println("\n=============================================================")
    println("🧪 TEST 1: PRAYER TIMES WITH DEFAULT VALUES")
    println("=============================================================")

    // Using Faisalabad / Punjab coordinates as a default testing point
    val lat = 31.39965
    val lng = 73.02003
    val timeZone = 5.0 // UTC

    // Instantiating with default parameters (Karachi, Hanafi, 12.714C, 1010mbar, 0m elevation)
    val tauqeet = Tauqeet()

    val today = LocalDate.now()
    val date = DateComponents(today.year, today.monthValue, today.dayOfMonth)

    println("Config inputs:")
    println("Date: ${date.year}-${date.month}-${date.day} (Today)")
    println("Lat: $lat, Lng: $lng")
    println("Timezone: UTC")
    println("Method: ${tauqeet.method.name}")
    println("Madhab: ${tauqeet.madhab.name}")
    println("Temp: ${tauqeet.temperatureC}°C, Pressure: ${tauqeet.pressureMbar} mbar, Elevation: ${tauqeet.elevationMeters} ft")

    try {
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
