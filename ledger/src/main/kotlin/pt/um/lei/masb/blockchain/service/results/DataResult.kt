package pt.um.lei.masb.blockchain.service.results

import pt.um.lei.masb.blockchain.ledger.LedgerContract

sealed class DataResult<T : LedgerContract> {
    data class Success<T : LedgerContract>(
        val data: T
    ) : DataResult<T>()

    data class QueryFailure<T : LedgerContract>(
        val cause: String,
        val exception: Exception?
    ) : DataResult<T>()

    data class NonMatchingCrypter<T : LedgerContract>(
        val at: String
    ) : DataResult<T>()

    data class UnregisteredCrypter<T : LedgerContract>(
        val at: String
    ) : DataResult<T>()

    data class UnrecognizedDataType<T : LedgerContract>(
        val at: String
    ) : DataResult<T>()

    data class IllegalConversion<T : LedgerContract>(
        val at: String
    ) : DataResult<T>()
}