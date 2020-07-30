package org.knowledger.ledger.adapters

import org.knowledger.ledger.database.results.QueryFailure

internal interface QueryLoadable<out T> : Loadable<T, QueryFailure> {
}