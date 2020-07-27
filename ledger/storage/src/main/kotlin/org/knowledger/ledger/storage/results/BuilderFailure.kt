package org.knowledger.ledger.storage.results

import org.knowledger.ledger.results.Failable
import org.knowledger.ledger.results.Failure

sealed class BuilderFailure : Failure {
    class ParameterUninitialized(
        cause: String
    ) : BuilderFailure() {
        override val failable: Failable.LightFailure =
            Failable.LightFailure(cause)
    }

    class ParameterNotRegistered(
        cause: String
    ) : BuilderFailure() {
        override val failable: Failable.LightFailure =
            Failable.LightFailure(cause)
    }

    class UnknownFailure(
        cause: String,
        exception: Exception?
    ) : BuilderFailure() {
        override val failable: Failable.HardFailure =
            Failable.HardFailure(cause, exception)
    }

    class Propagated(
        pointOfFailure: String,
        failable: Failable
    ) : BuilderFailure() {
        override val failable: Failable.PropagatedFailure =
            Failable.PropagatedFailure(
                pointOfFailure,
                failable
            )
    }
}