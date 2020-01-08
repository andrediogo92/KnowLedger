package org.knowledger.ledger.results

sealed class Failable {
    abstract val cause: String

    /**
     * Reserved for direct irrecoverable errors.
     * Query failures will wrap exceptions if thrown.
     */
    data class HardFailure(
        override val cause: String,
        val exception: Exception?
    ) : Failable()

    data class LightFailure(
        override val cause: String
    ) : Failable()

    /**
     * Reserved for indirect irrecoverable errors propagated
     * by some internal result.
     */
    data class PropagatedFailure(
        val pointOfFailure: String,
        val inner: Failable
    ) : Failable() {
        override val cause: String
            get() = "$pointOfFailure: ${inner.cause}"

    }
}