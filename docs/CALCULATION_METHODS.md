# 🗺️ Calculation Methods Reference

A complete reference for all `CalculationMethod` presets, `Madhab` options, and `HighLatitudeRule` strategies built into `tauqeet-kmp`.

---

## `CalculationMethod` Enum

Each method encodes the solar depression angles used by a specific Islamic authority to determine Fajr (dawn) and Isha (night) twilight. All methods also configure Maghrib behavior (angle-based, or fixed-minutes after sunset).

| Enum Value | Full Name | Fajr Angle | Isha Angle | Isha Interval | Maghrib Angle | Region |
| :--- | :--- | :---: | :---: | :---: | :---: | :--- |
| `MWL` | Muslim World League | 18.0° | 17.0° | — | Sunset | Global |
| `ISNA` | Islamic Society of North America | 15.0° | 15.0° | — | Sunset | North America |
| `EGYPT` | Egyptian General Authority of Survey | 19.5° | 17.5° | — | Sunset | Egypt, Middle East |
| `MAKKAH` | Umm al-Qura, Makkah | 18.5° | — | 90 min | Sunset | Arabian Peninsula |
| `KARACHI` | University of Islamic Sciences, Karachi | 18.0° | 18.0° | — | Sunset | Pakistan, Bangladesh, India |
| `TEHRAN` | Institute of Geophysics, Tehran | 17.7° | 14.0° | — | 4.5° | Iran |
| `JAFARI` | Shia Ithna-Ashari, Leva Institute, Qum | 16.0° | 14.0° | — | 4.0° | Shia communities |
| `GULF` | Gulf Region | 19.5° | — | 90 min | Sunset | UAE, Bahrain, Oman |
| `KUWAIT` | Kuwait | 18.0° | 17.5° | — | Sunset | Kuwait |
| `QATAR` | Qatar | 18.0° | — | 90 min | Sunset | Qatar |
| `SINGAPORE` | Majlis Ugama Islam Singapura | 20.0° | 18.0° | — | Sunset | Singapore, Malaysia |
| `FRANCE` | Union des organisations islamiques de France | 12.0° | 12.0° | — | Sunset | France |
| `TURKEY` | Diyanet İşleri Başkanlığı | 18.0° | 17.0° | — | Sunset | Turkey |
| `RUSSIA` | Spiritual Administration of Muslims of Russia | 16.0° | 15.0° | — | Sunset | Russia, Central Asia |
| `ALGERIA` | Algeria | 18.0° | 12.0° | — | Sunset | Algeria |
| `CUSTOM` | User-defined | custom | custom | custom | custom | Any |

### Understanding the Angles

- **Fajr Angle**: How far (in degrees) the sun must be *below* the horizon for Fajr to begin. A larger angle means an earlier Fajr.
- **Isha Angle**: How far the sun must be *below* the horizon for Isha to begin. A larger angle means a later Isha.
- **Isha Interval**: Some methods (Makkah, Gulf, Qatar) define Isha as a fixed number of minutes after Sunset, rather than using an angle. When `ishaInterval > 0`, the angle is ignored.
- **Maghrib Angle**: Some methods (Tehran, Jafari) define Maghrib as a depression angle after sunset, rather than exactly at sunset.

### Using `CUSTOM`

```kotlin
import com.tauqeet.library.Tauqeet
import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.CalculationMethodParameters

val myCustomMethod = CalculationMethodParameters(
    fajrAngle    = 15.5,  // degrees
    ishaAngle    = 15.0,  // degrees (ignored if ishaInterval > 0)
    ishaInterval = 0,     // minutes after sunset (0 = use ishaAngle)
    maghribAngle = 0.0,   // degrees (0 = use sunset)
    maghribInterval = 0   // minutes after sunset (0 = use maghribAngle)
)

val tauqeet = Tauqeet(
    method = CalculationMethod.CUSTOM,
    customMethodParams = myCustomMethod
)
```

---

## `Madhab` Enum

Controls the Asr prayer time, which is determined by the length of an object's shadow relative to its height. The difference between schools is the shadow *multiplier*.

| Enum Value | Shadow Factor | Notes |
| :--- | :---: | :--- |
| `SHAFI` | 1× | Standard. Also applies to Maliki and Hanbali. |
| `MALIKI` | 1× | Alias for Shafi (same shadow factor). |
| `HANBALI` | 1× | Alias for Shafi (same shadow factor). |
| `JAAFARI` | 1× | Alias for Shafi (same shadow factor). |
| `HANAFI` | 2× | Asr starts later; shadow must be twice the object's height. |

The shadow length at the time of Asr is: `shadow_length = shadow_at_noon + (factor × object_height)`.

---

## `HighLatitudeRule` Enum

Used when the sun does not dip far enough below the horizon to trigger Fajr and Isha angles. Three juristic estimation strategies are available:

| Enum Value | Description | Best For |
| :--- | :--- | :--- |
| `MIDDLE_OF_NIGHT` | Splits the interval from Sunset to next Sunrise into two halves. Fajr = last half, Isha = first half. | Most commonly recommended rule. |
| `SEVENTH_OF_NIGHT` | Divides the same interval into sevenths. Fajr starts 1/7th before Sunrise, Isha starts 1/7th after Sunset. | Conservative; produces tighter prayer windows. |
| `TWILIGHT_ANGLE` | Night is divided proportionally using the `fajrAngle` and `ishaAngle` of the chosen `CalculationMethod`. | Most mathematically aligned with the chosen method. |

### When is it triggered?

- **Continuous Twilight**: The sun sets but never reaches the 15°–20° depression needed for Fajr/Isha.
  - Example: London (51°N) in July.
- **Polar Day**: The sun never sets at all.
  - Example: Tromsø (69°N) in June.

In both cases, the library sets `isPolarDay = true` in `PrayerTimesMetadata` and applies the rule automatically.
