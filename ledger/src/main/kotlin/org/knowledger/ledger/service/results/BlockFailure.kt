package org.knowledger.ledger.service.results

import org.knowledger.ledger.core.results.Failable
import org.knowledger.ledger.core.results.Failure
import org.knowledger.ledger.data.Hash


sealed class BlockFailure : Failure {
    class UnknownFailure(
        cause: String,
        exception: Exception?
    ) : BlockFailure() {
        override val failable: Failable.HardFailure =
            Failable.HardFailure(cause, exception)
    }

    class Propagated(
        pointOfFailure: String,
        failable: Failable
    ) : BlockFailure() {
        override val failable: Failable.PropagatedFailure =
            Failable.PropagatedFailure(pointOfFailure, failable)
    }

    data class NoBlockForHash(
        val hash: Hash
    ) : BlockFailure() {
        override val failable: Failable =
            Failable.LightFailure("No block with hash -> ${hash.print} in BlockPool")
    }

}