package pt.um.masb.ledger.service.results

import pt.um.masb.common.results.Failable


sealed class LedgerFailure : Failable {

    data class NonExistentData(
        override val cause: String
    ) : LedgerFailure()

    data class NonMatchingCrypter(
        override val cause: String
    ) : LedgerFailure()

    data class NoKnownStorageAdapter(
        override val cause: String
    ) : LedgerFailure()


    /**
     * Reserved for direct irrecoverable errors.
     * Query failures will wrap exceptions if thrown.
     */
    data class UnknownFailure(
        override val cause: String,
        val exception: Exception? = null
    ) : LedgerFailure()


    /**
     * Reserved for indirect irrecoverable errors propagated
     * by some internal result.
     */
    data class Propagated(
        val pointOfFailure: String,
        val failable: Failable
    ) : LedgerFailure() {
        override val cause: String
            get() = "$pointOfFailure: ${failable.cause}"
    }

}
