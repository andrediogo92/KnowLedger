package org.knowledger.ledger.storage.results

import org.knowledger.ledger.results.Failable
import org.knowledger.ledger.results.Failure


sealed class UpdateFailure : Failure {
    object NotYetStored : UpdateFailure() {
        override val failable: Failable.LightFailure =
            Failable.LightFailure("Element has not yet been persisted first.")
    }

    class FailedToSave(cause: String) : UpdateFailure() {
        override val failable: Failable.LightFailure =
            Failable.LightFailure(cause)
    }

    class UnknownFailure(cause: String, exception: Exception?) : UpdateFailure() {
        override val failable: Failable.HardFailure =
            Failable.HardFailure(cause, exception)
    }

    class Propagated(pointOfFailure: String, failable: Failable) : UpdateFailure() {
        override val failable: Failable.PropagatedFailure =
            Failable.PropagatedFailure(pointOfFailure, failable)
    }
}