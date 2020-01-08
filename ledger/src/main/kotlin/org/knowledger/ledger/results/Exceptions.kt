package org.knowledger.ledger.results

import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.database.results.QueryFailure
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.handles.builder.LedgerConfig
import org.knowledger.ledger.service.results.LedgerFailure
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.service.results.UpdateFailure

//-----------------------------------------
// Exception Handlers
//-----------------------------------------

internal inline fun tryOrHandleUnknownFailure(
    run: () -> Outcome<LedgerConfig, LedgerHandle.Failure>
): Outcome<LedgerConfig, LedgerHandle.Failure> =
    try {
        run()
    } catch (e: Exception) {
        Outcome.Error(
            LedgerHandle.Failure.UnknownFailure(
                e.message ?: "", e
            )
        )
    }

inline fun <T> tryOrLedgerUnknownFailure(
    run: () -> Outcome<T, LedgerFailure>
): Outcome<T, LedgerFailure> =
    try {
        run()
    } catch (e: Exception) {
        Outcome.Error(
            LedgerFailure.UnknownFailure(
                e.message ?: "", e
            )
        )
    }


inline fun <T> tryOrLoadUnknownFailure(
    run: () -> Outcome<T, LoadFailure>
): Outcome<T, LoadFailure> =
    try {
        run()
    } catch (e: Exception) {
        Outcome.Error(
            LoadFailure.UnknownFailure(
                e.message ?: "", e
            )
        )
    }

inline fun <T> tryOrDataUnknownFailure(
    run: () -> Outcome<T, DataFailure>
): Outcome<T, DataFailure> =
    try {
        run()
    } catch (e: Exception) {
        Outcome.Error(
            DataFailure.UnknownFailure(
                e.message ?: "", e
            )
        )
    }

inline fun <T> tryOrUpdateUnknownFailure(
    run: () -> Outcome<T, UpdateFailure>
): Outcome<T, UpdateFailure> =
    try {
        run()
    } catch (e: Exception) {
        Outcome.Error(
            UpdateFailure.UnknownFailure(
                e.message ?: "", e
            )
        )
    }


inline fun <T> tryOrQueryUnknownFailure(
    run: () -> Outcome<T, QueryFailure>
): Outcome<T, QueryFailure> =
    try {
        run()
    } catch (e: Exception) {
        Outcome.Error(
            QueryFailure.UnknownFailure(
                e.message ?: "", e
            )
        )
    }

/**
 * Executes the given [block] function on this resource and returns
 * its result and then closes it down correctly whether an exception
 * is thrown or not.
 */
@Suppress("ConvertTryFinallyToUseCall")
inline fun <R : AutoCloseable, T> R.use(block: R.() -> T): T =
    try {
        this.block()
    } finally {
        this.close()
    }


fun deadCode(): Nothing {
    throw RuntimeException("Dead code invoked")
}

@Suppress("unused")
fun <T> T.checkSealed() {}
