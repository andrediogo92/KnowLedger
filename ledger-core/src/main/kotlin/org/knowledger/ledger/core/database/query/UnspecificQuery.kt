package org.knowledger.ledger.core.database.query


data class UnspecificQuery(
    override val query: String,
    override val params: Map<String, Any> = emptyMap()
) : GenericQuery