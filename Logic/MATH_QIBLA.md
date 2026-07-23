# Qibla Geometry (KMP)

Qibla direction is computed geodesically. `tauqeet-kmp` relies on exact spherical trigonometry mapping coordinates against the Kaaba.

$\tan(Q) = \frac{\sin(\lambda_k - \lambda)}{\cos(\phi)\tan(\phi_k) - \sin(\phi)\cos(\lambda_k - \lambda)}$

Where:
- $\phi_k$, $\lambda_k$ = Latitude and Longitude of Kaaba (Mecca).
- $\phi$, $\lambda$ = Observer coordinates.

Output bearings are strictly bounded into standard `[0.0, 360.0]` domains.
