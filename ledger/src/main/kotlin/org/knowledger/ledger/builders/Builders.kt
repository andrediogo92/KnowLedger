package org.knowledger.ledger.builders

import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.results.BuilderFailure
import org.knowledger.ledger.service.Identity
import org.knowledger.ledger.service.handles.LedgerHandle

fun chainBuilder(
    ledgerHash: Hash,
    chainId: ChainId,
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
            Outcome.Ok(WorkingChainBuilder(container, chainId, identity))
        }
    }
}

internal fun <T : Builder<*, *>> T.uninitialized(
    parameter: String
): BuilderFailure.ParameterUninitialized =
    BuilderFailure.ParameterUninitialized(
        "$parameter not initialized for ${this::class.simpleName}"
    )
