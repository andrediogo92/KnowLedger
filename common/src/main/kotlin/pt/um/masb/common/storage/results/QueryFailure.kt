package pt.um.masb.common.storage.results

import pt.um.masb.common.results.Failable
import pt.um.masb.common.results.HardFailure
import pt.um.masb.common.results.PropagatedFailure


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