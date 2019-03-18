package pt.um.lei.masb.blockchain.utils

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.ledger.LedgerContract
import pt.um.lei.masb.blockchain.persistance.results.DataListResult
import pt.um.lei.masb.blockchain.persistance.results.DataResult
import pt.um.lei.masb.blockchain.persistance.results.PersistResult
import pt.um.lei.masb.blockchain.service.ServiceHandle
import pt.um.lei.masb.blockchain.service.results.LedgerResult
import pt.um.lei.masb.blockchain.service.results.LoadListResult
import pt.um.lei.masb.blockchain.service.results.LoadResult


inline fun <R : LedgerContract, T : ServiceHandle> LoadResult<R>.intoLedger(
    success: R.() -> T
): LedgerResult<T> =
    when (this) {
        is LoadResult.Success ->
            LedgerResult.Success(data.success())
        is LoadResult.QueryFailure ->
            LedgerResult.QueryFailure(cause, exception)
        is LoadResult.NonMatchingCrypter ->
            LedgerResult.NonMatchingCrypter(cause)
        is LoadResult.UnregisteredCrypter ->
            LedgerResult.UnregisteredCrypter(cause)
        is LoadResult.UnrecognizedDataType ->
            LedgerResult.QueryFailure(cause)
        is LoadResult.NonExistentData ->
            LedgerResult.NonExistentData(cause)
    }

fun <R : LedgerContract, T : ServiceHandle> LoadResult<R>.intoLedger(): LedgerResult<T> =
    when (this) {
        is LoadResult.Success ->
            LedgerResult.QueryFailure("LoadResult can't auto-convert into LedgerResult.")
        is LoadResult.QueryFailure ->
            LedgerResult.QueryFailure(cause, exception)
        is LoadResult.NonMatchingCrypter ->
            LedgerResult.NonMatchingCrypter(cause)
        is LoadResult.UnregisteredCrypter ->
            LedgerResult.UnregisteredCrypter(cause)
        is LoadResult.UnrecognizedDataType ->
            LedgerResult.QueryFailure(cause)
        is LoadResult.NonExistentData ->
            LedgerResult.NonExistentData(cause)
    }


inline fun <R : ServiceHandle, T : LedgerContract> LedgerResult<R>.intoLoad(
    success: R.() -> T
): LoadResult<T> =
    when (this) {
        is LedgerResult.Success ->
            LoadResult.Success(data.success())
        is LedgerResult.QueryFailure ->
            LoadResult.QueryFailure(cause, exception)
        is LedgerResult.NonMatchingCrypter ->
            LoadResult.NonMatchingCrypter(cause)
        is LedgerResult.UnregisteredCrypter ->
            LoadResult.UnregisteredCrypter(cause)
        is LedgerResult.NonExistentData ->
            LoadResult.QueryFailure(cause)
    }

fun <R : ServiceHandle, T : LedgerContract> LedgerResult<R>.intoLoad(): LoadResult<T> =
    when (this) {
        is LedgerResult.Success ->
            LoadResult.QueryFailure("LedgerResult can't auto-convert into LoadResult.")
        is LedgerResult.QueryFailure ->
            LoadResult.QueryFailure(cause, exception)
        is LedgerResult.NonMatchingCrypter ->
            LoadResult.NonMatchingCrypter(cause)
        is LedgerResult.UnregisteredCrypter ->
            LoadResult.UnregisteredCrypter(cause)
        is LedgerResult.NonExistentData ->
            LoadResult.NonExistentData(cause)
    }


inline fun <R : ServiceHandle, T : ServiceHandle> LedgerResult<R>.intoLedger(
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
            LedgerResult.NonMatchingCrypter(cause)
        is LedgerResult.UnregisteredCrypter ->
            LedgerResult.UnregisteredCrypter(cause)
        is LedgerResult.NonExistentData ->
            LedgerResult.NonExistentData(cause)
    }

fun <R : ServiceHandle, T : ServiceHandle> LedgerResult<R>.intoLedger(): LedgerResult<T> =
    when (this) {
        is LedgerResult.Success ->
            LedgerResult.QueryFailure("Can't auto-convert between different typed Ledger results")
        is LedgerResult.QueryFailure ->
            LedgerResult.QueryFailure(
                cause, exception
            )
        is LedgerResult.NonMatchingCrypter ->
            LedgerResult.NonMatchingCrypter(cause)
        is LedgerResult.UnregisteredCrypter ->
            LedgerResult.UnregisteredCrypter(cause)
        is LedgerResult.NonExistentData ->
            LedgerResult.NonExistentData(cause)
    }


inline fun <R : LedgerContract, T : LedgerContract> LoadResult<R>.intoLoad(
    success: R.() -> T
): LoadResult<T> =
    when (this) {
        is LoadResult.Success ->
            LoadResult.Success(data.success())
        is LoadResult.QueryFailure ->
            LoadResult.QueryFailure(cause, exception)
        is LoadResult.NonMatchingCrypter ->
            LoadResult.NonMatchingCrypter(cause)
        is LoadResult.UnregisteredCrypter ->
            LoadResult.UnregisteredCrypter(cause)
        is LoadResult.UnrecognizedDataType ->
            LoadResult.UnrecognizedDataType(cause)
        is LoadResult.NonExistentData ->
            LoadResult.NonExistentData(cause)
    }


fun <R : LedgerContract, T : LedgerContract> LoadResult<R>.intoLoad(): LoadResult<T> =
    when (this) {
        is LoadResult.Success ->
            LoadResult.QueryFailure("Can't auto-convert between different typed Load results")
        is LoadResult.QueryFailure ->
            LoadResult.QueryFailure(cause, exception)
        is LoadResult.NonMatchingCrypter ->
            LoadResult.NonMatchingCrypter(cause)
        is LoadResult.UnregisteredCrypter ->
            LoadResult.UnregisteredCrypter(cause)
        is LoadResult.UnrecognizedDataType ->
            LoadResult.UnrecognizedDataType(cause)
        is LoadResult.NonExistentData ->
            LoadResult.NonExistentData(cause)
    }


inline fun <R : BlockChainData, T : LedgerContract> DataResult<R>.intoLoad(
    success: R.() -> T
): LoadResult<T> =
    when (this) {
        is DataResult.Success ->
            LoadResult.Success(data.success())
        is DataResult.QueryFailure ->
            LoadResult.QueryFailure(cause, exception)
        is DataResult.UnrecognizedDataType ->
            LoadResult.UnrecognizedDataType(cause)
        is DataResult.UnrecognizedUnit ->
            LoadResult.QueryFailure(this.cause)
        is DataResult.UnexpectedClass ->
            LoadResult.QueryFailure(this.cause)
        is DataResult.NonRegisteredSchema ->
            LoadResult.QueryFailure(this.cause)
        is DataResult.NonExistentData ->
            LoadResult.NonExistentData(cause)
    }

fun <R : BlockChainData, T : LedgerContract> DataResult<R>.intoLoad(): LoadResult<T> =
    when (this) {
        is DataResult.Success ->
            LoadResult.QueryFailure("DataResult can't auto-convert into LoadResult.")
        is DataResult.QueryFailure ->
            LoadResult.QueryFailure(cause, exception)
        is DataResult.UnrecognizedDataType ->
            LoadResult.UnrecognizedDataType(cause)
        is DataResult.UnrecognizedUnit ->
            LoadResult.QueryFailure(this.cause)
        is DataResult.UnexpectedClass ->
            LoadResult.QueryFailure(this.cause)
        is DataResult.NonRegisteredSchema ->
            LoadResult.QueryFailure(this.cause)
        is DataResult.NonExistentData ->
            LoadResult.NonExistentData(cause)
    }


fun <R : LedgerContract, T : LedgerContract> LoadResult<R>.intoList(): LoadListResult<T> =
    when (this) {
        is LoadResult.Success ->
            //This should never ever happen.
            LoadListResult.QueryFailure("DataResult can't auto-convert into ListResult.")
        is LoadResult.QueryFailure ->
            LoadListResult.QueryFailure(cause, exception)
        is LoadResult.NonMatchingCrypter ->
            LoadListResult.NonMatchingCrypter(cause)
        is LoadResult.UnregisteredCrypter ->
            LoadListResult.UnregisteredCrypter(cause)
        is LoadResult.UnrecognizedDataType ->
            LoadListResult.UnrecognizedDataType(cause)
        is LoadResult.NonExistentData ->
            LoadListResult.NonExistentData(cause)
    }


fun <R : LedgerContract, T : LedgerContract> LoadListResult<R>.intoLoad(): LoadResult<T> =
    when (this) {
        is LoadListResult.Success ->
            //This should never ever happen.
            LoadResult.QueryFailure("ListResult can't auto-convert into DataResult.")
        is LoadListResult.QueryFailure ->
            LoadResult.QueryFailure(cause, exception)
        is LoadListResult.NonMatchingCrypter ->
            LoadResult.NonMatchingCrypter(cause)
        is LoadListResult.UnregisteredCrypter ->
            LoadResult.UnregisteredCrypter(cause)
        is LoadListResult.UnrecognizedDataType ->
            LoadResult.UnrecognizedDataType(cause)
        is LoadListResult.NonExistentData ->
            LoadResult.NonExistentData(cause)
    }

inline fun <R : BlockChainData, T : BlockChainData> DataListResult<R>.intoData(
    reduce: List<R>.() -> T
): DataResult<T> =
    when (this) {
        is DataListResult.Success ->
            DataResult.Success(data.reduce())
        is DataListResult.QueryFailure ->
            DataResult.QueryFailure(cause, exception)
        is DataListResult.UnrecognizedDataType ->
            DataResult.UnrecognizedDataType(cause)
        is DataListResult.UnrecognizedUnit ->
            DataResult.UnrecognizedDataType(cause)
        is DataListResult.UnexpectedClass ->
            DataResult.UnexpectedClass(cause)
        is DataListResult.NonRegisteredSchema ->
            DataResult.NonRegisteredSchema(cause)
        is DataListResult.NonExistentData ->
            DataResult.NonExistentData(cause)
    }

fun <R : BlockChainData, T : BlockChainData> DataListResult<R>.intoData(): DataResult<T> =
    when (this) {
        is DataListResult.Success ->
            //This should never ever happen.
            DataResult.QueryFailure("ListResult can't auto-convert into DataResult.")
        is DataListResult.QueryFailure ->
            DataResult.QueryFailure(cause, exception)
        is DataListResult.UnrecognizedDataType ->
            DataResult.UnrecognizedDataType(cause)
        is DataListResult.UnrecognizedUnit ->
            DataResult.UnrecognizedDataType(cause)
        is DataListResult.UnexpectedClass ->
            DataResult.UnexpectedClass(cause)
        is DataListResult.NonRegisteredSchema ->
            DataResult.NonRegisteredSchema(cause)
        is DataListResult.NonExistentData ->
            DataResult.NonExistentData(cause)
    }


fun <R : BlockChainData, T : BlockChainData> DataResult<R>.intoList(
    reduce: R.() -> List<T>
): DataListResult<T> =
    when (this) {
        is DataResult.Success ->
            DataListResult.Success(data.reduce())
        is DataResult.QueryFailure ->
            DataListResult.QueryFailure(cause, exception)
        is DataResult.UnrecognizedDataType ->
            DataListResult.UnrecognizedDataType(cause)
        is DataResult.UnrecognizedUnit ->
            DataListResult.UnrecognizedUnit(cause)
        is DataResult.UnexpectedClass ->
            DataListResult.UnexpectedClass(cause)
        is DataResult.NonRegisteredSchema ->
            DataListResult.NonRegisteredSchema(cause)
        is DataResult.NonExistentData ->
            DataListResult.NonExistentData(cause)
    }


fun <R : BlockChainData, T : BlockChainData> DataResult<R>.intoList(): DataListResult<T> =
    when (this) {
        is DataResult.Success ->
            //This should never ever happen.
            DataListResult.QueryFailure("DataResult can't auto-convert into DataListResult.")
        is DataResult.QueryFailure ->
            DataListResult.QueryFailure(cause, exception)
        is DataResult.UnrecognizedDataType ->
            DataListResult.UnrecognizedDataType(cause)
        is DataResult.UnrecognizedUnit ->
            DataListResult.UnrecognizedUnit(cause)
        is DataResult.UnexpectedClass ->
            DataListResult.UnexpectedClass(cause)
        is DataResult.NonRegisteredSchema ->
            DataListResult.NonRegisteredSchema(cause)
        is DataResult.NonExistentData ->
            DataListResult.NonExistentData(cause)
    }


fun <T : LedgerContract> Sequence<LoadResult<T>>.collapse(): LoadListResult<T> {
    val accumulator: MutableList<T> = mutableListOf()
    var short = false
    lateinit var shorter: LoadResult<T>
    for (shorting in this) {
        if (shorting !is LoadResult.Success) {
            shorter = shorting
            short = true
            break
        } else {
            accumulator += shorting.data
        }
    }
    return if (short) {
        shorter.intoList()
    } else {
        LoadListResult.Success(
            accumulator
        )
    }
}


fun <T : BlockChainData> Sequence<DataResult<T>>.collapse(): DataListResult<T> {
    val accumulator: MutableList<T> = mutableListOf()
    var short = false
    lateinit var shorter: DataResult<T>
    for (shorting in this) {
        if (shorting !is DataResult.Success) {
            shorter = shorting
            short = true
            break
        } else {
            accumulator += shorting.data
        }
    }
    return if (short) {
        shorter.intoList()
    } else {
        DataListResult.Success(
            accumulator
        )
    }
}

inline fun <T : BlockChainData> PersistResult.intoData(
    success: OElement.() -> T
): DataResult<T> =
    when (this) {
        is PersistResult.Success ->
            DataResult.Success(data.success())
        is PersistResult.QueryFailure ->
            DataResult.QueryFailure(cause, exception)
        is PersistResult.NonExistentData ->
            DataResult.NonExistentData(cause)
        is PersistResult.UnrecognizedDataType ->
            DataResult.UnrecognizedDataType(cause)
        is PersistResult.UnrecognizedUnit ->
            DataResult.UnrecognizedUnit(cause)
        is PersistResult.UnexpectedClass ->
            DataResult.UnexpectedClass(cause)
        is PersistResult.NonRegisteredSchema ->
            DataResult.QueryFailure(cause)
        is PersistResult.NonMatchingCrypter ->
            DataResult.QueryFailure(cause)
        is PersistResult.UnregisteredCrypter ->
            DataResult.QueryFailure(cause)
    }

fun <T : BlockChainData> PersistResult.intoData(): DataResult<T> =
    when (this) {
        is PersistResult.Success ->
            DataResult.QueryFailure("PersistResult can't auto-convert into DataResult.")
        is PersistResult.QueryFailure ->
            DataResult.QueryFailure(cause, exception)
        is PersistResult.NonExistentData ->
            DataResult.NonExistentData(cause)
        is PersistResult.UnrecognizedDataType ->
            DataResult.UnrecognizedDataType(cause)
        is PersistResult.UnrecognizedUnit ->
            DataResult.UnrecognizedUnit(cause)
        is PersistResult.UnexpectedClass ->
            DataResult.UnexpectedClass(cause)
        is PersistResult.NonRegisteredSchema ->
            DataResult.NonRegisteredSchema(cause)
        is PersistResult.NonMatchingCrypter ->
            DataResult.QueryFailure(cause)
        is PersistResult.UnregisteredCrypter ->
            DataResult.QueryFailure(cause)
    }

inline fun <T : LedgerContract> PersistResult.intoLoad(
    success: OElement.() -> T
): LoadResult<T> =
    when (this) {
        is PersistResult.Success ->
            LoadResult.Success(data.success())
        is PersistResult.QueryFailure ->
            LoadResult.QueryFailure(cause, exception)
        is PersistResult.NonExistentData ->
            LoadResult.NonExistentData(cause)
        is PersistResult.UnrecognizedDataType ->
            LoadResult.UnrecognizedDataType(cause)
        is PersistResult.UnrecognizedUnit ->
            LoadResult.QueryFailure(cause)
        is PersistResult.UnexpectedClass ->
            LoadResult.QueryFailure(cause)
        is PersistResult.NonRegisteredSchema ->
            LoadResult.QueryFailure(cause)
        is PersistResult.NonMatchingCrypter ->
            LoadResult.QueryFailure(cause)
        is PersistResult.UnregisteredCrypter ->
            LoadResult.QueryFailure(cause)
    }

fun <T : LedgerContract> PersistResult.intoLoad(): LoadResult<T> =
    when (this) {
        is PersistResult.Success ->
            LoadResult.QueryFailure("PersistResult can't auto-convert into LoadResult.")
        is PersistResult.QueryFailure ->
            LoadResult.QueryFailure(cause, exception)
        is PersistResult.NonExistentData ->
            LoadResult.NonExistentData(cause)
        is PersistResult.UnrecognizedDataType ->
            LoadResult.UnrecognizedDataType(cause)
        is PersistResult.UnrecognizedUnit ->
            LoadResult.QueryFailure(cause)
        is PersistResult.UnexpectedClass ->
            LoadResult.QueryFailure(cause)
        is PersistResult.NonRegisteredSchema ->
            LoadResult.QueryFailure(cause)
        is PersistResult.NonMatchingCrypter ->
            LoadResult.QueryFailure(cause)
        is PersistResult.UnregisteredCrypter ->
            LoadResult.QueryFailure(cause)
    }


inline fun <T : ServiceHandle> PersistResult.intoLedger(
    success: OElement.() -> T
): LedgerResult<T> =
    when (this) {
        is PersistResult.Success ->
            LedgerResult.Success(data.success())
        is PersistResult.QueryFailure ->
            LedgerResult.QueryFailure(cause, exception)
        is PersistResult.NonExistentData ->
            LedgerResult.QueryFailure(cause)
        is PersistResult.UnrecognizedDataType ->
            LedgerResult.QueryFailure(cause)
        is PersistResult.UnrecognizedUnit ->
            LedgerResult.QueryFailure(cause)
        is PersistResult.UnexpectedClass ->
            LedgerResult.QueryFailure(cause)
        is PersistResult.NonRegisteredSchema ->
            LedgerResult.QueryFailure(cause)
        is PersistResult.NonMatchingCrypter ->
            LedgerResult.NonMatchingCrypter(cause)
        is PersistResult.UnregisteredCrypter ->
            LedgerResult.UnregisteredCrypter(cause)
    }

fun <T : ServiceHandle> PersistResult.intoLedger(): LedgerResult<T> =
    when (this) {
        is PersistResult.Success ->
            LedgerResult.QueryFailure("PersistResult can't auto-convert into LedgerResult.")
        is PersistResult.QueryFailure ->
            LedgerResult.QueryFailure(cause, exception)
        is PersistResult.NonExistentData ->
            LedgerResult.QueryFailure(cause)
        is PersistResult.UnrecognizedDataType ->
            LedgerResult.QueryFailure(cause)
        is PersistResult.UnrecognizedUnit ->
            LedgerResult.QueryFailure(cause)
        is PersistResult.UnexpectedClass ->
            LedgerResult.QueryFailure(cause)
        is PersistResult.NonRegisteredSchema ->
            LedgerResult.QueryFailure(cause)
        is PersistResult.NonMatchingCrypter ->
            LedgerResult.NonMatchingCrypter(cause)
        is PersistResult.UnregisteredCrypter ->
            LedgerResult.UnregisteredCrypter(cause)
    }

inline fun <T : ServiceHandle> tryOrLedgerQueryFailure(
    run: () -> LedgerResult<T>
): LedgerResult<T> =
    try {
        run()
    } catch (e: Exception) {
        LedgerResult.QueryFailure(
            e.message ?: "", e
        )
    }

inline fun <T : LedgerContract> tryOrLoadQueryFailure(
    run: () -> LoadResult<T>
): LoadResult<T> =
    try {
        run()
    } catch (e: Exception) {
        LoadResult.QueryFailure(
            e.message ?: "", e
        )
    }

inline fun <T : LedgerContract> tryOrLoadListQueryFailure(
    run: () -> LoadListResult<T>
): LoadListResult<T> =
    try {
        run()
    } catch (e: Exception) {
        LoadListResult.QueryFailure(
            e.message ?: "", e
        )
    }

inline fun <T : BlockChainData> tryOrDataQueryFailure(
    run: () -> DataResult<T>
): DataResult<T> =
    try {
        run()
    } catch (e: Exception) {
        DataResult.QueryFailure(
            e.message ?: "", e
        )
    }

inline fun <T : BlockChainData> tryOrDataListQueryFailure(
    run: () -> DataListResult<T>
): DataListResult<T> =
    try {
        run()
    } catch (e: Exception) {
        DataListResult.QueryFailure(
            e.message ?: "", e
        )
    }

inline fun tryOrPersistQueryFailure(
    run: () -> PersistResult
): PersistResult =
    try {
        run()
    } catch (e: Exception) {
        PersistResult.QueryFailure(
            e.message ?: "", e
        )
    }


fun deadCode(): Nothing {
    throw RuntimeException("Dead code invoked")
}