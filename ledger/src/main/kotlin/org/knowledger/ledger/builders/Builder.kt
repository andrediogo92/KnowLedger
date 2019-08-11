package org.knowledger.ledger.builders

import org.knowledger.ledger.core.results.Failable
import org.knowledger.ledger.core.results.Outcome

interface Builder<T, R : Failable> {
    fun build(): Outcome<T, R>
}