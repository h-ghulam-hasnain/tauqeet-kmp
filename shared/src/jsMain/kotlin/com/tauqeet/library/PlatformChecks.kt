@file:JsExport
package com.tauqeet.library

import kotlin.js.*

actual fun isNode(): Boolean = js("typeof process !== 'undefined' && process.versions != null && process.versions.node != null") as Boolean

actual fun isBrowser(): Boolean = js("typeof window !== 'undefined' && typeof document !== 'undefined'") as Boolean
