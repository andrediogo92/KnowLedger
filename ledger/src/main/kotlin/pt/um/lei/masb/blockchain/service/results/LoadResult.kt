package pt.um.lei.masb.blockchain.service.results

import pt.um.lei.masb.blockchain.ledger.LedgerContract
import pt.um.lei.masb.blockchain.results.Failable

/**
 * Result class representing loading of [LedgerContract] classes
 * from the database.
 */
sealed class LoadResult<T : LedgerContract> {
    data class Success<T : LedgerContract>(
        val data: T
    ) : LoadResult<T>()

    data class NonExistentData<T : LedgerContract>(
        override val cause: String
    ) : Failable, LoadResult<T>()

    data class NonMatchingCrypter<T : LedgerContract>(
        override val cause: String
    ) : Failable, LoadResult<T>()

    data class UnrecognizedDataType<T : LedgerContract>(
        override val cause: String
    ) : Failable, LoadResult<T>()


    /**
     * Reserved for direct irrecoverable errors.
     * Query failures will wrap exceptions if thrown.
     */
    data class QueryFailure<T : LedgerContract>(
        override val cause: String,
        val exception: Exception? = null
    ) : Failable, LoadResult<T>()

    /**
     * Reserved for indirect irrecoverable errors propagated
     * by some internal result.
     */
    data class Propagated<T : LedgerContract>(
        val pointOfFailure: String,
        val failable: Failable
    ) : Failable, LoadResult<T>() {
        override val cause: String
            get() = "$pointOfFailure: ${failable.cause}"
    }

}