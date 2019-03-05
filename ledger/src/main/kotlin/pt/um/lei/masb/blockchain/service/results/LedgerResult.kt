package pt.um.lei.masb.blockchain.service.results

import pt.um.lei.masb.blockchain.service.ServiceHandle

sealed class LedgerResult<T : ServiceHandle> {
    data class Success<T : ServiceHandle>(
        val data: T
    ) : LedgerResult<T>()

    data class QueryFailure<T : ServiceHandle>(
        val cause: String,
        val exception: Exception?
    ) : LedgerResult<T>()

    data class InexistentFailure<T : ServiceHandle>(
        val at: String
    ) : LedgerResult<T>()

    data class NonMatchingCrypter<T : ServiceHandle>(
        val at: String
    ) : LedgerResult<T>()

    data class UnregisteredCrypter<T : ServiceHandle>(
        val at: String
    ) : LedgerResult<T>()
}
