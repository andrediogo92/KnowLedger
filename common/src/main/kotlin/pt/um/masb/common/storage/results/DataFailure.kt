package pt.um.masb.common.storage.results

import pt.um.masb.common.results.Failable
import pt.um.masb.common.results.HardFailure
import pt.um.masb.common.results.PropagatedFailure


sealed class DataFailure : Failable {
    data class NonExistentData(
        override val cause: String
    ) : DataFailure()

    data class UnrecognizedDataType(
        override val cause: String
    ) : DataFailure()

    data class UnrecognizedUnit(
        override val cause: String
    ) : DataFailure()

    data class UnexpectedClass(
        override val cause: String
    ) : DataFailure()

    data class NonRegisteredSchema(
        override val cause: String
    ) : DataFailure()

    data class UnknownFailure(
        override val cause: String,
        override val exception: Exception? = null
    ) : DataFailure(), HardFailure

    data class Propagated(
        override val pointOfFailure: String,
        override val failable: Failable
    ) : DataFailure(), PropagatedFailure
}