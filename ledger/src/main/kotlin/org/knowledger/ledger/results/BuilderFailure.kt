package org.knowledger.ledger.results

import org.knowledger.ledger.core.results.Failable
import org.knowledger.ledger.core.results.HardFailure
import org.knowledger.ledger.core.results.PropagatedFailure

sealed class BuilderFailure : Failable {
    data class ParameterUninitialized(
        override val cause: String
    ) : BuilderFailure()

    data class ParameterNotRegistered(
        override val cause: String
    ) : BuilderFailure()

    data class UnknownFailure(
        override val cause: String,
        override val exception: Exception? = null
    ) : BuilderFailure(), HardFailure

    data class Propagated(
        override val pointOfFailure: String,
        override val failable: Failable
    ) : BuilderFailure(), PropagatedFailure
}