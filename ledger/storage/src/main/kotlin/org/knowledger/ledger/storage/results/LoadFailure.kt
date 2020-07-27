package org.knowledger.ledger.storage.results

import org.knowledger.ledger.core.data.LedgerContract
import org.knowledger.ledger.results.Failable
import org.knowledger.ledger.results.Failure

/**
 * Result class representing loading of [LedgerContract] classes
 * from the database.
 */
sealed class LoadFailure : Failure {
    class NonExistentData(
        cause: String
    ) : LoadFailure() {
        override val failable: Failable.LightFailure =
            Failable.LightFailure(
                cause
            )
    }

    class UnrecognizedDataType(
        cause: String
    ) : LoadFailure() {
        override val failable: Failable.LightFailure =
            Failable.LightFailure(
                cause
            )
    }

    class UnknownFailure(
        cause: String,
        exception: Exception?
    ) : LoadFailure() {
        override val failable: Failable.HardFailure =
            Failable.HardFailure(cause, exception)
    }

    class Propagated(
        pointOfFailure: String,
        failable: Failable
    ) : LoadFailure() {
        override val failable: Failable.PropagatedFailure =
            Failable.PropagatedFailure(pointOfFailure, failable)
    }
}