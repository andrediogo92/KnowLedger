package org.knowledger.ledger.database.results

import org.knowledger.ledger.storage.results.Failable
import org.knowledger.ledger.storage.results.Failure


sealed class QueryFailure : Failure {
    class NonExistentData(
        cause: String
    ) : QueryFailure() {
        override val failable: Failable.LightFailure =
            Failable.LightFailure(
                cause
            )
    }


    class UnknownFailure(
        cause: String,
        exception: Exception?
    ) : QueryFailure() {
        override val failable: Failable.HardFailure =
            Failable.HardFailure(cause, exception)
    }

    class Propagated(
        pointOfFailure: String,
        failable: Failable
    ) : QueryFailure() {
        override val failable: Failable.PropagatedFailure =
            Failable.PropagatedFailure(pointOfFailure, failable)
    }

}