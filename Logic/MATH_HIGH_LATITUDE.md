# High Latitude Geometry (KMP)

At locations far from the equator (above ~48° latitude), the Sun may never reach the standard twilight depression angles (e.g., -18° for Fajr/Isha). During summer months, the sun remains above the horizon at midnight (Midnight Sun).

## Bounding Operations
To avoid destructive `NaN` propagations when resolving ratios of polar days, `tauqeet-kmp` aggressively bounds divisors. `nightDuration` is strictly clamped at a minimum of `0.001` to prevent mathematical division by zero faults right exactly on the theoretical geographic tipping points of polar day cycles.

## Resolution Rules

1. **Middle of the Night:**
   Splits the period between Sunset and Sunrise evenly in half, assigning maximum limits to Isha and Fajr constraints.

2. **Seventh of the Night:**
   Splits the night period into 7 portions, allocating the first 1/7th limit to Isha, and the last 1/7th limit to Fajr constraints.

3. **Twilight Angle Ratio:**
   Scales the missing twilight parametrically based on the respective missing angle requirement (e.g., $18 / 60$).
