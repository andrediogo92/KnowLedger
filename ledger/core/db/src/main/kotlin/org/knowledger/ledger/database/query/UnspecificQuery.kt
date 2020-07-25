package org.knowledger.ledger.database.query


data class UnspecificQuery(
    override val query: String,
    override val params: Map<String, Any> = emptyMap()
) : GenericQuery