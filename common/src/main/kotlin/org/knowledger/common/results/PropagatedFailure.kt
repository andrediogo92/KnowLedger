package org.knowledger.common.results

/**
 * Reserved for indirect irrecoverable errors propagated
 * by some internal result.
 */
interface PropagatedFailure : Failable {
    val pointOfFailure: String
    val failable: Failable
    override val cause: String
        get() = "$pointOfFailure: ${failable.cause}"
}