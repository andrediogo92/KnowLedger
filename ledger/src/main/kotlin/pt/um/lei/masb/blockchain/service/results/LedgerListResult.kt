package pt.um.lei.masb.blockchain.service.results

import pt.um.lei.masb.blockchain.results.Failable
import pt.um.lei.masb.blockchain.service.ServiceHandle

sealed class LedgerListResult<T : ServiceHandle> {
    data class Success<T : ServiceHandle>(
        val data: List<T>
    ) : LedgerListResult<T>()

    data class QueryFailure<T : ServiceHandle>(
        override val cause: String,
        val exception: Exception? = null
    ) : Failable, LedgerListResult<T>()

    data class NonExistentData<T : ServiceHandle>(
        override val cause: String
    ) : Failable, LedgerListResult<T>()

    data class NonMatchingCrypter<T : ServiceHandle>(
        override val cause: String
    ) : Failable, LedgerListResult<T>()

    data class UnregisteredCrypter<T : ServiceHandle>(
        override val cause: String
    ) : Failable, LedgerListResult<T>()

    data class Propagated<T : ServiceHandle>(
        val pointOfFailure: String,
        val failable: Failable
    ) : Failable, LedgerListResult<T>() {
        override val cause: String
            get() = "$pointOfFailure: ${failable.cause}"
    }

}
