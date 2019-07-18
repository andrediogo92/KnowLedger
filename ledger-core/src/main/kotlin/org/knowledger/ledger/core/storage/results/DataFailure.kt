package org.knowledger.ledger.core.storage.results

import org.knowledger.ledger.core.results.Failable
import org.knowledger.ledger.core.results.HardFailure
import org.knowledger.ledger.core.results.PropagatedFailure


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