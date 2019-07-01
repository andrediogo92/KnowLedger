package pt.um.masb.ledger.storage.transactions

import pt.um.masb.common.database.query.Filters
import pt.um.masb.common.database.query.GenericSelect
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.ledger.service.results.LoadFailure
import pt.um.masb.ledger.storage.Transaction
import pt.um.masb.ledger.storage.adapters.TransactionStorageAdapter
import java.security.PublicKey


// ------------------------------
// Transactions over transactions.
// Go figure.
//
// Execution must be runtime determined.
// ------------------------------
internal fun PersistenceWrapper.getTransactionsFromAgent(
    ledgerHash: Hash,
    publicKey: PublicKey
): Outcome<Sequence<Transaction>, LoadFailure> =
    TransactionStorageAdapter.let {
        queryResults(
            ledgerHash,
            GenericSelect(
                it.id
            ).withSimpleFilter(
                Filters.WHERE,
                "publicKey",
                "publicKey",
                publicKey.encoded
            ),
            it
        )
    }

internal fun PersistenceWrapper.getTransactionByHash(
    ledgerHash: Hash,
    hash: Hash
): Outcome<Transaction, LoadFailure> =
    TransactionStorageAdapter.let {
        queryUniqueResult(
            ledgerHash,
            GenericSelect(
                it.id
            ).withSimpleFilter(
                Filters.WHERE,
                "hashId",
                "hashId",
                hash.bytes
            ),
            it
        )

    }


//Execution must be runtime determined.
internal fun PersistenceWrapper.getTransactionsOrderedByTimestamp(
    ledgerHash: Hash
): Outcome<Sequence<Transaction>, LoadFailure> =
    TransactionStorageAdapter.let {
        queryResults(
            ledgerHash,
            GenericSelect(
                it.id
            ).withSimpleFilter(
                Filters.ORDER,
                "value.seconds DESC, value.nanos DESC"
            ),
            it
        )

    }

internal fun PersistenceWrapper.getTransactionsByClass(
    ledgerHash: Hash,
    typeName: String
): Outcome<Sequence<Transaction>, LoadFailure> =
    TransactionStorageAdapter.let {
        queryResults(
            ledgerHash,
            GenericSelect(
                it.id
            ).withSimpleFilter(
                Filters.WHERE,
                "value.value.@class",
                "typeName",
                typeName
            ),
            it
        )

    }