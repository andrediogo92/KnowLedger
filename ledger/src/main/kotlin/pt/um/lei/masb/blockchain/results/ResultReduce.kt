package pt.um.lei.masb.blockchain.results

import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.ledger.LedgerContract
import pt.um.lei.masb.blockchain.persistance.results.DataListResult
import pt.um.lei.masb.blockchain.persistance.results.DataResult
import pt.um.lei.masb.blockchain.persistance.results.QueryResult
import pt.um.lei.masb.blockchain.service.ServiceHandle
import pt.um.lei.masb.blockchain.service.results.LedgerListResult
import pt.um.lei.masb.blockchain.service.results.LedgerResult
import pt.um.lei.masb.blockchain.service.results.LoadListResult
import pt.um.lei.masb.blockchain.service.results.LoadResult


//---------------------------------------
// Into List Result.
//---------------------------------------


inline fun <R : BlockChainData, T : BlockChainData> DataResult<R>.intoList(
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
        is DataResult.Propagated -> DataListResult.Propagated(
            "DataResult -> $pointOfFailure", failable
        )
    }


fun <R : BlockChainData, T : BlockChainData> DataResult<R>.intoList(): DataListResult<T> =
    when (this) {
        //This should never ever happen.
        is DataResult.Success -> DataListResult.QueryFailure(
            "DataResult can't auto-convert into DataListResult."
        )
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
        is DataResult.Propagated -> DataListResult.Propagated(
            "DataResult -> $pointOfFailure", failable
        )
    }


inline fun <R : LedgerContract, T : LedgerContract> LoadResult<R>.intoList(
    reduce: R.() -> List<T>
): LoadListResult<T> =
    when (this) {
        is LoadResult.Success ->
            LoadListResult.Success(data.reduce())
        is LoadResult.QueryFailure ->
            LoadListResult.QueryFailure(cause, exception)
        is LoadResult.NonMatchingCrypter ->
            LoadListResult.NonMatchingCrypter(cause)
        is LoadResult.NonExistentData ->
            LoadListResult.NonExistentData(cause)
        is LoadResult.UnrecognizedDataType ->
            LoadListResult.UnrecognizedDataType(cause)
        is LoadResult.Propagated -> LoadListResult.Propagated(
            "LoadResult -> $pointOfFailure", failable
        )
    }


fun <R : LedgerContract, T : LedgerContract> LoadResult<R>.intoList(): LoadListResult<T> =
    when (this) {
        //This should never ever happen.
        is LoadResult.Success -> LoadListResult.QueryFailure(
            "DataResult can't auto-convert into ListResult."
        )
        is LoadResult.QueryFailure ->
            LoadListResult.QueryFailure(cause, exception)
        is LoadResult.NonMatchingCrypter ->
            LoadListResult.NonMatchingCrypter(cause)
        is LoadResult.NonExistentData ->
            LoadListResult.NonExistentData(cause)
        is LoadResult.UnrecognizedDataType ->
            LoadListResult.UnrecognizedDataType(cause)
        is LoadResult.Propagated -> LoadListResult.Propagated(
            "LoadResult -> $pointOfFailure", failable
        )
    }


inline fun <R : ServiceHandle, T : ServiceHandle> LedgerResult<R>.intoList(
    reduce: R.() -> List<T>
): LedgerListResult<T> =
    when (this) {
        is LedgerResult.Success ->
            LedgerListResult.Success(data.reduce())
        is LedgerResult.QueryFailure ->
            LedgerListResult.QueryFailure(cause, exception)
        is LedgerResult.NonExistentData -> LedgerListResult.NonExistentData(cause)
        is LedgerResult.NonMatchingCrypter -> LedgerListResult.NonMatchingCrypter(cause)
        is LedgerResult.Propagated -> LedgerListResult.Propagated(
            "LedgerResult -> $pointOfFailure", failable
        )
    }


fun <R : ServiceHandle, T : ServiceHandle> LedgerResult<R>.intoList(): LedgerListResult<T> =
    when (this) {
        //This should never ever happen.
        is LedgerResult.Success -> LedgerListResult.QueryFailure(
            "LedgerResult can't auto-convert into LedgerListResult"
        )
        is LedgerResult.QueryFailure ->
            LedgerListResult.QueryFailure(cause, exception)
        is LedgerResult.NonExistentData -> LedgerListResult.NonExistentData(cause)
        is LedgerResult.NonMatchingCrypter -> LedgerListResult.NonMatchingCrypter(cause)
        is LedgerResult.Propagated -> LedgerListResult.Propagated(
            "LedgerResult -> $pointOfFailure", failable
        )
    }


inline fun <T : Any> QueryResult<T>.intoList(
    reduce: T.() -> List<T>
): QueryResult<List<T>> =
    when (this) {
        is QueryResult.Success ->
            QueryResult.Success(data.reduce())
        is QueryResult.QueryFailure ->
            QueryResult.QueryFailure(cause, exception)
        is QueryResult.NonExistentData ->
            QueryResult.NonExistentData(cause)
        is QueryResult.Propagated -> QueryResult.Propagated(
            pointOfFailure, failable
        )
    }


fun <T : Any> QueryResult<T>.intoList(): QueryResult<List<T>> =
    when (this) {
        //This should never ever happen.
        is QueryResult.Success -> QueryResult.QueryFailure(
            "QueryResult can't auto-convert into a QueryResult of Lists."
        )
        is QueryResult.QueryFailure ->
            QueryResult.QueryFailure(cause, exception)
        is QueryResult.NonExistentData ->
            QueryResult.NonExistentData(cause)
        is QueryResult.Propagated -> QueryResult.Propagated(
            pointOfFailure, failable
        )
    }


// ---------------------------------------
// List Reductions
// ---------------------------------------


fun <T : ServiceHandle> Sequence<LedgerResult<T>>.collapse(): LedgerListResult<T> {
    val accumulator: MutableList<T> = mutableListOf()
    var short = false
    var shorter: LedgerResult<T> =
        LedgerResult.NonExistentData("Input Sequence empty")
    for (shorting in this) {
        if (shorting !is LedgerResult.Success) {
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
        LedgerListResult.Success(
            accumulator
        )
    }
}


fun <T : LedgerContract> Sequence<LoadResult<T>>.collapse(): LoadListResult<T> {
    val accumulator: MutableList<T> = mutableListOf()
    var short = false
    var shorter: LoadResult<T> =
        LoadResult.NonExistentData("Input Sequence empty")
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
    var shorter: DataResult<T> =
        DataResult.NonExistentData("Input Sequence empty")
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

fun <T : Any> Sequence<QueryResult<T>>.collapse(): QueryResult<List<T>> {
    val accumulator: MutableList<T> = mutableListOf()
    var short = false
    var shorter: QueryResult<T> =
        QueryResult.NonExistentData("Input Sequence empty")
    for (shorting in this) {
        if (shorting !is QueryResult.Success) {
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
        QueryResult.Success(
            accumulator
        )
    }
}

fun <T : Any, R : Any> QueryResult<R>.collapse(
    success: R.() -> T,
    failure: () -> T
): T =
    when (this) {
        is QueryResult.Success -> data.success()
        is QueryResult.QueryFailure -> failure()
        is QueryResult.NonExistentData -> failure()
        is QueryResult.Propagated -> failure()
    }


//-----------------------------------------
// Exception Handlers
//-----------------------------------------


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


inline fun <T : ServiceHandle> tryOrLedgerListQueryFailure(
    run: () -> LedgerListResult<T>
): LedgerListResult<T> =
    try {
        run()
    } catch (e: Exception) {
        LedgerListResult.QueryFailure(
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

inline fun <T : Any> tryOrQueryQueryFailure(
    run: () -> QueryResult<T>
): QueryResult<T> =
    try {
        run()
    } catch (e: Exception) {
        QueryResult.QueryFailure(
            e.message ?: "", e
        )
    }


fun deadCode(): Nothing {
    throw RuntimeException("Dead code invoked")
}

fun <T : Any> T.checkSealed() {}