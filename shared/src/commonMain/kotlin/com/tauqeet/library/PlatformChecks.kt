
package com.tauqeet.library

/**
 * Returns `true` if the current runtime is a Node.js process.
 *
 * ⚠️ **Not part of the stable public API.** Will be made `internal` in 0.3.0.
 * Do not depend on this from external code.
 */
expect fun isNode(): Boolean
/**
 * Returns `true` if the current runtime is a browser environment.
 *
 * ⚠️ **Not part of the stable public API.** Will be made `internal` in 0.3.0.
 * Do not depend on this from external code.
 */
expect fun isBrowser(): Boolean
