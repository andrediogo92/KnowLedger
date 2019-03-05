package pt.um.lei.masb.blockchain.persistance.query

import pt.um.lei.masb.blockchain.ledger.Hash
import pt.um.lei.masb.blockchain.ledger.truncated

class ClusterSelect(
    val typeCluster: String,
    val blockChainId: Hash,
    private var projection: String = "*",
    private var filters: MutableMap<Filters, String> = mutableMapOf(),
    private var queryParams: MutableMap<String, Any> = mutableMapOf()
) : GenericQuery {
    override val query: String
        get() = "SELECT $projection FROM CLUSTER:${
        typeCluster.toLowerCase()
        }${
        blockChainId.truncated().toLowerCase()
        } ${
        filters.entries.joinToString(" ") {
            "${it.key.s} ${it.value}"
        }}"

    override val params: Map<String, Any>
        get() = queryParams

    fun withProjection(projection: String): ClusterSelect =
        apply {
            this.projection = projection
        }

    fun withSimpleFilter(
        filter: Filters,
        field: String,
        varName: String,
        variable: Any
    ): ClusterSelect =
        apply {
            filters.merge(
                filter,
                "$field = :$varName"
            )
            { _: String,
              it: String ->
                it
            }
            queryParams[varName] = variable
        }

    fun withSimpleFilter(
        filter: Filters,
        field: String
    ): ClusterSelect =
        apply {
            filters.merge(filter, field)
            { _: String,
              it: String ->
                it
            }
        }

    fun withContainsFilter(
        field: String,
        varName: String,
        variable: Any
    ): ClusterSelect =
        apply {
            filters.merge(Filters.WHERE, "$field CONTAINSALL :$varName")
            { _: String,
              it: String ->
                it
            }
            queryParams[varName] = variable
        }
}