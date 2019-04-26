package pt.um.lei.masb.agent.utils

import pt.um.lei.masb.blockchain.service.ServiceHandle
import pt.um.lei.masb.blockchain.service.results.LedgerResult

fun <T : ServiceHandle> LedgerResult<T>.unpackOrThrow(): T =
    when (this) {
        is LedgerResult.Success -> data
        is LedgerResult.NonExistentData -> throw RuntimeException(cause)
        is LedgerResult.QueryFailure -> throw exception ?: RuntimeException(cause)
        is LedgerResult.NonMatchingCrypter -> throw RuntimeException(cause)
        is LedgerResult.Propagated -> throw RuntimeException(cause)
    }

inline fun <T : ServiceHandle> LedgerResult<T>.unpackOrThrowAndDoOnNonExistent(
    attempt: LedgerResult.NonExistentData<T>.() -> T
): T =
    when (this) {
        is LedgerResult.Success -> data
        is LedgerResult.NonExistentData -> attempt()
        is LedgerResult.QueryFailure -> throw exception ?: RuntimeException(cause)
        is LedgerResult.NonMatchingCrypter -> throw RuntimeException(cause)
        is LedgerResult.Propagated -> throw RuntimeException(cause)
    }
