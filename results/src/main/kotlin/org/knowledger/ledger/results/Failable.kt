package org.knowledger.ledger.results

/**
 * Failable is a simple error class that distinguishes
 * between three distinct types of failure:
 * 1. [HardFailure] -> Associated with irrecoverable
 * errors, usually triggered by exceptions.
 * 2. [LightFailure] -> A simple descriptive error cause.
 * 3. [PropagatedFailure] -> A recursive failure that
 * wraps previous failures with a new point of failure.
 * This permits propagating an ad-hoc error stack.
 */
sealed class Failable {
    abstract val cause: String

    /**
     * Reserved for direct irrecoverable errors.
     * [HardFailure] cam wrap exceptions.
     */
    data class HardFailure(override val cause: String, val exception: Exception?) : Failable()

    /**
     * Used for recoverable errors.
     */
    data class LightFailure(override val cause: String) : Failable()

    /**
     * Reserved for indirect errors propagated
     * by some internal result.
     * Cause will be prefixed by the [pointOfFailure] supplied.
     */
    data class PropagatedFailure(val pointOfFailure: String, val inner: Failable) : Failable() {
        override val cause: String get() = "$pointOfFailure :>: ${inner.cause}"

    }
}