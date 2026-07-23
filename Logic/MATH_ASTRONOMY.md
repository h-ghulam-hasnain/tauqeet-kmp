# Astronomical Logic & Solar Ephemeris (KMP)

The `tauqeet-kmp` engine relies on an ultra-precise native Kotlin multiplatform solar tracking model. 

---

## 1. Solar Ephemeris (VSOP87)

The core positional engine relies on a streamlined implementation of the **VSOP87D** planetary theory. 

### Implementation Details
- The engine computes the heliocentric longitude, latitude, and radius vector of the Earth natively.
- To reduce memory allocations (GC pressure) on low-end mobile devices, the KMP engine entirely eliminates `Double?` boxed caching, opting for native unboxed `Double` properties initialized to `Double.NaN`. 
- Kahan Summation is natively executed in unrolled Kotlin loops to maintain strict 64-bit precision without losing floating-point mantissas over thousands of periodic terms.

---

## 2. Atmospheric Refraction (Bennett's Formula)

A core upgrade in KMP is the native mathematical implementation of **Bennett's Refraction Formula (1982)**.
Rather than assuming a rigid generic geometric offset (e.g., exactly `0.833°`), the library dynamically factors atmospheric bending given the Sun's real-time true altitude:
- **Refraction:** $R = \cot\left(h + \frac{7.31}{h + 4.4}\right)$

## 3. Dynamic Semidiameter & Parallax

The exact angular diameter of the Sun viewed from Earth fluctuates between 15.8 and 16.3 arcminutes. `tauqeet-kmp` dynamically pulls the Earth's radius vector (`R`) from the VSOP87 model to assign the exact semidiameter for Sunrise/Sunset bounds rather than hardcoding static angular radii.
