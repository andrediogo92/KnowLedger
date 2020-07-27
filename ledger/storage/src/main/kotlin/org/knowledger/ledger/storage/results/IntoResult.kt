package org.knowledger.ledger.storage.results

import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.database.results.QueryFailure
import org.knowledger.ledger.results.propagate


//---------------------------------------
//Into Ledger Result
//---------------------------------------


fun LoadFailure.intoLedger(): LedgerFailure =
    when (this) {
        is LoadFailure.UnknownFailure ->
            LedgerFailure.UnknownFailure(failable.cause, failable.exception)
        is LoadFailure.UnrecognizedDataType ->
            failable.propagate(LedgerFailure::Propagated)
        is LoadFailure.NonExistentData ->
            LedgerFailure.NonExistentData(failable.cause)
        is LoadFailure.Propagated ->
            LedgerFailure.Propagated(
                "LoadFailure -> ${failable.pointOfFailure}",
                failable.inner
            )
    }

fun QueryFailure.intoLedger(): LedgerFailure =
    when (this) {
        is QueryFailure.UnknownFailure ->
            LedgerFailure.UnknownFailure(failable.cause, failable.exception)
        is QueryFailure.NonExistentData ->
            LedgerFailure.NonExistentData(failable.cause)
        is QueryFailure.Propagated ->
            LedgerFailure.Propagated(
                "QueryFailure -> ${failable.pointOfFailure}",
                failable
            )
    }

//-----------------------------------------
// Into Load Result
//-----------------------------------------


fun DataFailure.intoLoad(): LoadFailure =
    when (this) {
        is DataFailure.UnknownFailure ->
            LoadFailure.UnknownFailure(
                failable.cause,
                failable.exception
            )
        is DataFailure.UnrecognizedDataType ->
            failable.propagate(LoadFailure::Propagated)
        is DataFailure.UnrecognizedUnit ->
            failable.propagate(LoadFailure::Propagated)
        is DataFailure.UnexpectedClass ->
            failable.propagate(LoadFailure::Propagated)
        is DataFailure.NonRegisteredSchema ->
            failable.propagate(LoadFailure::Propagated)
        is DataFailure.NonExistentData ->
            LoadFailure.NonExistentData(cause)
        is DataFailure.Propagated ->
            LoadFailure.Propagated(
                "DataFailure -> ${failable.pointOfFailure}",
                failable
            )
    }


fun LedgerFailure.intoLoad(): LoadFailure =
    when (this) {
        is LedgerFailure.UnknownFailure ->
            LoadFailure.UnknownFailure(
                failable.cause,
                failable.exception
            )
        is LedgerFailure.NonExistentData ->
            LoadFailure.NonExistentData(failable.cause)
        is LedgerFailure.NoKnownStorageAdapter ->
            failable.propagate(LoadFailure::Propagated)
        is LedgerFailure.Propagated ->
            LoadFailure.Propagated(
                "LedgerFailure -> ${failable.pointOfFailure}",
                failable
            )
    }


fun QueryFailure.intoLoad(): LoadFailure =
    when (this) {
        is QueryFailure.UnknownFailure ->
            LoadFailure.UnknownFailure(
                failable.cause,
                failable.exception
            )
        is QueryFailure.NonExistentData ->
            LoadFailure.NonExistentData(failable.cause)
        is QueryFailure.Propagated ->
            LoadFailure.Propagated(
                "UnknownFailure -> ${failable.pointOfFailure}",
                failable
            )
    }


//-------------------------------------
// Into Query Result
//-------------------------------------


fun LoadFailure.intoQuery(): QueryFailure =
    when (this) {
        is LoadFailure.UnknownFailure ->
            QueryFailure.UnknownFailure(failable.cause, failable.exception)
        is LoadFailure.NonExistentData ->
            QueryFailure.NonExistentData(failable.cause)
        is LoadFailure.UnrecognizedDataType ->
            failable.propagate(QueryFailure::Propagated)
        is LoadFailure.Propagated ->
            QueryFailure.Propagated(
                "LoadFailure -> ${failable.pointOfFailure}",
                failable
            )
    }

fun LedgerFailure.intoQuery(): QueryFailure =
    when (this) {
        is LedgerFailure.UnknownFailure ->
            QueryFailure.UnknownFailure(failable.cause, failable.exception)
        is LedgerFailure.NonExistentData ->
            QueryFailure.NonExistentData(failable.cause)
        is LedgerFailure.NoKnownStorageAdapter ->
            failable.propagate(QueryFailure::Propagated)
        is LedgerFailure.Propagated ->
            QueryFailure.Propagated(
                "LedgerResult -> ${failable.pointOfFailure}",
                failable
            )
    }

//---------------------------------------
// Into Data Result
//---------------------------------------

fun QueryFailure.intoData(): DataFailure =
    when (this) {
        is QueryFailure.UnknownFailure ->
            DataFailure.UnknownFailure(failable.cause, failable.exception)
        is QueryFailure.NonExistentData ->
            DataFailure.NonExistentData(failable.cause)
        is QueryFailure.Propagated ->
            DataFailure.Propagated(
                "UnknownFailure -> ${failable.pointOfFailure}",
                failable
            )
    }