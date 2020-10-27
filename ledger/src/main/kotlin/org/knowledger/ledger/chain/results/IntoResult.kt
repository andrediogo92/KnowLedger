package org.knowledger.ledger.chain.results

import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.database.results.QueryFailure
import org.knowledger.ledger.results.propagate
import org.knowledger.ledger.storage.results.LedgerFailure
import org.knowledger.ledger.storage.results.LoadFailure
import org.knowledger.ledger.storage.results.UpdateFailure

//---------------------------------------
//Into Handle Result
//---------------------------------------

fun LoadFailure.intoHandle(): LedgerBuilderFailure =
    when (this) {
        is LoadFailure.UnknownFailure ->
            LedgerBuilderFailure.UnknownFailure(failable.cause, failable.exception)
        is LoadFailure.UnrecognizedDataType,
        is LoadFailure.DuplicatedTransaction,
        is LoadFailure.NonExistentData,
        -> failable.propagate(LedgerBuilderFailure::Propagated)
        is LoadFailure.Propagated -> LedgerBuilderFailure.Propagated(
            "LoadFailure -> ${failable.pointOfFailure}", failable.inner
        )
    }

fun LedgerFailure.intoHandle(): LedgerBuilderFailure =
    when (this) {
        is LedgerFailure.UnknownFailure ->
            LedgerBuilderFailure.UnknownFailure(failable.cause, failable.exception)
        is LedgerFailure.NonExistentData,
        is LedgerFailure.NoKnownStorageAdapter,
        -> failable.propagate(LedgerBuilderFailure::Propagated)
        is LedgerFailure.Propagated -> LedgerBuilderFailure.Propagated(
            "LedgerFailure -> ${failable.pointOfFailure}", failable.inner
        )
    }

fun DataFailure.intoQuery(): QueryFailure =
    when (this) {
        is DataFailure.UnknownFailure ->
            QueryFailure.UnknownFailure(failable.cause, failable.exception)
        is DataFailure.NoUpdateNeeded,
        is DataFailure.NonExistentData,
        is DataFailure.UnrecognizedDataType,
        is DataFailure.UnrecognizedUnit,
        is DataFailure.UnexpectedClass,
        is DataFailure.NonRegisteredSchema,
        -> failable.propagate(QueryFailure::Propagated)
        is DataFailure.Propagated -> QueryFailure.Propagated(
            "DataFailure -> ${failable.pointOfFailure}", failable.inner
        )
    }

fun LoadFailure.intoQuery(): QueryFailure =
    when (this) {
        is LoadFailure.UnknownFailure ->
            QueryFailure.UnknownFailure(failable.cause, failable.exception)
        is LoadFailure.NonExistentData -> QueryFailure.NonExistentData(failable.cause)
        is LoadFailure.UnrecognizedDataType,
        is LoadFailure.DuplicatedTransaction,
        -> failable.propagate(QueryFailure::Propagated)
        is LoadFailure.Propagated -> QueryFailure.Propagated(
            "LoadFailure -> ${failable.pointOfFailure}", failable.inner
        )
    }

fun DataFailure.intoUpdate(): UpdateFailure =
    when (this) {
        is DataFailure.UnknownFailure ->
            UpdateFailure.UnknownFailure(failable.cause, failable.exception)
        is DataFailure.NoUpdateNeeded,
        is DataFailure.NonExistentData,
        is DataFailure.UnrecognizedDataType,
        is DataFailure.UnrecognizedUnit,
        is DataFailure.UnexpectedClass,
        is DataFailure.NonRegisteredSchema,
        -> failable.propagate(UpdateFailure::Propagated)
        is DataFailure.Propagated -> UpdateFailure.Propagated(
            "DataFailure -> ${failable.pointOfFailure}", failable.inner
        )
    }