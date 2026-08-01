# ❓ Troubleshooting & FAQ

Common questions and issues when integrating `tauqeet-kmp`.

---

## Prayer Times

### "Fajr or Isha is `null` in my app"

**Cause:** You are in a high-latitude region (e.g. UK, Scandinavia, Canada) during summer, and the sun never dips to the required depression angle. This is called **Continuous Twilight**.

**Fix:** The engine auto-estimates using `highLatitudeRule`. Always use safe-calls:
```kotlin
val fajrText = times.fajr?.toTimeStringShort() ?: "--:--"
```
Check metadata to inform the user:
```kotlin
if (times.metadata?.isPolarDay == true) {
    showBanner("Estimated times — sun stays near the horizon today.")
}
```

---

### "All times are `null`"

**Cause:** You are in **Polar Night** (e.g. northern Norway in December). The sun does not rise at all. `isPolarNight` will be `true`.

**Fix:** Detect this flag and fall back to a reference city (e.g. nearest city or Mecca times):
```kotlin
if (times.metadata?.isPolarNight == true) {
    loadFallbackTimesForNearestCity()
}
```

---

### "Fajr and Isha look too early/late compared to another app"

**Cause:** Different apps may use a different `CalculationMethod`. For example:
- `ISNA` uses 15° for both Fajr and Isha.
- `MWL` uses 18° for Fajr, which gives an earlier Fajr.
- `KARACHI` uses 18° for both.

**Fix:** Match your `method` to the convention used in your region. See [CALCULATION_METHODS.md](CALCULATION_METHODS.md) for a full comparison table.

---

### "Asr is much later than I expected"

**Cause:** You are using `Madhab.HANAFI`, which uses shadow factor 2×. Hanafi Asr consistently occurs 30–90 minutes later than Shafi/Maliki/Hanbali.

**Fix:** Confirm your madhab setting:
```kotlin
val tauqeet = Tauqeet(madhab = Madhab.SHAFI) // or HANAFI
```

---

### "Dhuhr seems off by a few minutes"

**Cause:** This is usually correct. Dhuhr is solar noon (when the sun crosses the meridian), which is rarely exactly 12:00. The equation of time causes it to vary by ±16 minutes over the year.

---

### "Times are correct but shown in UTC instead of local time"

**Cause:** You did not pass `timezoneOffset`.

**Fix:** Pass the correct offset in decimal hours:
```kotlin
tauqeet.computePrayerTimes(year, month, day, lat, lng, timezoneOffset = 5.0)  // UTC+5
tauqeet.computePrayerTimes(year, month, day, lat, lng, timezoneOffset = -4.0) // UTC-4
tauqeet.computePrayerTimes(year, month, day, lat, lng, timezoneOffset = 5.5)  // UTC+5:30 (India)
tauqeet.computePrayerTimes(year, month, day, lat, lng, timezoneOffset = 5.75) // UTC+5:45 (Nepal)
```

---

## Qibla

### "`qiblaDirection()` returned `null`"

**Cause:** Your coordinates are extremely close to the Kaaba (within ~1 meter). Every direction from inside the Kaaba points toward the Kaaba, so the concept of a single bearing is undefined.

**Fix:** This is expected behavior. Display a special message:
```kotlin
val qibla = tauqeet.qiblaDirection(lat, lng)
    ?: return println("You are at the Kaaba. All directions are Qibla.")
```

---

### "Qibla bearing looks very different from other apps"

**Cause 1:** Other apps may use the simple Haversine/Spherical formula. `tauqeet-kmp` uses the more accurate WGS-84 Vincenty formula, which can differ by a fraction of a degree for long distances.

**Cause 2:** Are you comparing True North bearing vs Magnetic North bearing? `tauqeet-kmp` always returns **True North**. You must apply magnetic declination to align with a hardware compass.

---

## Build & Integration

### "Unresolved reference: `toTimeString`"

**Fix:** Import the extension function explicitly:
```kotlin
import com.tauqeet.library.toTimeString
import com.tauqeet.library.toTimeStringShort
import com.tauqeet.library.toISOTimeStringWithMillis
```

### "Dependency not found on Maven Central"

**Fix:** Ensure you have `mavenCentral()` in your repositories block and are using the correct group ID:
```kotlin
repositories { mavenCentral() }
dependencies { implementation("io.github.h-ghulam-hasnain:tauqeet-kmp:0.1.0") }
```
