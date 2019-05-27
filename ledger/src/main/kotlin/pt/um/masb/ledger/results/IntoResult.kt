package pt.um.masb.ledger.results

import pt.um.masb.common.storage.results.DataFailure
import pt.um.masb.common.storage.results.QueryFailure
import pt.um.masb.ledger.service.results.LedgerFailure
import pt.um.masb.ledger.service.results.LoadFailure


//---------------------------------------
//Into Ledger Result
//---------------------------------------


fun LoadFailure.intoLedger(): LedgerFailure =
    when (this) {
        is LoadFailure.UnknownFailure ->
            LedgerFailure.UnknownFailure(cause, exception)
        is LoadFailure.NonMatchingCrypter ->
            LedgerFailure.NonMatchingCrypter(cause)
        is LoadFailure.UnrecognizedDataType ->
            LedgerFailure.Propagated(
                "LoadFailure", this
            )
        is LoadFailure.NonExistentData ->
            LedgerFailure.NonExistentData(cause)
        is LoadFailure.Propagated ->
            LedgerFailure.Propagated(
                "LoadFailure -> $pointOfFailure",
                failable
            )
    }

fun QueryFailure.intoLedger(): LedgerFailure =
    when (this) {
        is QueryFailure.UnknownFailure ->
            LedgerFailure.UnknownFailure(cause, exception)
        is QueryFailure.NonExistentData ->
            LedgerFailure.NonExistentData(cause)
        is QueryFailure.Propagated ->
            LedgerFailure.Propagated(
                "UnknownFailure -> $pointOfFailure",
                failable
            )
    }

//-----------------------------------------
// Into Load Result
//-----------------------------------------


fun DataFailure.intoLoad(): LoadFailure =
    when (this) {
        is DataFailure.UnknownFailure ->
            LoadFailure.UnknownFailure(cause, exception)
        is DataFailure.UnrecognizedDataType ->
            LoadFailure.Propagated(
                "DataFailure", this
            )
        is DataFailure.UnrecognizedUnit ->
            LoadFailure.Propagated(
                "DataFailure", this
            )
        is DataFailure.UnexpectedClass ->
            LoadFailure.Propagated(
                "DataFailure", this
            )
        is DataFailure.NonRegisteredSchema ->
            LoadFailure.Propagated(
                "DataFailure", this
            )
        is DataFailure.NonExistentData ->
            LoadFailure.NonExistentData(cause)
        is DataFailure.Propagated ->
            LoadFailure.Propagated(
                "DataFailure -> $pointOfFailure",
                failable
            )
    }


fun LedgerFailure.intoLoad(): LoadFailure =
    when (this) {
        is LedgerFailure.UnknownFailure ->
            LoadFailure.UnknownFailure(cause, exception)
        is LedgerFailure.NonMatchingCrypter ->
            LoadFailure.NonMatchingCrypter(cause)
        is LedgerFailure.NonExistentData ->
            LoadFailure.NonExistentData(cause)
        is LedgerFailure.UnregisteredCrypter ->
            LoadFailure.Propagated(
                "LedgerResult", this
            )
        is LedgerFailure.Propagated ->
            LoadFailure.Propagated(
                "LedgerResult -> $pointOfFailure",
                failable
            )
    }


fun QueryFailure.intoLoad(): LoadFailure =
    when (this) {
        is QueryFailure.UnknownFailure ->
            LoadFailure.UnknownFailure(cause, exception)
        is QueryFailure.NonExistentData ->
            LoadFailure.NonExistentData(cause)
        is QueryFailure.Propagated ->
            LoadFailure.Propagated(
                "UnknownFailure -> $pointOfFailure",
                failable
            )
    }


//-------------------------------------
// Into Query Result
//-------------------------------------


fun LoadFailure.intoQuery(): QueryFailure =
    when (this) {
        is LoadFailure.UnknownFailure ->
            QueryFailure.UnknownFailure(cause, exception)
        is LoadFailure.NonExistentData ->
            QueryFailure.NonExistentData(cause)
        is LoadFailure.UnrecognizedDataType ->
            QueryFailure.Propagated(
                "LoadFailure", this
            )
        is LoadFailure.NonMatchingCrypter ->
            QueryFailure.Propagated(
                "LoadFailure", this
            )
        is LoadFailure.Propagated ->
            QueryFailure.Propagated(
                "LoadFailure -> $pointOfFailure",
                failable
            )
    }

fun LedgerFailure.intoQuery(): QueryFailure =
    when (this) {
        is LedgerFailure.UnknownFailure ->
            QueryFailure.UnknownFailure(cause, exception)
        is LedgerFailure.NonExistentData ->
            QueryFailure.NonExistentData(cause)
        is LedgerFailure.NonMatchingCrypter ->
            QueryFailure.Propagated(
                "LedgerResult", this
            )
        is LedgerFailure.UnregisteredCrypter ->
            QueryFailure.Propagated(
                "LedgerResult", this
            )
        is LedgerFailure.Propagated ->
            QueryFailure.Propagated(
                "LedgerResult -> $pointOfFailure",
                failable
            )
    }

//---------------------------------------
// Into Data Result
//---------------------------------------

fun QueryFailure.intoData(): DataFailure =
    when (this) {
        is QueryFailure.UnknownFailure ->
            DataFailure.UnknownFailure(cause, exception)
        is QueryFailure.NonExistentData ->
            DataFailure.NonExistentData(cause)
        is QueryFailure.Propagated ->
            DataFailure.Propagated(
                "UnknownFailure -> $pointOfFailure",
                failable
            )
    }