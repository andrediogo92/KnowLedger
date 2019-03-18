package pt.um.lei.masb.blockchain.service.results

import pt.um.lei.masb.blockchain.ledger.LedgerContract
import pt.um.lei.masb.blockchain.utils.Failable

sealed class LoadResult<T : LedgerContract> {
    data class Success<T : LedgerContract>(
        val data: T
    ) : LoadResult<T>()

    data class QueryFailure<T : LedgerContract>(
        override val cause: String,
        val exception: Exception? = null
    ) : Failable, LoadResult<T>()

    data class NonExistentData<T : LedgerContract>(
        override val cause: String
    ) : Failable, LoadResult<T>()

    data class NonMatchingCrypter<T : LedgerContract>(
        override val cause: String
    ) : Failable, LoadResult<T>()

    data class UnregisteredCrypter<T : LedgerContract>(
        override val cause: String
    ) : Failable, LoadResult<T>()

    data class UnrecognizedDataType<T : LedgerContract>(
        override val cause: String
    ) : Failable, LoadResult<T>()
}