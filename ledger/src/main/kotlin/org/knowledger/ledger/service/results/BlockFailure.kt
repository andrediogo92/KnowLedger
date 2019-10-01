package org.knowledger.ledger.service.results

import org.knowledger.ledger.core.results.Failable
import org.knowledger.ledger.core.results.Failure


sealed class BlockFailure : Failure {
    class UnknownFailure(
        cause: String,
        exception: Exception?
    ) : BlockFailure() {
        override val failable: Failable.HardFailure =
            Failable.HardFailure(cause, exception)
    }

    class Propagated(
        pointOfFailure: String,
        failable: Failable
    ) : BlockFailure() {
        override val failable: Failable.PropagatedFailure =
            Failable.PropagatedFailure(pointOfFailure, failable)
    }

}