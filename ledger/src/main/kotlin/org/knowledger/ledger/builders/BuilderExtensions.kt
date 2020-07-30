package org.knowledger.ledger.builders

import org.knowledger.ledger.storage.results.BuilderFailure

internal fun <T : Builder<*, *>> T.uninitialized(
    parameter: String
): BuilderFailure.ParameterUninitialized =
    BuilderFailure.ParameterUninitialized(
        "$parameter not initialized for ${javaClass.simpleName}"
    )
