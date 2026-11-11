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

    private fun parseVectors(json: String): ParsedVectors {
        val locations = mutableListOf<Location>()
        
        val locBlock = json.substringAfter("\"locations\"").substringAfter("[").substringBefore("]")
        val locEntries = locBlock.split("},").map { it.trim().trimEnd('}').trim() + "}" }
        for (raw in locEntries) {
            val name = raw.substringAfter("\"name\"").substringAfter("\"").substringBefore("\"")
            val lat = raw.substringAfter("\"lat\"").substringAfter(":").substringBefore(",").trim().toDouble()
            val lngStr = raw.substringAfter("\"lng\"").substringAfter(":").substringBefore("}").trim().trimEnd(',').trim()
            val lng = lngStr.toDouble()
            locations.add(Location(name, lat, lng))
        }

        fun parseArray(key: String): List<String> {
            val block = json.substringAfter("\"$key\"").substringAfter("[").substringBefore("]")
            return block.split(",").map { it.trim().trim('"') }.filter { it.isNotBlank() }
        }

        return ParsedVectors(
            locations = locations,
            dates = parseArray("dates"),
            methods = parseArray("methods"),
            madhabs = parseArray("madhabs"),
            highLatitudeRules = parseArray("highLatitudeRules")
        )
    }

    data class ParsedVectors(
        val locations: List<Location>,
        val dates: List<String>,
        val methods: List<String>,
        val madhabs: List<String>,
        val highLatitudeRules: List<String>
    )

    @Test
    fun exportKmpParity() {
        val cwd = File(System.getProperty("user.dir"))
        val vectorsFile = File(cwd, "../../comparison/parity_vectors.json").canonicalFile
        org.junit.Assume.assumeTrue(
            "Parity vectors missing at ${vectorsFile.absolutePath} (CWD: ${cwd.absolutePath}); " +
            "developer differential test, skipping on CI.",
            vectorsFile.exists()
        )

        val vectorsJson = vectorsFile.readText()
        val parsed = parseVectors(vectorsJson)

        val sb = StringBuilder()
        sb.append("[\n")
        var first = true

        for (loc in parsed.locations) {
            for (dateStr in parsed.dates) {
                for (methodStr in parsed.methods) {
                    for (madhabStr in parsed.madhabs) {
                        for (highLatStr in parsed.highLatitudeRules) {
                            val parts = dateStr.split("-").map { it.toInt() }
                            val year = parts[0]
                            val month = parts[1]
                            val day = parts[2]

                            if (!first) sb.append(",\n")
                            first = false

                            try {
                                val methodEnum = CalculationMethod.valueOf(methodStr.uppercase())
                                val madhabEnum = Madhab.valueOf(madhabStr.uppercase())
                                // Kotlin KMP enum names are uppercase with underscores, the JS strings are PascalCase
                                // MiddleOfNight -> MIDDLE_OF_NIGHT
                                val highLatEnumStr = highLatStr.replace(Regex("([a-z])([A-Z]+)"), "$1_$2").uppercase()
                                val highLatEnum = HighLatitudeRule.valueOf(highLatEnumStr)

                                val tauqeet = Tauqeet()
                                val result = tauqeet.computePrayerTimes(prayerRequest {
                                    this.latitude = loc.lat
                                    this.longitude = loc.lng
                                    this.date = DateComponents(year, month, day)
                                    this.timeZoneOffset = 0.0
                                    calculation {
                                        method = methodEnum
                                        madhab = madhabEnum
                                        highLatitudeRule = highLatEnum
                                    }
                                })

                                sb.append(
                                    """  {"location":"${loc.name}","lat":${loc.lat},"lng":${loc.lng},"date":"$dateStr","method":"$methodStr","madhab":"$madhabStr","highLatitudeRule":"$highLatStr",""" +
                                            """"fajr":${result.fajr},""" +
                                            """"sunrise":${result.sunrise},""" +
                                            """"dhuhr":${result.dhuhr},""" +
                                            """"asr":${result.asr},""" +
                                            """"maghrib":${result.maghrib},""" +
                                            """"isha":${result.isha},""" +
                                            """"status":"${result.status}",""" +
                                            """"metadata":{"method":"${result.metadata?.method}","madhab":"${result.metadata?.madhab}","highLatitudeRule":"${result.metadata?.highLatitudeRule}","isPolarDay":${result.metadata?.isPolarDay},"isPolarNight":${result.metadata?.isPolarNight}},""" +
                                            """"resolutionInfo":{"solver":"${result.resolutionInfo?.solver}","ruleApplied":"${result.resolutionInfo?.ruleApplied?.name}"}}"""
                                )
                            } catch (e: Exception) {
                                sb.append(
                                    """  {"location":"${loc.name}","lat":${loc.lat},"lng":${loc.lng},"date":"$dateStr","method":"$methodStr","madhab":"$madhabStr","highLatitudeRule":"$highLatStr",""" +
                                            """"error":"${e.message?.replace("\"", "\\\"")}"}"""
                                )
                            }
                        }
                    }
                }
            }
        }
        sb.append("\n]")

        val outFile = File(cwd, "../../comparison/kmp_results.json").canonicalFile
        outFile.writeText(sb.toString())
        println("✅ KMP parity results written to ${outFile.absolutePath} (${parsed.locations.size * parsed.dates.size * parsed.methods.size * parsed.madhabs.size * parsed.highLatitudeRules.size} entries)")
    }
}
