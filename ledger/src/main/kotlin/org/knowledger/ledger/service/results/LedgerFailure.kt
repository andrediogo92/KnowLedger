package org.knowledger.ledger.service.results

import org.knowledger.ledger.core.results.Failable
import org.knowledger.ledger.core.results.HardFailure
import org.knowledger.ledger.core.results.PropagatedFailure


sealed class LedgerFailure : Failable {

    data class NonExistentData(
        override val cause: String
    ) : LedgerFailure()

    data class NonMatchingCrypter(
        override val cause: String
    ) : LedgerFailure()

    data class NoKnownStorageAdapter(
        override val cause: String
    ) : LedgerFailure()


    data class UnknownFailure(
        override val cause: String,
        override val exception: Exception? = null
    ) : LedgerFailure(), HardFailure

    data class Propagated(
        override val pointOfFailure: String,
        override val failable: Failable
    ) : LedgerFailure(), PropagatedFailure

}
