# 🏗️ Architecture & Design Philosophy

`tauqeet-kmp` is designed from the ground up for Kotlin Multiplatform (KMP), making it an ideal choice for modern Android, iOS, Desktop (JVM/Native), and Web (JS/Wasm) applications. 

## Goals

1. **Zero-Allocation Core**: The core mathematical and astronomical algorithms are designed to minimize object allocations during calculations. This is crucial for high-performance applications (such as AR applications displaying Qibla directions 60 times a second).
2. **Mathematical Parity**: The library guarantees 1:1 mathematical parity with the original `tauqeet-js` TypeScript implementation, bringing the same rigorous astronomical standards to the Kotlin ecosystem.
3. **Graceful Fallbacks**: The library is built to avoid crashes in extreme edge cases (like standing exactly at the Kaaba or at its antipode, or computing prayer times inside the Arctic Circle).
4. **Unified Public API**: The public surface now prefers a `PrayerRequest` DSL that packages date, location, timezone, and calculation configuration into a single request object while keeping the old flat overloads as deprecated compatibility shims.
5. **Solver Metadata Transparency**: The final result now exposes `resolutionInfo` and `flags` so callers can understand whether the engine resolved a request as `NORMAL`, `HIGH_LATITUDE`, `POLAR_DAY`, or `POLAR_NIGHT`.

## Project Structure

The project is structured as a standard KMP library:
- **`shared`**: Contains all common multiplatform code.
    - `astronomy`: Houses the core VSOP87 engine, Julian Day conversions, and solar coordinate mechanics.
    - `prayers`: Implements the request DSL (`PrayerRequest` / `PrayerCalculationParameters`), solver decomposition (`IterativeSolver`, `SunriseSunsetSolver`, `AsrSolver`, `HighLatitudeResolver`), high-latitude adjustments, atmospheric refraction models, and madhab logic.
    - `qibla`: Contains the geodetic WGS-84 Vincenty Inverse and Haversine distance utilities.
    - `time`: Time manipulation and conversion utilities.
    - `internal`: Internal helpers not exposed in the public API.

## The VSOP87 Engine

Instead of relying on simplified algorithmic approximations for solar position, `tauqeet-kmp` integrates a robust VSOP87 (Variations Séculaires des Orbites Planétaires) engine. This ensures that solar declination and equation of time variables remain incredibly precise over centuries, a requirement for scientifically accurate prayer time calculations.
