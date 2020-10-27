package org.knowledger.ledger.chain.results

import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.tryOrConvertToFailure
import org.knowledger.ledger.storage.LedgerId
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal inline fun tryOrHandleUnknownFailure(
    function: () -> Outcome<LedgerId, LedgerBuilderFailure>,
): Outcome<LedgerId, LedgerBuilderFailure> {
    contract {
        callsInPlace(function, InvocationKind.EXACTLY_ONCE)
    }
    return tryOrConvertToFailure(function) { exception ->
        LedgerBuilderFailure.UnknownFailure(exception.message ?: "", exception)
    }
}
