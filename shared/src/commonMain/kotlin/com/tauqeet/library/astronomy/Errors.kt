package com.tauqeet.library.astronomy

open class SearchConvergenceError(message: String) : Exception(message)

open class InvalidArgumentError(message: String) : IllegalArgumentException(message)

open class OperationAbortedError(message: String = "Operation was aborted") : Exception(message)
