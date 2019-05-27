package pt.um.masb.ledger.service.results

import pt.um.masb.common.results.Failable
import pt.um.masb.common.storage.LedgerContract


/**
 * Result class representing loading of [LedgerContract] classes
 * from the database.
 */
sealed class LoadFailure : Failable {
    data class NonExistentData(
        override val cause: String
    ) : LoadFailure()

    data class NonMatchingCrypter(
        override val cause: String
    ) : LoadFailure()

    data class UnrecognizedDataType(
        override val cause: String
    ) : LoadFailure()


    /**
     * Reserved for direct irrecoverable errors.
     * Query failures will wrap exceptions if thrown.
     */
    data class UnknownFailure(
        override val cause: String,
        val exception: Exception? = null
    ) : LoadFailure()

    /**
     * Reserved for indirect irrecoverable errors propagated
     * by some internal result.
     */
    data class Propagated(
        val pointOfFailure: String,
        val failable: Failable
    ) : LoadFailure() {
        override val cause: String
            get() = "$pointOfFailure: ${failable.cause}"
    }

}