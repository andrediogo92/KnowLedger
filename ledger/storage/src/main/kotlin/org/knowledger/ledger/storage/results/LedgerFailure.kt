package org.knowledger.ledger.storage.results

import org.knowledger.ledger.results.Failable
import org.knowledger.ledger.results.Failure


sealed class LedgerFailure : Failure {

    class NonExistentData(pointOfFailure: String) : LedgerFailure() {
        override val failable: Failable.LightFailure =
            Failable.LightFailure("Empty result set for $pointOfFailure")
    }

    class NoKnownStorageAdapter(cause: String) : LedgerFailure() {
        override val failable: Failable.LightFailure =
            Failable.LightFailure(cause)
    }


    class UnknownFailure(cause: String, exception: Exception?) : LedgerFailure() {
        override val failable: Failable.HardFailure =
            Failable.HardFailure(cause, exception)
    }

    class Propagated(pointOfFailure: String, failable: Failable) : LedgerFailure() {
        override val failable: Failable.PropagatedFailure =
            Failable.PropagatedFailure(pointOfFailure, failable)
    }
}
