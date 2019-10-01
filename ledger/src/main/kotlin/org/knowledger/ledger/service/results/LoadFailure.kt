package org.knowledger.ledger.service.results

import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Failable
import org.knowledger.ledger.core.results.Failure
import org.knowledger.ledger.core.storage.LedgerContract

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

    class NonMatchingCrypter(
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

    class NoMatchingContainer(
        ledgerHash: Hash
    ) : LoadFailure() {
        override val failable: Failable.LightFailure =
            Failable.LightFailure(
                "No container present for $ledgerHash"
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