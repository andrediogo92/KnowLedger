package org.knowledger.agent.feed

import org.knowledger.ledger.data.LedgerData


interface Reduxer {
    fun reduce(type: LedgerData): String
    fun type(): String
}
