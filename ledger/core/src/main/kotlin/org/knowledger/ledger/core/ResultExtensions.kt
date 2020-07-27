package org.knowledger.ledger.core

import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.tryOrConvertToFailure

inline fun <T> tryOrDataUnknownFailure(
    function: () -> Outcome<T, DataFailure>
): Outcome<T, DataFailure> =
    tryOrConvertToFailure(function) { exception ->
        DataFailure.UnknownFailure(
            exception.message ?: "", exception
        )
    }