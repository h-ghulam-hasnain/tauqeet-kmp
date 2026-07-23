package com.tauqeet.library

import com.tauqeet.library.prayers.CalculationMethod
import org.junit.Test
import java.io.File

class ComparisonDumper {
    @Test
    fun dumpResults() {
        val lats = listOf(24.8607, 51.5072, 69.6492, -33.8688)
        val lngs = listOf(67.0011, -0.1276, 18.9553, 151.2093)
        val methods = listOf(CalculationMethod.MWL, CalculationMethod.ISNA)
        
        val sb = StringBuilder()
        sb.append("[\n")
        var first = true
        for (i in lats.indices) {
            val lat = lats[i]
            val lng = lngs[i]
            for (method in methods) {
                val tauqeet = Tauqeet(method = method)
                for (month in 1..12 step 3) {
                    val date = DateComponents(2024, month, 15)
                    val times = tauqeet.computePrayerTimes(date, lat, lng, 0.0)
                    val qibla = qiblaBearing(lat, lng)
                    if (!first) sb.append(",\n")
                    sb.append("""{"lat":$lat,"lng":$lng,"month":$month,"method":"${method.name}","fajr":${times.fajr},"sunrise":${times.sunrise},"dhuhr":${times.dhuhr},"asr":${times.asr},"maghrib":${times.maghrib},"isha":${times.isha},"qibla":$qibla}""")
                    first = false
                }
            }
        }
        sb.append("\n]")
        File("/home/hasnain/code/tauqeet/comparison-tests/kmp_results.json").writeText(sb.toString())
    }
}
