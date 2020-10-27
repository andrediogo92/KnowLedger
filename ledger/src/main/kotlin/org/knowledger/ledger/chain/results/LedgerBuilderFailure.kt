package org.knowledger.ledger.chain.results

import org.knowledger.ledger.results.Failable
import org.knowledger.ledger.results.Failure

sealed class LedgerBuilderFailure : Failure {
    class PathCannotResolveAsDirectory(cause: String) : LedgerBuilderFailure() {
        override val failable: Failable.LightFailure =
            Failable.LightFailure(cause)
    }

    object NoIdentitySupplied : LedgerBuilderFailure() {
        override val failable: Failable.LightFailure =
            Failable.LightFailure("No hash or identity supplied to builder.")
    }

    object NonExistentLedger : LedgerBuilderFailure() {
        override val failable: Failable.LightFailure =
            Failable.LightFailure("No ledger matching hash in DB")
    }

    class NotRegisteredDataFormula(cause: String) : LedgerBuilderFailure() {
        override val failable: Failable.LightFailure =
            Failable.LightFailure(cause)
    }

    class UnknownFailure(cause: String, exception: Exception?) : LedgerBuilderFailure() {
        override val failable: Failable.HardFailure =
            Failable.HardFailure(cause, exception)
    }

    class Propagated(pointOfFailure: String, failable: Failable) : LedgerBuilderFailure() {
        override val failable: Failable.PropagatedFailure =
            Failable.PropagatedFailure(pointOfFailure, failable)
    }
}