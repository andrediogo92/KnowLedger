package org.knowledger.ledger.database.results

import org.knowledger.ledger.results.Failable
import org.knowledger.ledger.results.Failure


sealed class DataFailure : Failure {
    class NonExistentData(
        val cause: String
    ) : DataFailure() {
        override val failable: Failable.LightFailure =
            Failable.LightFailure(
                cause
            )
    }

    class UnrecognizedDataType(
        cause: String
    ) : DataFailure() {
        override val failable: Failable.LightFailure =
            Failable.LightFailure(
                cause
            )
    }

    class UnrecognizedUnit(
        cause: String
    ) : DataFailure() {
        override val failable: Failable.LightFailure =
            Failable.LightFailure(
                cause
            )
    }

    class UnexpectedClass(
        cause: String
    ) : DataFailure() {
        override val failable: Failable.LightFailure =
            Failable.LightFailure(
                cause
            )
    }

    class NonRegisteredSchema(
        val cause: String
    ) : DataFailure() {
        override val failable: Failable.LightFailure =
            Failable.LightFailure(
                cause
            )
    }

    class UnknownFailure(
        cause: String,
        exception: Exception?
    ) : DataFailure() {
        override val failable: Failable.HardFailure =
            Failable.HardFailure(cause, exception)
    }

    class Propagated(
        pointOfFailure: String,
        failable: Failable
    ) : DataFailure() {
        override val failable: Failable.PropagatedFailure =
            Failable.PropagatedFailure(pointOfFailure, failable)
    }
}