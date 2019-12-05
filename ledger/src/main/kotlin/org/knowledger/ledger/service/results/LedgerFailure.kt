package org.knowledger.ledger.service.results

import org.knowledger.ledger.core.results.Failable
import org.knowledger.ledger.core.results.Failure
import org.knowledger.ledger.crypto.hash.Hash


sealed class LedgerFailure : Failure {

    class NonExistentData(
        pointOfFailure: String
    ) : LedgerFailure() {
        override val failable: Failable.LightFailure =
            Failable.LightFailure(
                "Empty result set for $pointOfFailure"
            )
    }

    data class NonMatchingHasher(
        val ledgerHash: Hash
    ) : LedgerFailure() {
        override val failable: Failable.LightFailure =
            Failable.LightFailure(
                "No matching hasher present for $ledgerHash"
            )
    }

    class NoKnownStorageAdapter(
        cause: String
    ) : LedgerFailure() {
        override val failable: Failable.LightFailure =
            Failable.LightFailure(
                cause
            )
    }


    class UnknownFailure(
        cause: String,
        exception: Exception?
    ) : LedgerFailure() {
        override val failable: Failable.HardFailure =
            Failable.HardFailure(cause, exception)
    }

    class Propagated(
        pointOfFailure: String,
        failable: Failable
    ) : LedgerFailure() {
        override val failable: Failable.PropagatedFailure =
            Failable.PropagatedFailure(pointOfFailure, failable)
    }
}
