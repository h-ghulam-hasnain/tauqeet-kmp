package com.tauqeet.manual.qibla

import com.tauqeet.library.qiblaBearing

fun main() {
    println("\n=============================================================")
    println("🕋 EXAMPLE 1: QIBLA BEARING — DEFAULT")
    println("=============================================================")

    // Faisalabad Coordinates
    val lat = 31.4187
    val lng = 73.0791

    try {
        val qibla = qiblaBearing(lat, lng)

        println("\nInputs:")
        println("  Latitude : $lat°")
        println("  Longitude: $lng°")

        println("\nResult:")
        println("  Qibla Bearing: $qibla° (from North)")

    } catch (err: Exception) {
        println("Qibla Calculation failed: ${err.message}")
    }

    println("=============================================================\n")
}
