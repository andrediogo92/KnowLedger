package org.knowledger.ledger.builders

import org.knowledger.ledger.results.Failure
import org.knowledger.ledger.results.Outcome

interface Builder<T, R : Failure> {
    fun build(): Outcome<T, R>
}