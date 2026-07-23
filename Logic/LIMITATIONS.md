# Mathematical Limitations (KMP)

- Output timestamps are securely floored to `Long` milliseconds, which intrinsically limits picosecond fidelity for theoretical space-scale geometries but guarantees perfection for global civil timing constraints.
- Native implementations of `kotlin.math.sin` and `kotlin.math.cos` depend rigidly on the local C-frameworks (e.g. `glibc` vs Darwin Native bounds) resulting in microscopic sub-nanosecond ULP disparities across compiled iOS vs Android vs Node.js binaries.
