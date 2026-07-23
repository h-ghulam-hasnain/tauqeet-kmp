# Performance Audit (KMP)

The `tauqeet-kmp` library underwent extreme profiling audits to support 60Hz and 120Hz smooth UI constraints on low-memory edge platforms (Mobile Android/iOS natively).

## Key Discoveries
- **Zero-Allocation**: The library bypasses all `Double?` Kotlin autoboxing primitives. Utilizing raw `Double.NaN` initialization, thousands of transient garbage objects were eliminated from the VSOP87 loop.
- **Float Drift Safety**: Returning results via strictly explicit `Long` format representing precise milliseconds prevents upstream rounding errors completely, sidestepping fractional string-serialization ULP degradation.
