package org.knowledger.ledger.results

import org.knowledger.common.results.Failable
import org.knowledger.common.storage.results.DataFailure
import org.knowledger.common.storage.results.QueryFailure
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.results.LedgerFailure
import org.knowledger.ledger.service.results.LoadFailure

//---------------------------------------
//Into Handle Result
//---------------------------------------

fun LoadFailure.intoHandle(): LedgerHandle.Failure =
    when (this) {
        is LoadFailure.UnrecognizedDataType ->
            LedgerHandle.Failure.Propagated(
                propagate(this), this
            )
        is LoadFailure.NonExistentData ->
            LedgerHandle.Failure.Propagated(
                propagate(this), this
            )
        is LoadFailure.NonMatchingCrypter ->
            LedgerHandle.Failure.Propagated(
                propagate(this), this
            )
        is LoadFailure.UnknownFailure ->
            LedgerHandle.Failure.UnknownFailure(
                this.cause, this.exception
            )
        is LoadFailure.Propagated ->
            LedgerHandle.Failure.Propagated(
                "LoadFailure -> $pointOfFailure",
                failable
            )
    }

fun LedgerFailure.intoHandle(): LedgerHandle.Failure =
    when (this) {
        is LedgerFailure.NonExistentData ->
            LedgerHandle.Failure.Propagated(
                propagate(this), this
            )
        is LedgerFailure.NonMatchingCrypter ->
            LedgerHandle.Failure.Propagated(
                propagate(this), this
            )
        is LedgerFailure.NoKnownStorageAdapter ->
            LedgerHandle.Failure.Propagated(
                propagate(this), this
            )

        is LedgerFailure.UnknownFailure ->
            LedgerHandle.Failure.UnknownFailure(
                this.cause, this.exception
            )
        is LedgerFailure.Propagated ->
            LedgerHandle.Failure.Propagated(
                "LoadFailure -> $pointOfFailure",
                failable
            )

    }


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
                propagate(this), this
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
                "QueryFailure -> $pointOfFailure",
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
                propagate(this), this
            )
        is DataFailure.UnrecognizedUnit ->
            LoadFailure.Propagated(
                propagate(this), this
            )
        is DataFailure.UnexpectedClass ->
            LoadFailure.Propagated(
                propagate(this), this
            )
        is DataFailure.NonRegisteredSchema ->
            LoadFailure.Propagated(
                propagate(this), this
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
        is LedgerFailure.NoKnownStorageAdapter ->
            LoadFailure.Propagated(
                propagate(this), this
            )
        is LedgerFailure.Propagated ->
            LoadFailure.Propagated(
                "LedgerFailure -> $pointOfFailure",
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
                propagate(this), this
            )
        is LoadFailure.NonMatchingCrypter ->
            QueryFailure.Propagated(
                propagate(this), this
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
                propagate(this), this
            )
        is LedgerFailure.NoKnownStorageAdapter ->
            QueryFailure.Propagated(
                propagate(this), this
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


fun <T : Failable> propagate(type: T): String =
    type::class.simpleName!!