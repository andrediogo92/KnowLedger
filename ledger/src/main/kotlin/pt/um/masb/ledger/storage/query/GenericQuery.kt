package pt.um.masb.ledger.storage.query

interface GenericQuery {
    val query: String
    val params: Map<String, Any>
}