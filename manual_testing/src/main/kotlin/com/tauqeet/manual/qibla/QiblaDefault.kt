package com.tauqeet.manual.qibla

import com.tauqeet.library.qibla.tauqeetQibla

fun main() {
    println("\n=============================================================")
    println("🕋 EXAMPLE 1: QIBLA BEARING & DISTANCE — DEFAULT")
    println("=============================================================")

    // Faisalabad Coordinates
    val lat = 31.4187
    val lng = 73.0791

    try {
        val qibla = tauqeetQibla(lat, lng)

        println("\nInputs:")
        println("  Latitude : $lat°")
        println("  Longitude: $lng°")

        println("\nResult:")
        println("  Qibla Bearing: ${qibla?.bearing}° (from North)")
        println("  Distance     : ${qibla?.distanceKm} km")

    } catch (err: Exception) {
        println("Qibla Calculation failed: ${err.message}")
    }

    println("=============================================================\n")
}
