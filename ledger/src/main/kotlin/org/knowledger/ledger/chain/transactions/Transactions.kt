package org.knowledger.ledger.chain.transactions

import org.knowledger.ledger.chain.data.TransactionWithBlockHash
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.query.UnspecificQuery
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.results.LoadFailure


// ------------------------------
// Transactions over transactions.
// Go figure.
//
// Execution must be runtime determined.
// ------------------------------


internal fun QueryManager.getTransactionsFromAgent(
    publicKey: EncodedPublicKey,
): Outcome<List<MutableTransaction>, LoadFailure> =
    transactionStorageAdapter.let { adapter ->
        val query = UnspecificQuery(
            """ SELECT FROM ${adapter.id}
                |WHERE data.ledgerData.@class = :tag
                    |AND publicKey = :publicKey
            """.trimMargin(), mapOf(
                "tag" to chainId.tag.id, "publicKey" to publicKey.bytes
            )
        )
        queryResults(query, adapter)
    }

internal fun QueryManager.getTransactionByIndex(
    blockHash: Hash, index: Int,
): Outcome<MutableTransaction, LoadFailure> {
    val query = UnspecificQuery(
        """ SELECT expand(transactions[index = :index]) as tx
            |FROM ${blockStorageAdapter.id}
            |WHERE blockHeader.hash = :hash
            |UNWIND tx
        """.trimMargin(), mapOf("hash" to blockHash.bytes, "index" to index)
    )
    return queryUniqueResult(query, transactionStorageAdapter)
}

internal fun QueryManager.getTransactionByBound(
    currentMillis: Long, diff: Long,
): Outcome<TransactionWithBlockHash, LoadFailure> {
    val lowerBound = currentMillis - diff
    val upperBound = currentMillis + diff
    val query = UnspecificQuery(
        """ SELECT txMin, txBlockHash, txHash, txData, txIndex, txMillis
            |FROM ${'$'}tx
            |LET ${'$'}tx =
                |(SELECT abs(tx.millis - :millis) as txMin, 
                        |txBlockHash, tx.hash as txHash, tx.data as txData,
                        |tx.index as txIndex, tx.millis as txMillis
                |FROM (SELECT blockHeader.hash as txBlockHash,
                            |transactions:{hash, index, data, data.millis as millis} as tx
                    |FROM ${blockStorageAdapter.id}
                    |UNWIND tx)
                |WHERE tx.millis BETWEEN :lower AND :upper),
            |${'$'}min = (SELECT min(txMin) FROM ${'$'}tx)
            |WHERE txMin = ${'$'}min
            """.trimMargin(), mapOf(
            "millis" to currentMillis, "lower" to lowerBound, "upper" to upperBound
        )
    )
    return queryUniqueResult(query, transactionWithBlockHashStorageLoadable)
}

internal fun QueryManager.getTransactionByHash(
    hash: Hash,
): Outcome<MutableTransaction, LoadFailure> =
    transactionStorageAdapter.let { adapter ->
        val query = UnspecificQuery(
            """ SELECT FROM ${adapter.id}
                |WHERE data.ledgerData.@class = :tag
                    |AND hash = :hash
            """.trimMargin(), mapOf("tag" to chainId.tag.id, "hash" to hash.bytes)
        )
        queryUniqueResult(query, adapter)
    }

internal fun QueryManager.getTransactionsOrderedByTimestamp(
): Outcome<List<MutableTransaction>, LoadFailure> =
    transactionStorageAdapter.let { adapter ->
        val query = UnspecificQuery(
            """ SELECT FROM ${adapter.id}
                |WHERE data.ledgerData.@class = :tag
                |ORDER BY data.millis ASC
            """.trimMargin(), mapOf("tag" to chainId.tag.id)
        )
        queryResults(query, adapter)
    }

internal fun QueryManager.getTransactionsByClass(): Outcome<List<Transaction>, LoadFailure> =
    transactionStorageAdapter.let { adapter ->
        val query = UnspecificQuery(
            """ SELECT FROM ${adapter.id}
                |WHERE data.ledgerData.@class = :tag
            """.trimMargin(), mapOf("tag" to chainId.tag.id)
        )
        queryResults(query, adapter)
    }