package org.knowledger.ledger.storage.results

import org.knowledger.ledger.database.results.QueryFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.tryOrConvertToFailure
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

//-----------------------------------------
// Exception Handlers
//-----------------------------------------
inline fun <T> tryOrLedgerUnknownFailure(
    function: () -> Outcome<T, LedgerFailure>
): Outcome<T, LedgerFailure> =
    tryOrConvertToFailure(function) { exception ->
        LedgerFailure.UnknownFailure(exception.message ?: "", exception)
    }


inline fun <T> tryOrLoadUnknownFailure(
    function: () -> Outcome<T, LoadFailure>
): Outcome<T, LoadFailure> =
    tryOrConvertToFailure(function) { exception ->
        LoadFailure.UnknownFailure(exception.message ?: "", exception)
    }

inline fun <T> tryOrUpdateUnknownFailure(
    function: () -> Outcome<T, UpdateFailure>
): Outcome<T, UpdateFailure> =
    tryOrConvertToFailure(function) { exception ->
        UpdateFailure.UnknownFailure(exception.message ?: "", exception)
    }


inline fun <T> tryOrQueryUnknownFailure(
    function: () -> Outcome<T, QueryFailure>
): Outcome<T, QueryFailure> =
    tryOrConvertToFailure(function) { exception ->
        QueryFailure.UnknownFailure(exception.message ?: "", exception)
    }

/**
 * Executes the given [block] function on this resource and returns
 * its result and then closes it down correctly whether an exception
 * is thrown or not.
 */
@Suppress("ConvertTryFinallyToUseCall")
inline fun <R : AutoCloseable, T> R.use(block: R.() -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return try {
        block()
    } finally {
        close()
    }
}


fun deadCode(): Nothing {
    throw RuntimeException("Dead code invoked")
}

@Suppress("unused")
fun <T> T.checkSealed() {
}
