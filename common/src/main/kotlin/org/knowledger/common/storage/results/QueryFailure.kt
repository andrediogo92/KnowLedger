package org.knowledger.common.storage.results

import org.knowledger.common.results.Failable
import org.knowledger.common.results.HardFailure
import org.knowledger.common.results.PropagatedFailure


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