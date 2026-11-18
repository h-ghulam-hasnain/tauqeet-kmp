package com.tauqeet.library.prayers

/**
 * Raw angle and interval parameters that define a single calculation authority's convention.
 *
 * Most callers should use [CalculationMethod] enum entries rather than constructing this directly.
 * Pass a custom instance via `Tauqeet(customMethodParams = …)` to override a named method's angles.
 *
 * @property fajrAngle         Solar depression angle (degrees below horizon) that marks Fajr.
 * @property ishaAngle         Solar depression angle (degrees below horizon) that marks Isha.
 * @property ishaInterval      Fixed interval in minutes after Sunset for Isha (overrides [ishaAngle] when > 0).
 * @property maghribAngle      Solar depression angle for Maghrib (overrides Sunset when > 0.0).
 * @property maghribInterval   Fixed interval in minutes after Sunset for Maghrib (overrides [maghribAngle] when > 0).
 * @property methodAdjustment  Global minute adjustment applied to all computed times.
 */
data class CalculationMethodParameters(
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaInterval: Int = 0,
    val maghribAngle: Double = 0.0,
    val maghribInterval: Int = 0,
    val methodAdjustment: Int = 0
)

/**
 * Named Islamic prayer-time calculation authorities, each bundling the Fajr/Isha twilight angles
 * (and optional Maghrib/Isha fixed intervals) used by that authority.
 *
 * Pass the desired value to the [com.tauqeet.library.Tauqeet] constructor:
 * ```kotlin
 * val tauqeet = Tauqeet(method = CalculationMethod.ISNA)
 * ```
 *
 * @property params  The [CalculationMethodParameters] (angles and intervals) for this authority.
 */
enum class CalculationMethod(val params: CalculationMethodParameters) {
    /** Muslim World League — Fajr 18°, Isha 17°. Widely used in Europe, Far East, and parts of America. */
    MWL(CalculationMethodParameters(fajrAngle = 18.0, ishaAngle = 17.0)),

    /** Islamic Society of North America — Fajr 15°, Isha 15°. Standard for the USA and Canada. */
    ISNA(CalculationMethodParameters(fajrAngle = 15.0, ishaAngle = 15.0)),

    /** Egyptian General Authority of Survey — Fajr 19.5°, Isha 17.5°. Used in Africa and parts of Asia. */
    EGYPT(CalculationMethodParameters(fajrAngle = 19.5, ishaAngle = 17.5)),

    /** Umm al-Qura University, Makkah — Fajr 18.5°, Isha fixed 90 min after Sunset. Used in Saudi Arabia. */
    MAKKAH(CalculationMethodParameters(fajrAngle = 18.5, ishaAngle = 0.0, ishaInterval = 90)),

    /** Alias for [MAKKAH] — Umm al-Qura University convention. */
    UMM_AL_QURA(CalculationMethodParameters(fajrAngle = 18.5, ishaAngle = 0.0, ishaInterval = 90)),

    /** University of Islamic Sciences, Karachi — Fajr 18°, Isha 18°. Used in Pakistan, Bangladesh, and India. */
    KARACHI(CalculationMethodParameters(fajrAngle = 18.0, ishaAngle = 18.0)),

    /** Institute of Geophysics, University of Tehran — Fajr 17.7°, Isha 14°, Maghrib 4.5°. Used in Iran. */
    TEHRAN(CalculationMethodParameters(fajrAngle = 17.7, ishaAngle = 14.0, maghribAngle = 4.5)),

    /** Shia Ithna Ashari / Leva Research Institute, Qom — Fajr 16°, Isha 14°, Maghrib 4°. */
    JAFARI(CalculationMethodParameters(fajrAngle = 16.0, ishaAngle = 14.0, maghribAngle = 4.0)),

    /** Gulf region convention — Fajr 19.5°, Isha fixed 90 min after Sunset. */
    GULF(CalculationMethodParameters(fajrAngle = 19.5, ishaAngle = 0.0, ishaInterval = 90)),

    /** Dubai Islamic Affairs & Charitable Activities Department — Fajr 18.2°, Isha 18.2°. */
    DUBAI(CalculationMethodParameters(fajrAngle = 18.2, ishaAngle = 18.2)),

    /** Kuwait Ministry of Awqaf — Fajr 18°, Isha 17.5°. */
    KUWAIT(CalculationMethodParameters(fajrAngle = 18.0, ishaAngle = 17.5)),

    /** Qatar Ministry of Islamic Affairs — Fajr 18°, Isha fixed 90 min after Sunset. */
    QATAR(CalculationMethodParameters(fajrAngle = 18.0, ishaAngle = 0.0, ishaInterval = 90)),

    /** Majlis Ugama Islam Singapura (MUIS) — Fajr 20°, Isha 18°. Used in Singapore. */
    SINGAPORE(CalculationMethodParameters(fajrAngle = 20.0, ishaAngle = 18.0)),

    /** Department of Islamic Development Malaysia (JAKIM) — Fajr 20°, Isha 18°. */
    JAKIM(CalculationMethodParameters(fajrAngle = 20.0, ishaAngle = 18.0)),

    /** Ministry of Religious Affairs, Indonesia (Kemenag) — Fajr 20°, Isha 18°. */
    KEMENAG(CalculationMethodParameters(fajrAngle = 20.0, ishaAngle = 18.0)),

    /** Grande Mosquée de Paris / Conseil Français du Culte Musulman — Fajr 12°, Isha 12°. */
    FRANCE(CalculationMethodParameters(fajrAngle = 12.0, ishaAngle = 12.0)),

    /** France — 12° variant (alias for [FRANCE]). */
    FRANCE_12(CalculationMethodParameters(fajrAngle = 12.0, ishaAngle = 12.0)),

    /** France — 15° variant. Used by some French Muslim organisations. */
    FRANCE_15(CalculationMethodParameters(fajrAngle = 15.0, ishaAngle = 15.0)),

    /** France — 18° variant. Used by some French Muslim organisations. */
    FRANCE_18(CalculationMethodParameters(fajrAngle = 18.0, ishaAngle = 18.0)),

    /** Diyanet İşleri Başkanlığı, Turkey — Fajr 18°, Isha 17°. Official Turkish method. */
    TURKEY(CalculationMethodParameters(fajrAngle = 18.0, ishaAngle = 17.0)),

    /** UK Islamic Mission / Unified UK convention — Fajr 12°, Isha 12°. */
    UK_UNIFIED(CalculationMethodParameters(fajrAngle = 12.0, ishaAngle = 12.0)),

    /** North Africa convention — Fajr 18°, Isha 12°. Used in Morocco, Algeria, and Tunisia. */
    NORTH_AFRICA(CalculationMethodParameters(fajrAngle = 18.0, ishaAngle = 12.0)),

    /** Russia/CIS convention — Fajr 16°, Isha 15°. */
    RUSSIA(CalculationMethodParameters(fajrAngle = 16.0, ishaAngle = 15.0)),

    /** Algeria — Fajr 18°, Isha 12°. */
    ALGERIA(CalculationMethodParameters(fajrAngle = 18.0, ishaAngle = 12.0)),

    /** Placeholder for user-defined angles. Supply a [CalculationMethodParameters] via `Tauqeet(customMethodParams = …)`. */
    CUSTOM(CalculationMethodParameters(fajrAngle = 18.0, ishaAngle = 18.0))
}

/**
 * Juristic school (madhab) that determines the Asr shadow-length multiplier.
 *
 * @property shadowFactor  The integer multiplier applied to object height to determine the
 *                         shadow length at which Asr begins. `1` = Shafi/Maliki/Hanbali/Jaafari;
 *                         `2` = Hanafi.
 */
enum class Madhab(val shadowFactor: Int) {
    /** Shafi'i school (shadow factor 1). Also applies to Maliki, Hanbali, and Jaafari rulings. */
    SHAFI(1),

    /** Hanafi school (shadow factor 2). Produces a later Asr time than the other schools. */
    HANAFI(2),

    /** Maliki school — same shadow factor as [SHAFI]. */
    MALIKI(1),

    /** Hanbali school — same shadow factor as [SHAFI]. */
    HANBALI(1),

    /** Jaafari school — same shadow factor as [SHAFI]. */
    JAAFARI(1)
}
