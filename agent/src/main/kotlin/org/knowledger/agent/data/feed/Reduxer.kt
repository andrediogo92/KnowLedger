package org.knowledger.agent.data.feed

import org.knowledger.ledger.core.data.LedgerData


interface Reduxer {
    fun reduce(type: LedgerData): String
    fun type(): String
}
