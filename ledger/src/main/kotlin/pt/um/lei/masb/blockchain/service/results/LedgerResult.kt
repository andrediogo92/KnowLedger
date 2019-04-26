package pt.um.lei.masb.blockchain.service.results

import pt.um.lei.masb.blockchain.results.Failable
import pt.um.lei.masb.blockchain.service.ServiceHandle

sealed class LedgerResult<T : ServiceHandle> {
    data class Success<T : ServiceHandle>(
        val data: T
    ) : LedgerResult<T>()

    data class QueryFailure<T : ServiceHandle>(
        override val cause: String,
        val exception: Exception? = null
    ) : Failable, LedgerResult<T>()

    data class NonExistentData<T : ServiceHandle>(
        override val cause: String
    ) : Failable, LedgerResult<T>()

    data class NonMatchingCrypter<T : ServiceHandle>(
        override val cause: String
    ) : Failable, LedgerResult<T>()

    data class Propagated<T : ServiceHandle>(
        val pointOfFailure: String,
        val failable: Failable
    ) : Failable, LedgerResult<T>() {
        override val cause: String
            get() = "$pointOfFailure: ${failable.cause}"
    }

}
