package pt.um.lei.masb.blockchain.service.results

import pt.um.lei.masb.blockchain.ledger.LedgerContract
import pt.um.lei.masb.blockchain.service.ServiceHandle

inline fun <R : LedgerContract, reified T : ServiceHandle> DataResult<R>.intoLedger(
    success: R.() -> T
): LedgerResult<T> =
    when (this) {
        is DataResult.Success ->
            LedgerResult.Success(
                data.success()
            )
        is DataResult.QueryFailure ->
            LedgerResult.QueryFailure(
                cause, exception
            )
        is DataResult.NonMatchingCrypter ->
            LedgerResult.NonMatchingCrypter(at)
        is DataResult.UnregisteredCrypter ->
            LedgerResult.UnregisteredCrypter(at)
        is DataResult.UnrecognizedDataType ->
            LedgerResult.QueryFailure(at, RuntimeException())
        is DataResult.IllegalConversion ->
            LedgerResult.QueryFailure(at, RuntimeException())
    }

inline fun <R : ServiceHandle, reified T : LedgerContract> LedgerResult<R>.intoData(
    success: R.() -> T
): DataResult<T> =
    when (this) {
        is LedgerResult.Success ->
            DataResult.Success(
                data.success()
            )
        is LedgerResult.QueryFailure ->
            DataResult.QueryFailure(
                cause, exception
            )
        is LedgerResult.NonMatchingCrypter ->
            DataResult.NonMatchingCrypter(at)
        is LedgerResult.UnregisteredCrypter ->
            DataResult.UnregisteredCrypter(at)
        is LedgerResult.InexistentFailure ->
            DataResult.QueryFailure(
                "Into: InexistentFailure",
                RuntimeException(at)
            )
    }

inline fun <R : ServiceHandle, reified T : ServiceHandle> LedgerResult<R>.intoLedger(
    success: R.() -> T
): LedgerResult<T> =
    when (this) {
        is LedgerResult.Success ->
            LedgerResult.Success(
                data.success()
            )
        is LedgerResult.QueryFailure ->
            LedgerResult.QueryFailure(
                cause, exception
            )
        is LedgerResult.NonMatchingCrypter ->
            LedgerResult.NonMatchingCrypter(at)
        is LedgerResult.UnregisteredCrypter ->
            LedgerResult.UnregisteredCrypter(at)
        is LedgerResult.InexistentFailure ->
            LedgerResult.InexistentFailure(at)
    }


inline fun <R : LedgerContract, reified T : LedgerContract> DataResult<R>.intoData(
    success: R.() -> T
): DataResult<T> =
    when (this) {
        is DataResult.Success ->
            DataResult.Success(
                data.success()
            )
        is DataResult.QueryFailure ->
            DataResult.QueryFailure(
                cause, exception
            )
        is DataResult.NonMatchingCrypter ->
            DataResult.NonMatchingCrypter(at)
        is DataResult.UnregisteredCrypter ->
            DataResult.UnregisteredCrypter(at)
        is DataResult.UnrecognizedDataType ->
            DataResult.UnrecognizedDataType(at)
        is DataResult.IllegalConversion -> DataResult.IllegalConversion(at)
    }


inline fun <R : LedgerContract, reified T : LedgerContract> DataResult<R>.intoList(): ListResult<T> =
    when (this) {
        is DataResult.Success ->
            //This should never ever happen.
            ListResult.IllegalConversion("Data result can't convert into list.")
        is DataResult.QueryFailure ->
            ListResult.QueryFailure(
                cause, exception
            )
        is DataResult.NonMatchingCrypter ->
            ListResult.NonMatchingCrypter(at)
        is DataResult.UnregisteredCrypter ->
            ListResult.UnregisteredCrypter(at)
        is DataResult.UnrecognizedDataType ->
            ListResult.UnrecognizedDataType(at)
        is DataResult.IllegalConversion ->
            ListResult.IllegalConversion(at)
    }


inline fun <R : LedgerContract, reified T : LedgerContract> ListResult<R>.intoData(): DataResult<T> =
    when (this) {
        is ListResult.Success ->
            //This should never ever happen.
            DataResult.IllegalConversion("List result can't convert into data result.")
        is ListResult.QueryFailure ->
            DataResult.QueryFailure(
                cause, exception
            )
        is ListResult.NonMatchingCrypter ->
            DataResult.NonMatchingCrypter(at)
        is ListResult.UnregisteredCrypter ->
            DataResult.UnregisteredCrypter(at)
        is ListResult.UnrecognizedDataType ->
            DataResult.UnrecognizedDataType(at)
        is ListResult.IllegalConversion ->
            DataResult.IllegalConversion(at)
    }


inline fun <reified T : LedgerContract> List<DataResult<T>>.collapse(): ListResult<T> {
    val short = this.find {
        it !is DataResult.Success
    }
    return short?.intoList() ?: ListResult.Success(
        this.map {
            (it as DataResult.Success).data
        }
    )
}

fun DEADCODE(): Nothing {
    throw RuntimeException("Dead code invoked")
}