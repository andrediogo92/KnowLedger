package pt.um.masb.common.storage.results

import pt.um.masb.common.results.Failable


sealed class QueryResult<T : Any> {
    data class Success<T : Any>(val data: T) : QueryResult<T>()

    data class QueryFailure<T : Any>(
        override val cause: String,
        val exception: Exception? = null
    ) : Failable, QueryResult<T>()

    data class NonExistentData<T : Any>(
        override val cause: String
    ) : Failable, QueryResult<T>()

    data class Propagated<T : Any>(
        val pointOfFailure: String,
        val failable: Failable
    ) : Failable, QueryResult<T>() {
        override val cause: String
            get() = "$pointOfFailure: ${failable.cause}"
    }
}