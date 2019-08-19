package org.knowledger.ledger.core.results

import org.tinylog.kotlin.Logger

fun HardFailure.logAndThrow(): Nothing {
    if (exception != null) {
        Logger.error(cause)
        throw RuntimeException(exception)
    } else {
        throw RuntimeException(cause)
    }
}

fun Failable.throwCause(): Nothing {
    throw RuntimeException(cause)
}