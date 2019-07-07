package org.knowledger.ledger.service.results

import org.knowledger.common.results.Failable
import org.knowledger.common.results.HardFailure
import org.knowledger.common.results.PropagatedFailure

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