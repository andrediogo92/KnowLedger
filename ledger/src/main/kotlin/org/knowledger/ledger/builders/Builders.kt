package org.knowledger.ledger.builders

import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.results.BuilderFailure
import org.knowledger.ledger.service.handles.ChainHandle

fun ChainHandle.chainBuilder(
    identity: Identity
): ChainBuilder =
    WorkingChainBuilder(adapterManager, container, id, identity)

internal fun <T : Builder<*, *>> T.uninitialized(
    parameter: String
): BuilderFailure.ParameterUninitialized =
    BuilderFailure.ParameterUninitialized(
        "$parameter not initialized for ${javaClass.simpleName}"
    )
