package pt.um.masb.common.storage.results

import pt.um.masb.common.results.Failable


sealed class QueryFailure : Failable {
    data class NonExistentData(
        override val cause: String
    ) : QueryFailure()


    /**
     * Reserved for direct irrecoverable errors.
     * Query failures will wrap exceptions if thrown.
     */
    data class UnknownFailure(
        override val cause: String,
        val exception: Exception? = null
    ) : QueryFailure()

    /**
     * Reserved for indirect irrecoverable errors propagated
     * by some internal result.
     */
    data class Propagated(
        val pointOfFailure: String,
        val failable: Failable
    ) : QueryFailure() {
        override val cause: String
            get() = "$pointOfFailure: ${failable.cause}"
    }

}