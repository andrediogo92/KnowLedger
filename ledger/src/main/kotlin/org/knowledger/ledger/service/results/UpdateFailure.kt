package org.knowledger.ledger.service.results

import org.knowledger.ledger.core.results.Failable
import org.knowledger.ledger.core.results.HardFailure
import org.knowledger.ledger.core.results.PropagatedFailure


sealed class UpdateFailure : Failable {
    object NotYetStored : UpdateFailure() {
        override val cause: String =
            "Element has not yet been persisted first."
    }

    data class FailedToSave(
        override val cause: String
    ) : UpdateFailure()

    data class UnknownFailure(
        override val cause: String,
        override val exception: Exception? = null
    ) : UpdateFailure(), HardFailure

    data class Propagated(
        override val pointOfFailure: String,
        override val failable: Failable
    ) : UpdateFailure(), PropagatedFailure
}