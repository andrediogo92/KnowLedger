package org.knowledger.ledger.builders

import org.knowledger.ledger.core.results.Failure
import org.knowledger.ledger.core.results.Outcome

interface Builder<T, R : Failure> {
    fun build(): Outcome<T, R>
}