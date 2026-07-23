package com.tauqeet.manual.prayer

import com.tauqeet.library.Tauqeet
import com.tauqeet.library.DateComponents
import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.HighLatitudeRule
import com.tauqeet.library.prayers.Madhab
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class ParityResult(
    val loc: String,
    val date: String,
    val fajr: Long?,
    val sunrise: Long?,
    val dhuhr: Long?,
    val asr: Long?,
    val maghrib: Long?,
    val isha: Long?
)

fun main() {
    val locations = listOf(
        Pair("Karachi", Pair(24.86, 67.00)),
        Pair("Sydney", Pair(-33.87, 151.21)),
        Pair("London", Pair(51.51, -0.13)),
        Pair("Moscow", Pair(55.76, 37.62)),
        Pair("Tromso", Pair(69.65, 18.96)),
        Pair("Longyearbyen", Pair(78.22, 15.65))
    )

    val dates = listOf(
        DateComponents(2026, 3, 21),
        DateComponents(2026, 6, 21),
        DateComponents(2026, 9, 21),
        DateComponents(2026, 12, 21)
    )

    val tauqeet = Tauqeet(
        method = CalculationMethod.MWL,
        madhab = Madhab.SHAFI,
        highLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT
    )

    val results = mutableListOf<ParityResult>()

    for (loc in locations) {
        for (date in dates) {
            val times = tauqeet.computePrayerTimes(date, loc.second.first, loc.second.second, 0.0)
            val dateStr = "${date.year}-${date.month.toString().padStart(2, '0')}-${date.day.toString().padStart(2, '0')}"
            results.add(ParityResult(
                loc = loc.first,
                date = dateStr,
                fajr = times.fajr,
                sunrise = times.sunrise,
                dhuhr = times.dhuhr,
                asr = times.asr,
                maghrib = times.maghrib,
                isha = times.isha
            ))
        }
    }

    val jsonString = Json { prettyPrint = true }.encodeToString(results)
    File("../kmp_results.json").writeText(jsonString)
    println("KMP Done")
}
