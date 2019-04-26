package pt.um.lei.masb.blockchain.results

import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.ledger.LedgerContract
import pt.um.lei.masb.blockchain.persistance.results.DataListResult
import pt.um.lei.masb.blockchain.persistance.results.DataResult
import pt.um.lei.masb.blockchain.persistance.results.QueryResult
import pt.um.lei.masb.blockchain.service.ServiceHandle
import pt.um.lei.masb.blockchain.service.results.LedgerResult
import pt.um.lei.masb.blockchain.service.results.LoadListResult
import pt.um.lei.masb.blockchain.service.results.LoadResult

//---------------------------------------
//Into Ledger Result
//---------------------------------------


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
        is LoadResult.UnrecognizedDataType -> LedgerResult.Propagated(
            "LoadResult", this
        )
        is LoadResult.NonExistentData ->
            LedgerResult.NonExistentData(cause)
        is LoadResult.Propagated -> LedgerResult.Propagated(
            "LoadResult -> $pointOfFailure", failable
        )
    }

fun <T : ServiceHandle> LoadResult<out LedgerContract>.intoLedger(): LedgerResult<T> =
    when (this) {
        //This should never ever happen.
        is LoadResult.Success -> LedgerResult.QueryFailure(
            "LoadResult can't auto-convert into LedgerResult."
        )
        is LoadResult.QueryFailure ->
            LedgerResult.QueryFailure(cause, exception)
        is LoadResult.NonMatchingCrypter ->
            LedgerResult.NonMatchingCrypter(cause)
        is LoadResult.NonExistentData ->
            LedgerResult.NonExistentData(cause)
        is LoadResult.UnrecognizedDataType -> LedgerResult.Propagated(
            "LoadResult", this
        )
        is LoadResult.Propagated -> LedgerResult.Propagated(
            "LoadResult -> $pointOfFailure", failable
        )
    }

inline fun <R : ServiceHandle, T : ServiceHandle> LedgerResult<R>.intoLedger(
    success: R.() -> T
): LedgerResult<T> =
    when (this) {
        is LedgerResult.Success ->
            LedgerResult.Success(data.success())
        is LedgerResult.QueryFailure ->
            LedgerResult.QueryFailure(cause, exception)
        is LedgerResult.NonMatchingCrypter ->
            LedgerResult.NonMatchingCrypter(cause)
        is LedgerResult.NonExistentData ->
            LedgerResult.NonExistentData(cause)
        is LedgerResult.Propagated -> LedgerResult.Propagated(
            pointOfFailure, failable
        )
    }

fun <T : ServiceHandle> LedgerResult<out ServiceHandle>.intoLedger(): LedgerResult<T> =
    when (this) {
        //This should never ever happen.
        is LedgerResult.Success -> LedgerResult.QueryFailure(
            "Can't auto-convert between different typed Ledger results"
        )
        is LedgerResult.QueryFailure ->
            LedgerResult.QueryFailure(cause, exception)
        is LedgerResult.NonMatchingCrypter ->
            LedgerResult.NonMatchingCrypter(cause)
        is LedgerResult.NonExistentData ->
            LedgerResult.NonExistentData(cause)
        is LedgerResult.Propagated -> LedgerResult.Propagated(
            pointOfFailure, failable
        )
    }


inline fun <T : ServiceHandle, R : Any> QueryResult<R>.intoLedger(
    success: R.() -> T
): LedgerResult<T> =
    when (this) {
        is QueryResult.Success ->
            LedgerResult.Success(data.success())
        is QueryResult.QueryFailure ->
            LedgerResult.QueryFailure(cause, exception)
        is QueryResult.NonExistentData ->
            LedgerResult.NonExistentData(cause)
        is QueryResult.Propagated -> LedgerResult.Propagated(
            "QueryResult -> $pointOfFailure", failable
        )
    }

fun <R : Any> QueryResult<R>.intoLedger(): LedgerResult<out ServiceHandle> =
    when (this) {
        //This should never ever happen.
        is QueryResult.Success -> LedgerResult.QueryFailure(
            "QueryResult can't auto-convert into LedgerResult."
        )
        is QueryResult.QueryFailure ->
            LedgerResult.QueryFailure(cause, exception)
        is QueryResult.NonExistentData ->
            LedgerResult.NonExistentData(cause)
        is QueryResult.Propagated -> LedgerResult.Propagated(
            "QueryResult -> $pointOfFailure", failable
        )
    }


//-----------------------------------------
// Into Load Result
//-----------------------------------------


inline fun <T : LedgerContract> DataResult<out BlockChainData>.intoLoad(
    success: BlockChainData.() -> T
): LoadResult<T> =
    when (this) {
        is DataResult.Success ->
            LoadResult.Success(data.success())
        is DataResult.QueryFailure ->
            LoadResult.QueryFailure(cause, exception)
        is DataResult.UnrecognizedDataType ->
            LoadResult.Propagated(
                "DataResult", this
            )
        is DataResult.UnrecognizedUnit ->
            LoadResult.Propagated(
                "DataResult", this
            )
        is DataResult.UnexpectedClass ->
            LoadResult.Propagated(
                "DataResult", this
            )
        is DataResult.NonRegisteredSchema ->
            LoadResult.Propagated(
                "DataResult", this
            )
        is DataResult.NonExistentData ->
            LoadResult.NonExistentData(cause)
        is DataResult.Propagated -> LoadResult.Propagated(
            "DataResult -> $pointOfFailure", failable
        )
    }

fun <T : LedgerContract> DataResult<out BlockChainData>.intoLoad(): LoadResult<T> =
    when (this) {
        //This should never ever happen.
        is DataResult.Success -> LoadResult.QueryFailure(
            "DataResult can't auto-convert into LoadResult."
        )
        is DataResult.QueryFailure ->
            LoadResult.QueryFailure(cause, exception)
        is DataResult.UnrecognizedDataType ->
            LoadResult.Propagated(
                "DataResult", this
            )
        is DataResult.UnrecognizedUnit ->
            LoadResult.Propagated(
                "DataResult", this
            )
        is DataResult.UnexpectedClass ->
            LoadResult.Propagated(
                "DataResult", this
            )
        is DataResult.NonRegisteredSchema ->
            LoadResult.Propagated(
                "DataResult", this
            )
        is DataResult.NonExistentData ->
            LoadResult.NonExistentData(cause)
        is DataResult.Propagated -> LoadResult.Propagated(
            "DataResult -> $pointOfFailure", failable
        )
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
        is LoadResult.NonExistentData ->
            LoadResult.NonExistentData(cause)
        is LoadResult.Propagated -> LoadResult.Propagated(
            pointOfFailure, failable
        )
        is LoadResult.UnrecognizedDataType ->
            LoadResult.UnrecognizedDataType(cause)
    }


fun <T : LedgerContract> LoadResult<out LedgerContract>.intoLoad(): LoadResult<T> =
    when (this) {
        //This should never ever happen.
        is LoadResult.Success -> LoadResult.QueryFailure(
            "Can't auto-convert between different typed Load results"
        )
        is LoadResult.QueryFailure ->
            LoadResult.QueryFailure(cause, exception)
        is LoadResult.NonMatchingCrypter ->
            LoadResult.NonMatchingCrypter(cause)
        is LoadResult.NonExistentData ->
            LoadResult.NonExistentData(cause)
        is LoadResult.UnrecognizedDataType ->
            LoadResult.UnrecognizedDataType(cause)
        is LoadResult.Propagated -> LoadResult.Propagated(
            pointOfFailure, failable
        )
    }


fun <T : LedgerContract> LoadListResult<out LedgerContract>.intoLoad(): LoadResult<T> =
    when (this) {
        //This should never ever happen.
        is LoadListResult.Success -> LoadResult.QueryFailure(
            "ListResult can't auto-convert into DataResult."
        )
        is LoadListResult.QueryFailure ->
            LoadResult.QueryFailure(cause, exception)
        is LoadListResult.NonMatchingCrypter ->
            LoadResult.NonMatchingCrypter(cause)
        is LoadListResult.UnrecognizedDataType -> LoadResult.Propagated(
            "DataResult", this
        )
        is LoadListResult.NonExistentData ->
            LoadResult.NonExistentData(cause)
        is LoadListResult.Propagated -> LoadResult.Propagated(
            "LoadResult -> $pointOfFailure", failable
        )
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
        is LedgerResult.NonExistentData -> LoadResult.Propagated(
            "LedgerResult", this
        )
        is LedgerResult.Propagated -> LoadResult.Propagated(
            "LedgerResult -> $pointOfFailure", failable
        )
    }

fun <T : LedgerContract> LedgerResult<out ServiceHandle>.intoLoad(): LoadResult<T> =
    when (this) {
        is LedgerResult.Success -> LoadResult.QueryFailure(
            "LedgerResult can't auto-convert into LoadResult."
        )
        is LedgerResult.QueryFailure ->
            LoadResult.QueryFailure(cause, exception)
        is LedgerResult.NonMatchingCrypter ->
            LoadResult.NonMatchingCrypter(cause)
        is LedgerResult.NonExistentData ->
            LoadResult.NonExistentData(cause)
        is LedgerResult.Propagated -> LoadResult.Propagated(
            "LedgerResult -> $pointOfFailure", failable
        )
    }


inline fun <T : LedgerContract, R : Any> QueryResult<R>.intoLoad(
    success: R.() -> T
): LoadResult<T> =
    when (this) {
        is QueryResult.Success ->
            LoadResult.Success(data.success())
        is QueryResult.QueryFailure ->
            LoadResult.QueryFailure(cause, exception)
        is QueryResult.NonExistentData ->
            LoadResult.NonExistentData(cause)
        is QueryResult.Propagated -> LoadResult.Propagated(
            "QueryResult -> $pointOfFailure", failable
        )
    }

fun <R : Any> QueryResult<R>.intoLoad(): LoadResult<out LedgerContract> =
    when (this) {
        //This should never ever happen.
        is QueryResult.Success -> LoadResult.QueryFailure(
            "QueryResult can't auto-convert into LoadResult."
        )
        is QueryResult.QueryFailure ->
            LoadResult.QueryFailure(cause, exception)
        is QueryResult.NonExistentData ->
            LoadResult.NonExistentData(cause)
        is QueryResult.Propagated -> LoadResult.Propagated(
            "QueryResult -> $pointOfFailure", failable
        )
    }


//-------------------------------------
// Into Query Result
//-------------------------------------


inline fun <R : LedgerContract, T : Any> LoadResult<R>.intoQuery(
    success: R.() -> T
): QueryResult<T> =
    when (this) {
        is LoadResult.Success ->
            QueryResult.Success(data.success())
        is LoadResult.QueryFailure ->
            QueryResult.QueryFailure(cause, exception)
        is LoadResult.NonExistentData ->
            QueryResult.NonExistentData(cause)
        is LoadResult.UnrecognizedDataType -> QueryResult.Propagated(
            "LoadResult", this
        )
        is LoadResult.NonMatchingCrypter -> QueryResult.Propagated(
            "LoadResult", this
        )
        is LoadResult.Propagated -> QueryResult.Propagated(
            "LoadResult -> $pointOfFailure", failable
        )
    }


fun <T : Any> LoadListResult<out LedgerContract>.intoQuery(): QueryResult<T> =
    when (this) {
        //This should never happen
        is LoadListResult.Success -> QueryResult.QueryFailure(
            "LoadListResult can't auto-convert into QueryResult."
        )
        is LoadListResult.QueryFailure ->
            QueryResult.QueryFailure(cause, exception)
        is LoadListResult.NonExistentData ->
            QueryResult.NonExistentData(cause)
        is LoadListResult.NonMatchingCrypter -> QueryResult.Propagated(
            "LoadListResult", this
        )
        is LoadListResult.UnrecognizedDataType -> QueryResult.Propagated(
            "LoadListResult", this
        )
        is LoadListResult.Propagated -> QueryResult.Propagated(
            "LoadResult -> $pointOfFailure", failable
        )
    }


inline fun <R : ServiceHandle, T : Any> LedgerResult<R>.intoQuery(
    success: R.() -> T
): QueryResult<T> =
    when (this) {
        is LedgerResult.Success ->
            QueryResult.Success(data.success())
        is LedgerResult.QueryFailure ->
            QueryResult.QueryFailure(cause, exception)
        is LedgerResult.NonExistentData -> QueryResult.NonExistentData(cause)
        is LedgerResult.NonMatchingCrypter -> QueryResult.Propagated(
            "LedgerResult", this
        )
        is LedgerResult.Propagated -> QueryResult.Propagated(
            "LedgerResult -> $pointOfFailure", failable
        )
    }

fun <T : Any, R : Any> QueryResult<R>.intoQuery(
    transform: R.() -> T
): QueryResult<T> =
    when (this) {
        is QueryResult.Success ->
            QueryResult.Success(data.transform())
        is QueryResult.QueryFailure ->
            QueryResult.QueryFailure(cause, exception)
        is QueryResult.NonExistentData ->
            QueryResult.QueryFailure(cause)
        is QueryResult.Propagated -> QueryResult.Propagated(
            pointOfFailure, failable
        )
    }


//---------------------------------------
// Into Data Result
//---------------------------------------


inline fun <T : BlockChainData> DataListResult<out BlockChainData>.intoData(
    reduce: List<BlockChainData>.() -> T
): DataResult<T> =
    when (this) {
        is DataListResult.Success ->
            DataResult.Success(data.reduce())
        is DataListResult.QueryFailure ->
            DataResult.QueryFailure(cause, exception)
        is DataListResult.UnrecognizedDataType ->
            DataResult.UnrecognizedDataType(cause)
        is DataListResult.UnrecognizedUnit ->
            DataResult.UnrecognizedUnit(cause)
        is DataListResult.UnexpectedClass ->
            DataResult.UnexpectedClass(cause)
        is DataListResult.NonRegisteredSchema ->
            DataResult.NonRegisteredSchema(cause)
        is DataListResult.NonExistentData ->
            DataResult.NonExistentData(cause)
        is DataListResult.Propagated -> DataResult.Propagated(
            "DataListResult ->", failable
        )
    }

fun <T : BlockChainData> DataListResult<out BlockChainData>.intoData(): DataResult<T> =
    when (this) {
        //This should never ever happen.
        is DataListResult.Success -> DataResult.QueryFailure(
            "ListResult can't auto-convert into DataResult."
        )
        is DataListResult.QueryFailure ->
            DataResult.QueryFailure(cause, exception)
        is DataListResult.UnrecognizedDataType ->
            DataResult.UnrecognizedDataType(cause)
        is DataListResult.UnrecognizedUnit ->
            DataResult.UnrecognizedUnit(cause)
        is DataListResult.UnexpectedClass ->
            DataResult.UnexpectedClass(cause)
        is DataListResult.NonRegisteredSchema ->
            DataResult.NonRegisteredSchema(cause)
        is DataListResult.NonExistentData ->
            DataResult.NonExistentData(cause)
        is DataListResult.Propagated -> TODO()
    }


inline fun <R : Any> QueryResult<R>.intoData(
    success: R.() -> BlockChainData
): DataResult<out BlockChainData> =
    when (this) {
        is QueryResult.Success ->
            DataResult.Success(data.success())
        is QueryResult.QueryFailure ->
            DataResult.QueryFailure(cause, exception)
        is QueryResult.NonExistentData ->
            DataResult.NonExistentData(cause)
        is QueryResult.Propagated -> DataResult.Propagated(
            "QueryResult -> $pointOfFailure", failable
        )
    }

fun <R : Any> QueryResult<R>.intoData(): DataResult<out BlockChainData> =
    when (this) {
        //This should never ever happen.
        is QueryResult.Success -> DataResult.QueryFailure(
            "PersistResult can't auto-convert into DataResult."
        )
        is QueryResult.QueryFailure ->
            DataResult.QueryFailure(cause, exception)
        is QueryResult.NonExistentData ->
            DataResult.NonExistentData(cause)
        is QueryResult.Propagated -> DataResult.Propagated(
            "QueryResult -> $pointOfFailure", failable
        )
    }