package com.tauqeet.manual.prayer

import com.tauqeet.library.Tauqeet
import com.tauqeet.library.DateComponents
import com.tauqeet.library.toTimeString
import com.tauqeet.library.time.dateToJulianDay

fun main() {
    println("\n=============================================================")
    println("📅 EXAMPLE 1: MONTHLY CALENDAR — DEFAULT PARAMETERS")
    println("=============================================================")

    val lat = 31.39965
    val lng = 73.02003
    val timeZone = 5.0
    val year = 2026
    val month = 9

    val tauqeet = Tauqeet()
    
    val daysInMonth = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        else -> 30
    }

    println("\nYear  : $year")
    println("Month : $month")
    println("Days  : $daysInMonth")
    println("Times shown in UTC+$timeZone (Local Time)\n")

    try {
        for (day in 1..daysInMonth) {
            val date = DateComponents(year, month, day)
            val times = tauqeet.computePrayerTimes(date, lat, lng, timeZone)

            val dateStr = "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
            
            println(
                "  $dateStr  " +
                "Fajr: ${times.fajr.toTimeString()}  " +
                "Sunrise: ${times.sunrise.toTimeString()}  " +
                "Dhuhr: ${times.dhuhr.toTimeString()}  " +
                "Asr: ${times.asr.toTimeString()}  " +
                "Maghrib: ${times.maghrib.toTimeString()}  " +
                "Isha: ${times.isha.toTimeString()}"
            )
        }
    } catch (e: Exception) {
        println("Monthly Calendar (default) failed: ${e.message}")
    }

    println("=============================================================\n")
}
