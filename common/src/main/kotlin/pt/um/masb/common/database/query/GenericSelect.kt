package pt.um.masb.common.database.query


class GenericSelect(
    val typeId: String,
    private var projection: String = "*",
    private var filters: MutableMap<Filters, String> = mutableMapOf(),
    private var queryParams: MutableMap<String, Any> = mutableMapOf()
) : GenericQuery {
    val ident: (String, String) -> String
        get() = { _: String, it: String ->
            it
        }

    override val query: String
        get() = "SELECT $projection FROM ${
        typeId.toLowerCase()
        } ${
        filters.entries.joinToString(" ") {
            "${it.key.s} ${it.value}"
        }
        }"

    override val params: Map<String, Any>
        get() = queryParams

    fun withProjection(projection: String): GenericSelect =
        apply {
            this.projection = projection
        }

    fun withSimpleFilter(
        filter: Filters,
        field: String,
        varName: String,
        variable: Any
    ): GenericSelect =
        apply {
            filters.merge(
                filter,
                "$field = :$varName",
                ident
            )
            queryParams[varName] = variable
        }


    fun withSimpleFilter(
        filter: Filters,
        field: String
    ): GenericSelect =
        apply {
            filters.merge(
                filter, field, ident
            )
        }

    fun withBetweenFilter(
        filter: Filters,
        field: String,
        varNames: Pair<String, String>,
        variables: Pair<Any, Any>,
        op: SimpleBinaryOperator
    ): GenericSelect =
        apply {
            filters.merge(
                filter,
                "$field BETWEEN :${varNames.first} $op :${varNames.second}",
                ident
            )
            queryParams[varNames.first] = variables.first
            queryParams[varNames.second] = variables.second
        }

    fun withContainsAllFilter(
        field: String,
        varName: String,
        variable: Any
    ): GenericSelect =
        apply {
            filters.merge(
                Filters.WHERE,
                "$field CONTAINSALL :$varName",
                ident
            )
            queryParams[varName] = variable
        }
}