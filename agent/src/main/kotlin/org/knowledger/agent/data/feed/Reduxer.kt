package org.knowledger.agent.data.feed

import org.knowledger.common.data.LedgerData


interface Reduxer {
    fun reduce(type: LedgerData): String
    fun type(): String
}
