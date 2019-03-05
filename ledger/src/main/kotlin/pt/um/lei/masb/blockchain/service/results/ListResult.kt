package pt.um.lei.masb.blockchain.service.results

import pt.um.lei.masb.blockchain.ledger.LedgerContract

sealed class ListResult<T : LedgerContract> {
    data class Success<T : LedgerContract>(
        val data: List<T>
    ) : ListResult<T>()

    data class QueryFailure<T : LedgerContract>(
        val cause: String,
        val exception: Exception?
    ) : ListResult<T>()

    data class NonMatchingCrypter<T : LedgerContract>(
        val at: String
    ) : ListResult<T>()

    data class UnregisteredCrypter<T : LedgerContract>(
        val at: String
    ) : ListResult<T>()

    data class UnrecognizedDataType<T : LedgerContract>(
        val at: String
    ) : ListResult<T>()

    data class IllegalConversion<T : LedgerContract>(
        val at: String
    ) : ListResult<T>()
}