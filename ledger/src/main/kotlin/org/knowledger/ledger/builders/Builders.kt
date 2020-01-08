package org.knowledger.ledger.builders

import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.results.BuilderFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.handles.ChainHandle
import org.knowledger.ledger.service.handles.LedgerHandle


fun ChainId.chainBuilder(
    identity: Identity
): Outcome<ChainBuilder, BuilderFailure> {
    val container = LedgerHandle.getContainer(
        ledgerHash
    )
    return when (container) {
        null ->
            Outcome.Error(
                BuilderFailure.ParameterNotRegistered(
                    "There is no active LedgerHandle with corresponding hash: $ledgerHash to derive builder."
                )
            )
        else -> {
            Outcome.Ok(WorkingChainBuilder(container, this, identity))
        }
    }
}

fun ChainHandle.chainBuilder(
    identity: Identity
): Outcome<ChainBuilder, BuilderFailure> =
    id.chainBuilder(identity)

internal fun <T : Builder<*, *>> T.uninitialized(
    parameter: String
): BuilderFailure.ParameterUninitialized =
    BuilderFailure.ParameterUninitialized(
        "$parameter not initialized for ${javaClass.simpleName}"
    )
