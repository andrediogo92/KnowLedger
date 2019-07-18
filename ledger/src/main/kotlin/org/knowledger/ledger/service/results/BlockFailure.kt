package org.knowledger.ledger.service.results

import org.knowledger.ledger.core.results.Failable
import org.knowledger.ledger.core.results.HardFailure
import org.knowledger.ledger.core.results.PropagatedFailure


sealed class BlockFailure : Failable {
    data class UnknownFailure(
        override val cause: String,
        override val exception: Exception? = null
    ) : BlockFailure(), HardFailure

    data class Propagated(
        override val pointOfFailure: String,
        override val failable: Failable
    ) : BlockFailure(), PropagatedFailure

}