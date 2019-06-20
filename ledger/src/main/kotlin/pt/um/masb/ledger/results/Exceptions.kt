package pt.um.masb.ledger.results

import pt.um.masb.common.results.Outcome
import pt.um.masb.common.storage.results.DataFailure
import pt.um.masb.common.storage.results.QueryFailure
import pt.um.masb.ledger.service.results.LedgerFailure
import pt.um.masb.ledger.service.results.LoadFailure

//-----------------------------------------
// Exception Handlers
//-----------------------------------------


inline fun <T : Any> tryOrLedgerUnknownFailure(
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


inline fun <T : Any> tryOrLoadUnknownFailure(
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

inline fun <T : Any> tryOrDataUnknownFailure(
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

inline fun <T : Any> tryOrQueryUnknownFailure(
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


fun deadCode(): Nothing {
    throw RuntimeException("Dead code invoked")
}

fun <T> T.checkSealed() {}