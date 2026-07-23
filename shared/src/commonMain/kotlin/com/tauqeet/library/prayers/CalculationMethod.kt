package com.tauqeet.library.prayers

data class CalculationMethodParameters(
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaInterval: Int = 0,
    val maghribAngle: Double = 0.0,
    val maghribInterval: Int = 0,
    val methodAdjustment: Int = 0
)

enum class CalculationMethod(val params: CalculationMethodParameters) {
    MWL(CalculationMethodParameters(fajrAngle = 18.0, ishaAngle = 17.0)),
    ISNA(CalculationMethodParameters(fajrAngle = 15.0, ishaAngle = 15.0)),
    EGYPT(CalculationMethodParameters(fajrAngle = 19.5, ishaAngle = 17.5)),
    MAKKAH(CalculationMethodParameters(fajrAngle = 18.5, ishaAngle = 0.0, ishaInterval = 90)),
    KARACHI(CalculationMethodParameters(fajrAngle = 18.0, ishaAngle = 18.0)),
    TEHRAN(CalculationMethodParameters(fajrAngle = 17.7, ishaAngle = 14.0, maghribAngle = 4.5)),
    JAFARI(CalculationMethodParameters(fajrAngle = 16.0, ishaAngle = 14.0, maghribAngle = 4.0)),
    GULF(CalculationMethodParameters(fajrAngle = 19.5, ishaAngle = 0.0, ishaInterval = 90)),
    KUWAIT(CalculationMethodParameters(fajrAngle = 18.0, ishaAngle = 17.5)),
    QATAR(CalculationMethodParameters(fajrAngle = 18.0, ishaAngle = 0.0, ishaInterval = 90)),
    SINGAPORE(CalculationMethodParameters(fajrAngle = 20.0, ishaAngle = 18.0)),
    FRANCE(CalculationMethodParameters(fajrAngle = 12.0, ishaAngle = 12.0)),
    TURKEY(CalculationMethodParameters(fajrAngle = 18.0, ishaAngle = 17.0)),
    RUSSIA(CalculationMethodParameters(fajrAngle = 16.0, ishaAngle = 15.0))
}

enum class Madhab(val shadowFactor: Int) {
    SHAFI(1), // Includes Maliki and Hanbali
    HANAFI(2)
}
