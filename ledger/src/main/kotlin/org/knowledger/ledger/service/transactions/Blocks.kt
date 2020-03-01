package org.knowledger.ledger.service.transactions

import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.query.UnspecificQuery
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.Block

// ------------------------------
// Block transactions.
//
// ------------------------------


internal fun QueryManager.getBlockByBlockHeight(
    chainHash: Hash,
    height: Long
): Outcome<Block, LoadFailure> =
    blockStorageAdapter.let {
        queryUniqueResult(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id} 
                    WHERE coinbase.blockheight = :height 
                        AND header.chainId.hash = :chainHash
                """.trimIndent(),
                mapOf(
                    "height" to height,
                    "chainHash" to chainHash.bytes
                )
            ),
            it
        )

    }


internal fun QueryManager.getBlockByHeaderHash(
    chainHash: Hash,
    hash: Hash
): Outcome<Block, LoadFailure> =
    blockStorageAdapter.let {
        queryUniqueResult(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id} 
                    WHERE header.chainId.hash = :chainHash 
                        AND header.hash = :hash
                """.trimIndent(),
                mapOf(
                    "hash" to hash.bytes,
                    "chainHash" to chainHash.bytes
                )
            ),
            it
        )

    }


internal fun QueryManager.getBlockByPrevHeaderHash(
    chainHash: Hash,
    hash: Hash
): Outcome<Block, LoadFailure> =
    blockStorageAdapter.let {
        queryUniqueResult(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id} 
                    WHERE header.chainId.hash = :chainHash 
                        AND header.previousHash = :hash
                """.trimIndent(),
                mapOf(
                    "hash" to hash.bytes,
                    "chainHash" to chainHash.bytes
                )
            ),
            it
        )

    }


internal fun QueryManager.getLatestBlock(
    chainHash: Hash
): Outcome<Block, LoadFailure> =
    blockStorageAdapter.let {
        queryUniqueResult(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id}
                    WHERE header.chainId.hash = :chainHash
                        AND coinbase.blockheight = max(coinbase.blockheight)
                """.trimIndent(),
                mapOf(
                    "chainHash" to chainHash.bytes
                )
            ),
            it
        )
    }

internal fun QueryManager.getBlockListByBlockHeightInterval(
    chainHash: Hash,
    startInclusive: Long,
    endInclusive: Long
): Outcome<Sequence<Block>, LoadFailure> =
    blockStorageAdapter.let {
        queryResults(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id} 
                    WHERE header.chainId.hash = :chainHash 
                        AND coinbase.blockheight BETWEEN :start AND :end
                """.trimIndent(),
                mapOf(
                    "start" to startInclusive,
                    "end" to endInclusive,
                    "chainHash" to chainHash.bytes
                )
            ),
            it
        )
    }

internal fun QueryManager.getBlockListByHash(
    chainHash: Hash, start: Hash,
    chunkSize: Long
): Outcome<Sequence<Block>, LoadFailure> =
    blockStorageAdapter.let {
        queryResults(
            UnspecificQuery(
                """
                    SELECT FROM ${it.id} 
                        LET ${'$'}temp = (SELECT coinbase.blockheight as blockheight
                                        FROM ${it.id} 
                                        WHERE header.hash = :start)
                    WHERE header.chainId.hash = :chainHash
                        AND coinbase.blockheight 
                        BETWEEN ${'$'}temp.blockheight 
                            AND ${'$'}temp.blockheight + :chunkSize
                """.trimIndent(),
                mapOf(
                    "start" to start,
                    "chunkSize" to chunkSize,
                    "chainHash" to chainHash.bytes
                )
            ),
            it
        )
    }

