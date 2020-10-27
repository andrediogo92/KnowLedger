package org.knowledger.ledger.chain.transactions

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.query.UnspecificQuery
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.storage.MutableBlock
import org.knowledger.ledger.storage.results.LoadFailure

// ------------------------------
// Block transactions.
//
// ------------------------------


internal fun QueryManager.getBlockByBlockHeight(height: Long): Outcome<MutableBlock, LoadFailure> =
    blockStorageAdapter.let { adapter ->
        val query = UnspecificQuery(
            """ SELECT 
                |FROM ${adapter.id} 
                |WHERE coinbase.coinbaseHeader.blockheight = :height 
                    |AND blockHeader.chainHash = :chainHash
            """.trimMargin(), mapOf("height" to height, "chainHash" to chainId.hash.bytes)
        )
        queryUniqueResult(query, adapter)
    }


internal fun QueryManager.getBlockByHeaderHash(hash: Hash): Outcome<MutableBlock, LoadFailure> =
    blockStorageAdapter.let { adapter ->
        val query = UnspecificQuery(
            """ SELECT 
                |FROM ${adapter.id} 
                |WHERE blockHeader.chainHash = :chainHash 
                    |AND blockHeader.hash = :hash
            """.trimMargin(), mapOf("hash" to hash.bytes, "chainHash" to chainId.hash.bytes)
        )
        queryUniqueResult(query, adapter)
    }


internal fun QueryManager.getBlockByPrevHeaderHash(hash: Hash): Outcome<MutableBlock, LoadFailure> =
    blockStorageAdapter.let { adapter ->
        val query = UnspecificQuery(
            """ SELECT 
                |FROM ${adapter.id} 
                |WHERE blockHeader.chainHash = :chainHash 
                    |AND blockHeader.previousHash = :hash
            """.trimMargin(), mapOf("hash" to hash.bytes, "chainHash" to chainId.hash.bytes)
        )
        queryUniqueResult(query, adapter)
    }


internal fun QueryManager.getLatestBlock(): Outcome<MutableBlock, LoadFailure> =
    blockStorageAdapter.let { adapter ->
        val query = UnspecificQuery(
            """ SELECT 
                |FROM ${adapter.id}
                |LET ${'$'}max = 
                    |(SELECT max(coinbase.coinbaseHeader.blockheight) 
                    |FROM ${blockStorageAdapter.id})
                |WHERE blockHeader.chainHash = :chainHash
                    |AND coinbase.coinbaseHeader.blockheight = ${'$'}max
            """.trimMargin(), mapOf("chainHash" to chainId.hash.bytes)
        )
        queryUniqueResult(query, adapter)
    }

internal fun QueryManager.getBlockListByBlockHeightInterval(
    startInclusive: Long, endInclusive: Long,
): Outcome<List<MutableBlock>, LoadFailure> =
    blockStorageAdapter.let { adapter ->
        val query = UnspecificQuery(
            """ SELECT 
                |FROM ${adapter.id} 
                |WHERE blockHeader.chainHash = :chainHash 
                    |AND coinbase.coinbaseHeader.blockheight BETWEEN :start AND :end
            """.trimMargin(), mapOf(
                "start" to startInclusive, "end" to endInclusive, "chainHash" to chainId.hash.bytes
            )
        )
        queryResults(query, adapter)
    }

internal fun QueryManager.getBlockListByHash(
    start: Hash, chunkSize: Long,
): Outcome<List<MutableBlock>, LoadFailure> =
    blockStorageAdapter.let { adapter ->
        val query = UnspecificQuery(
            """ SELECT FROM ${adapter.id} 
                |LET ${'$'}temp = 
                    |(SELECT coinbase.coinbaseHeader.blockheight as blockheight
                    |FROM ${adapter.id} 
                    |WHERE blockHeader.hash = :start)
                |WHERE blockHeader.chainHash = :chainHash
                    |AND coinbase.coinbaseHeader.blockheight 
                        |BETWEEN ${'$'}temp.blockheight 
                            |AND ${'$'}temp.blockheight + :chunkSize
            """.trimMargin(), mapOf(
                "start" to start, "chunkSize" to chunkSize, "chainHash" to chainId.hash.bytes
            )
        )
        queryResults(query, adapter)
    }

