package org.knowledger.ledger.chain.transactions

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.query.UnspecificQuery
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.results.LoadFailure


// ------------------------------
// Blockheader transactions.
//
// ------------------------------


internal fun QueryManager.getBlockHeaderByHash(
    hash: Hash,
): Outcome<MutableBlockHeader, LoadFailure> =
    blockHeaderStorageAdapter.let { adapter ->
        val query = UnspecificQuery(
            """ SELECT 
                |FROM ${adapter.id} 
                |WHERE hash = :hash AND chainHash = :chainHash
            """.trimMargin(), mapOf("hash" to hash.bytes, "chainHash" to chainId.hash.bytes)
        )
        return queryUniqueResult(query, adapter)
    }


internal fun QueryManager.getBlockHeaderByBlockHeight(
    height: Long,
): Outcome<MutableBlockHeader, LoadFailure> {
    val query = UnspecificQuery(
        """ SELECT blockHeader
            |FROM ${blockStorageAdapter.id}
            |WHERE coinbase.coinbaseHeader.blockheight = :blockheight 
                |AND blockHeader.chainHash = :chainHash
        """.trimMargin(), mapOf("blockheight" to height, "chainHash" to chainId.hash.bytes)
    )
    return queryUniqueResult(query, blockHeaderStorageAdapter)
}


internal fun QueryManager.getBlockHeaderByPrevHeaderHash(
    hash: Hash,
): Outcome<MutableBlockHeader, LoadFailure> =
    blockHeaderStorageAdapter.let { adapter ->
        val query = UnspecificQuery(
            """ SELECT 
                |FROM ${adapter.id} 
                |WHERE previousHash = :previousHash 
                    |AND chainHash = :chainHash
            """.trimMargin(), mapOf("previousHash" to hash.bytes, "chainHash" to chainId.hash.bytes)
        )
        queryUniqueResult(query, adapter)
    }

internal fun QueryManager.getLatestBlockHeader(): Outcome<MutableBlockHeader, LoadFailure> {
    val query = UnspecificQuery(
        """ SELECT 
            |FROM ${blockStorageAdapter.id} 
            |LET ${'$'}max = 
                |(SELECT max(coinbase.coinbaseHeader.blockheight) 
                |FROM ${blockStorageAdapter.id})
            |WHERE coinbase.coinbaseHeader.blockheight = ${'$'}max
                |AND blockHeader.chainHash = :chainHash
        """.trimMargin(), mapOf("chainHash" to chainId.hash.bytes)
    )
    return queryUniqueResult(query, blockHeaderStorageAdapter)
}
