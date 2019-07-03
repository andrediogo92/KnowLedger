package org.knowledger.ledger.service.results

import org.knowledger.common.results.Failable
import org.knowledger.common.results.HardFailure
import org.knowledger.common.results.PropagatedFailure
import org.knowledger.common.storage.LedgerContract


/**
 * Result class representing loading of [LedgerContract] classes
 * from the database.
 */
sealed class LoadFailure : Failable {
    data class NonExistentData(
        override val cause: String
    ) : LoadFailure()

    data class NonMatchingCrypter(
        override val cause: String
    ) : LoadFailure()

    data class UnrecognizedDataType(
        override val cause: String
    ) : LoadFailure()


    data class UnknownFailure(
        override val cause: String,
        override val exception: Exception? = null
    ) : LoadFailure(), HardFailure

    data class Propagated(
        override val pointOfFailure: String,
        override val failable: Failable
    ) : LoadFailure(), PropagatedFailure


}