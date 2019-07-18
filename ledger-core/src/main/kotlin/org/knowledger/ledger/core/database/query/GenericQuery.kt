package org.knowledger.ledger.core.database.query

interface GenericQuery {
    val query: String
    val params: Map<String, Any>
}