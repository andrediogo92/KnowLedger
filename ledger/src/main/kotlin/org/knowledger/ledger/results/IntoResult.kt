package org.knowledger.ledger.results

import org.knowledger.ledger.core.results.Failable
import org.knowledger.ledger.core.results.Failure
import org.knowledger.ledger.core.storage.results.DataFailure
import org.knowledger.ledger.core.storage.results.QueryFailure
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.results.LedgerFailure
import org.knowledger.ledger.service.results.LoadFailure

//---------------------------------------
//Into Handle Result
//---------------------------------------

fun LoadFailure.intoHandle(): LedgerHandle.Failure =
    when (this) {
        is LoadFailure.UnrecognizedDataType ->
            failable.propagate(LedgerHandle.Failure::Propagated)
        is LoadFailure.NonExistentData ->
            failable.propagate(LedgerHandle.Failure::Propagated)
        is LoadFailure.NonMatchingHasher ->
            failable.propagate(LedgerHandle.Failure::Propagated)
        is LoadFailure.NoMatchingContainer ->
            failable.propagate(LedgerHandle.Failure::Propagated)
        is LoadFailure.UnknownFailure ->
            LedgerHandle.Failure.UnknownFailure(
                failable.cause, failable.exception
            )
        is LoadFailure.Propagated ->
            LedgerHandle.Failure.Propagated(
                "LoadFailure -> ${failable.pointOfFailure}",
                failable.inner
            )
    }

fun LedgerFailure.intoHandle(): LedgerHandle.Failure =
    when (this) {
        is LedgerFailure.NonExistentData ->
            failable.propagate(LedgerHandle.Failure::Propagated)
        is LedgerFailure.NonMatchingHasher ->
            failable.propagate(LedgerHandle.Failure::Propagated)
        is LedgerFailure.NoKnownStorageAdapter ->
            failable.propagate(LedgerHandle.Failure::Propagated)
        is LedgerFailure.UnknownFailure ->
            LedgerHandle.Failure.UnknownFailure(
                failable.cause, failable.exception
            )
        is LedgerFailure.Propagated ->
            LedgerHandle.Failure.Propagated(
                "LoadFailure -> ${failable.pointOfFailure}",
                failable.inner
            )

    }


//---------------------------------------
//Into Ledger Result
//---------------------------------------


fun LoadFailure.intoLedger(): LedgerFailure =
    when (this) {
        is LoadFailure.UnknownFailure ->
            LedgerFailure.UnknownFailure(failable.cause, failable.exception)
        is LoadFailure.NonMatchingHasher ->
            LedgerFailure.NonMatchingHasher(ledgerHash)
        is LoadFailure.UnrecognizedDataType ->
            failable.propagate(LedgerFailure::Propagated)
        is LoadFailure.NonExistentData ->
            LedgerFailure.NonExistentData(failable.cause)
        is LoadFailure.NoMatchingContainer ->
            failable.propagate(LedgerFailure::Propagated)
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
            LoadFailure.UnknownFailure(failable.cause, failable.exception)
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
            LoadFailure.UnknownFailure(failable.cause, failable.exception)
        is LedgerFailure.NonMatchingHasher ->
            LoadFailure.NonMatchingHasher(ledgerHash)
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
            LoadFailure.UnknownFailure(failable.cause, failable.exception)
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
        is LoadFailure.NonMatchingHasher ->
            failable.propagate(QueryFailure::Propagated)
        is LoadFailure.NoMatchingContainer ->
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
        is LedgerFailure.NonMatchingHasher ->
            failable.propagate(QueryFailure::Propagated)
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

private inline fun <T : Failable, R : Failure> T.propagate(
    cons: (String, Failable) -> R
): R =
    cons(this.javaClass.simpleName, this)
