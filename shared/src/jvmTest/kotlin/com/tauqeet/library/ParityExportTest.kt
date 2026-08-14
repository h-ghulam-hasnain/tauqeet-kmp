package com.tauqeet.library

import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.HighLatitudeRule
import com.tauqeet.library.prayers.Madhab
import com.tauqeet.library.prayers.prayerRequest
import org.junit.Test
import java.io.File

/**
 * JVM-only test that reads the shared `parity_vectors.json` and computes
 * prayer times for every (location × date) combination using tauqeet-kmp.
 *
 * Output: `comparison/kmp_results.json` — an array of objects with the same
 * schema as the JS generator so the `compare_parity.mjs` script can diff them.
 *
 * Run:  ./gradlew :shared:jvmTest --tests "com.tauqeet.library.ParityExportTest"
 */
class ParityExportTest {

    // ── Minimal JSON-vector parser (no kotlinx-serialization dependency needed) ──

    data class Location(val name: String, val lat: Double, val lng: Double)

    private fun parseVectors(json: String): Pair<List<Location>, List<String>> {
        val locations = mutableListOf<Location>()
        val dates = mutableListOf<String>()

        // Parse locations array
        val locBlock = json.substringAfter("\"locations\"").substringAfter("[").substringBefore("]")
        val locEntries = locBlock.split("},").map { it.trim().trimEnd('}').trim() + "}" }
        for (raw in locEntries) {
            val name = raw.substringAfter("\"name\"").substringAfter("\"").substringBefore("\"")
            val lat = raw.substringAfter("\"lat\"").substringAfter(":").substringBefore(",").trim().toDouble()
            // lng may be the last field (no trailing comma) or have a trailing comma
            val lngStr = raw.substringAfter("\"lng\"").substringAfter(":").substringBefore("}").trim().trimEnd(',').trim()
            val lng = lngStr.toDouble()
            locations.add(Location(name, lat, lng))
        }

        // Parse dates array
        val dateBlock = json.substringAfter("\"dates\"").substringAfter("[").substringBefore("]")
        val dateEntries = dateBlock.split(",").map { it.trim().trim('"') }.filter { it.isNotBlank() }
        dates.addAll(dateEntries)

        return locations to dates
    }

    @Test
    fun exportKmpParity() {
        // Locate the vectors file relative to the working directory.
        // Gradle sets user.dir to the subproject dir (shared/), so we walk up
        // two levels: shared/ → tauqeet-kmp/ → library/
        val cwd = File(System.getProperty("user.dir"))
        val vectorsFile = File(cwd, "../../comparison/parity_vectors.json").canonicalFile
        require(vectorsFile.exists()) {
            "Cannot find parity_vectors.json at ${vectorsFile.absolutePath}. " +
                    "CWD is ${cwd.absolutePath}."
        }

        val vectorsJson = vectorsFile.readText()
        val (locations, dates) = parseVectors(vectorsJson)

        // We use MWL + Shafi + MiddleOfNight to match the vectors config
        val tauqeet = Tauqeet(
            method = CalculationMethod.MWL,
            madhab = Madhab.SHAFI,
            highLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT
        )

        val sb = StringBuilder()
        sb.append("[\n")
        var first = true

        for (loc in locations) {
            for (dateStr in dates) {
                val parts = dateStr.split("-").map { it.toInt() }
                val year = parts[0]
                val month = parts[1]
                val day = parts[2]

                if (!first) sb.append(",\n")
                first = false

                try {
                    val result = tauqeet.computePrayerTimes(prayerRequest {
                        this.latitude = loc.lat
                        this.longitude = loc.lng
                        this.date = DateComponents(year, month, day)
                        this.timeZoneOffset = 0.0
                    })

                    sb.append(
                        """  {"location":"${loc.name}","lat":${loc.lat},"lng":${loc.lng},"date":"$dateStr",""" +
                                """"fajr":${result.fajr},""" +
                                """"sunrise":${result.sunrise},""" +
                                """"dhuhr":${result.dhuhr},""" +
                                """"asr":${result.asr},""" +
                                """"maghrib":${result.maghrib},""" +
                                """"isha":${result.isha}}"""
                    )
                } catch (e: Exception) {
                    sb.append(
                        """  {"location":"${loc.name}","lat":${loc.lat},"lng":${loc.lng},"date":"$dateStr","error":"${e.message?.replace("\"", "'")}"}"""
                    )
                }
            }
        }

        sb.append("\n]")

        val outFile = File(projectRoot, "../comparison/kmp_results.json")
        outFile.writeText(sb.toString())
        println("✅ KMP parity results written to ${outFile.absolutePath} (${locations.size * dates.size} entries)")
    }
}
