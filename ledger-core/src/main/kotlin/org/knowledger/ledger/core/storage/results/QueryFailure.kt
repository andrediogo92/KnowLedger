package org.knowledger.ledger.core.storage.results

import org.knowledger.ledger.core.results.Failable
import org.knowledger.ledger.core.results.HardFailure
import org.knowledger.ledger.core.results.PropagatedFailure


sealed class QueryFailure : Failable {
    data class NonExistentData(
        override val cause: String
    ) : QueryFailure()


    data class UnknownFailure(
        override val cause: String,
        override val exception: Exception? = null
    ) : QueryFailure(), HardFailure

    data class Propagated(
        override val pointOfFailure: String,
        override val failable: Failable
    ) : QueryFailure(), PropagatedFailure

}