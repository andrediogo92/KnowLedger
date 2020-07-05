package org.knowledger.ledger.results

import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.database.results.QueryFailure
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.handles.builder.LedgerConfig
import org.knowledger.ledger.service.results.LedgerFailure
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.service.results.UpdateFailure
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

//-----------------------------------------
// Exception Handlers
//-----------------------------------------

inline fun <T, R : Failure> tryOrUnknownFailure(
    function: () -> Outcome<T, R>,
    failureConstructor: (Exception) -> R
): Outcome<T, R> {
    contract {
        callsInPlace(function, InvocationKind.EXACTLY_ONCE)
        callsInPlace(failureConstructor, InvocationKind.AT_MOST_ONCE)
    }
    return try {
        function()
    } catch (e: Exception) {
        Outcome.Error(
            failureConstructor(e)
        )
    }
}

internal inline fun tryOrHandleUnknownFailure(
    function: () -> Outcome<LedgerConfig, LedgerHandle.Failure>
): Outcome<LedgerConfig, LedgerHandle.Failure> =
    tryOrUnknownFailure(function) { exception ->
        LedgerHandle.Failure.UnknownFailure(
            exception.message ?: "", exception
        )
    }

inline fun <T> tryOrLedgerUnknownFailure(
    function: () -> Outcome<T, LedgerFailure>
): Outcome<T, LedgerFailure> =
    tryOrUnknownFailure(function) { exception ->
        LedgerFailure.UnknownFailure(
            exception.message ?: "", exception
        )
    }


inline fun <T> tryOrLoadUnknownFailure(
    function: () -> Outcome<T, LoadFailure>
): Outcome<T, LoadFailure> =
    tryOrUnknownFailure(function) { exception ->
        LoadFailure.UnknownFailure(
            exception.message ?: "", exception
        )
    }

inline fun <T> tryOrDataUnknownFailure(
    function: () -> Outcome<T, DataFailure>
): Outcome<T, DataFailure> =
    tryOrUnknownFailure(function) { exception ->
        DataFailure.UnknownFailure(
            exception.message ?: "", exception
        )
    }

inline fun <T> tryOrUpdateUnknownFailure(
    function: () -> Outcome<T, UpdateFailure>
): Outcome<T, UpdateFailure> =
    tryOrUnknownFailure(function) { exception ->
        UpdateFailure.UnknownFailure(
            exception.message ?: "", exception
        )
    }


inline fun <T> tryOrQueryUnknownFailure(
    function: () -> Outcome<T, QueryFailure>
): Outcome<T, QueryFailure> =
    tryOrUnknownFailure(function) { exception ->
        QueryFailure.UnknownFailure(
            exception.message ?: "", exception
        )
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
        this.block()
    } finally {
        this.close()
    }
}


fun deadCode(): Nothing {
    throw RuntimeException("Dead code invoked")
}

@Suppress("unused")
fun <T> T.checkSealed() {}
