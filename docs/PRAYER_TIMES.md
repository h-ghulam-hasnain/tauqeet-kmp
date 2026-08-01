# 🕌 Prayer Times Calculation Engine

The `tauqeet-kmp` prayer times engine is built upon the highly accurate **VSOP87** theory for solar position and mechanics. It supports a wide array of globally recognized calculation methods and includes robust handling for extreme geographical locations.

---

## 1. Input Parameters & Data Types

The library separates configuration (instantiation) from computation (execution). This allows you to create a `Tauqeet` instance once for a user's settings and reuse it daily.

### A. Engine Configuration (`Tauqeet` constructor)
All parameters here are **OPTIONAL** with sensible defaults.

| Parameter | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `method` | `CalculationMethod` | `KARACHI` | The convention used to calculate Fajr and Isha angles. |
| `madhab` | `Madhab` | `HANAFI` | Juristic method for Asr shadow length. |
| `highLatitudeRule` | `HighLatitudeRule`| `MIDDLE_OF_NIGHT` | Fallback strategy for extreme northern/southern regions. |
| `elevationMeters` | `Double` | `0.0` | Altitude above sea level in meters. Affects atmospheric dip. |
| `temperatureC` | `Double` | `12.714` | Ambient temperature in Celsius. Affects refraction. |
| `pressureMbar` | `Double` | `1010.0` | Atmospheric pressure in millibars. Affects refraction. |
| `customMethodParams` | `CalculationMethodParameters?` | `null` | Provide custom angles if overriding a preset `method`. |

### B. Computation Inputs (`PrayerRequest` / unified API)
The preferred entry point is `computePrayerTimes(request: PrayerRequest)`. The older flat overloads still exist for compatibility, but they are now deprecated.

| Input | Type | Required? | Default | Description |
| :--- | :--- | :--- | :--- | :--- |
| `latitude` | `Double` | **Yes** | - | Observer's latitude (-90.0 to 90.0). |
| `longitude` | `Double` | **Yes** | - | Observer's longitude (-180.0 to 180.0). |
| `date` | `DateComponents` | **Yes** | - | Gregorian date to calculate for. |
| `timeZoneOffset` | `Double` | No | `0.0` | Hours from UTC (e.g., `5.0` for UTC+5). |
| `includeAdvancedMetadata` | `Boolean` | No | `false` | Set to true to receive granular VSOP87 metadata. |
| `calculationParameters` | `PrayerCalculationParameters` | No | defaults from `Tauqeet` | Encapsulates `method`, `madhab`, `highLatitudeRule`, environmental values, and custom method overrides. |

---

## 2. Handling the Inputs Deep Dive

### 📍 Geography & Time
1. **Latitude (`lat`) & Longitude (`lng`)**: You should pull these from the device's GPS provider (e.g. FusedLocationProvider on Android, CoreLocation on iOS). They dictate the exact geometric relationship between the observer and the Sun.
2. **Timezone (`timezoneOffset`)**: Crucial for converting the internal UTC times to local time. For example, New York during DST is `-4.0`. You can pass decimal hours (e.g., `5.5` for India UTC+5:30) or `5.75` (Nepal UTC+5:45).

### ☁️ Environmental Factors
If you are building a highly accurate application (or avionic software):
3. **`elevationMeters`**: An observer on a mountain sees the sun rise earlier and set later than someone at sea level. Provide altitude in meters to dynamically calculate the atmospheric "dip".
4. **`temperatureC` & `pressureMbar`**: Cold air is denser than warm air, refracting sunlight differently. Supplying real-time weather data slightly shifts Fajr, Sunrise, Sunset, and Isha by adjusting the refraction coefficient.

### 📜 Religious Conventions
5. **`method` (Calculation Method)**: Different authorities specify different solar depression angles for twilight. For example, `ISNA` uses 15° for both Fajr and Isha, while `MWL` uses 18° and 17°.
6. **`madhab`**: Controls Asr time. 
   - `SHAFI`, `MALIKI`, `HANBALI`, `JAAFARI` (Standard): Asr starts when a shadow length equals the object's length + noon shadow.
   - `HANAFI`: Asr starts when a shadow length equals **twice** the object's length + noon shadow.

---

## 3. Possible Outcomes & Geographic Edge Cases

Because the earth is tilted on its axis, solar behavior changes drastically at high latitudes (e.g., Canada, Norway, Russia). `tauqeet-kmp` mathematically guarantees a robust output by adapting its behavior based on the following geographic realities:

### Scenario 1: Normal Days (Equatorial & Mid-Latitudes)
- **Description**: The sun rises, reaches its peak, sets, and twilight disappears completely before midnight.
- **Output**: The solver successfully finds all exact astronomical moments. 
- **Metadata Flag**: `isPolarDay = false`, `isPolarNight = false`.
- **Result**: `fajr`, `sunrise`, `dhuhr`, `asr`, `maghrib`, and `isha` are all returned as valid `Long` millisecond timestamps.

### Scenario 2: Continuous Twilight (Sub-Polar Regions)
- **Description**: In summer at sub-polar latitudes (e.g., London, UK), the sun sets, but it never dips low enough below the horizon (e.g., 18°) for true astronomical twilight to end. It stays somewhat bright all night.
- **Output**: The standard astronomical solver cannot find Fajr or Isha because those angles are never reached.
- **Fallback Triggered**: The engine automatically detects this and applies your `highLatitudeRule` (e.g., splitting the night).
- **Result**: `fajr` and `isha` are gracefully estimated and returned as `Long` timestamps without throwing errors. The `astronomicalMetadata.fajr.status` will be marked as `CONTINUOUS_TWILIGHT`.

### Scenario 3: Polar Day (The Midnight Sun)
- **Description**: Inside the Arctic/Antarctic circles during summer, the sun never sets. It stays above the horizon 24/7.
- **Output**: Astronomical Sunrise, Sunset, Maghrib, Isha, and Fajr literally do not occur. 
- **Fallback Triggered**: `isPolarDay = true` is flagged in the metadata.
- **Result**: Depending heavily on juristic interpretations, the `highLatitudeRule` is used to estimate Fajr and Isha relative to a mathematically assumed night. However, Sunrise and Sunset might remain `null` or be heavily estimated. You **must** check `isPolarDay` and safely handle potential nulls for `sunrise` and `maghrib` in your UI.

### Scenario 4: Polar Night
- **Description**: Inside the Arctic/Antarctic circles during winter, the sun never rises. It is dark 24/7.
- **Output**: Dhuhr, Asr, Sunrise, and Sunset do not occur astronomically.
- **Fallback Triggered**: `isPolarNight = true` is flagged in the metadata.
- **Result**: The solver returns `null` for times reliant on the sun being above the horizon. You must use regional fatwas or fallback to Mecca/nearest-city times on the client side when `isPolarNight == true`.

---

## 4. The High Latitude Rules

When the engine detects Continuous Twilight or Polar Day, it uses the `highLatitudeRule` to estimate Fajr and Isha:
- `MIDDLE_OF_NIGHT`: The period from Sunset to Sunrise is divided in half. The first half is considered the "night" for Isha, the second half for Fajr.
- `SEVENTH_OF_NIGHT`: The period from Sunset to Sunrise is divided into sevenths. Fajr begins 1/7th before Sunrise, and Isha begins 1/7th after Sunset.
- `TWILIGHT_ANGLE`: The night is divided proportionally based on the twilight angle chosen in the `CalculationMethod`.
