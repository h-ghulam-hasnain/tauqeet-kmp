# Mathematical Engine: Prayer Times Calculation (KMP)

The `tauqeet-kmp` engine calculates Islamic prayer times by executing exact spherical astronomical formulas combined with iterative root-finding techniques over the **VSOP87D** solar ephemeris model.

## 1. Hour Angle Resolution

Prayer times like Fajr, Sunrise, Maghrib, and Isha occur when the sun reaches specific geometric depressions below the horizon. The engine solves the classic Hour Angle equation:

$\cos(H) = \frac{\sin(h) - \sin(\phi)\sin(\delta)}{\cos(\phi)\cos(\delta)}$

Where:
- $H$ = Hour Angle
- $h$ = Target altitude / zenith
- $\phi$ = Observer's Latitude
- $\delta$ = Solar Declination

## 2. Iterative Convergence Solver

Because the Sun's declination ($\delta$) and the Equation of Time shift throughout the day, solving the Hour Angle dynamically requires an iterative numerical method rather than static assumptions:
1. The solver seeds an initial guess using the Julian Day at noon.
2. It projects a time estimate.
3. It recursively evaluates the Ephemeris for that exact micro-second estimate.
4. It converges to an exact coordinate within $< 10^{-5}$ fractional hours.

## 3. Dhahwa Kubra (Islamic Midday)

Dhahwa Kubra marks the absolute midday in Islamic jurisprudence, marking the boundary for certain fasting intentions.
It is explicitly computed mathematically as the midpoint of the visible twilight spectrum:
- $\text{Dhahwa Kubra} = (\text{Fajr Time} + \text{Sunset Time}) / 2$
