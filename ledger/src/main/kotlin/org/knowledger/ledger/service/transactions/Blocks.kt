package org.knowledger.ledger.service.transactions

import org.knowledger.ledger.core.database.query.UnspecificQuery
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.BlockStorageAdapter
import org.knowledger.ledger.storage.block.Block

// ------------------------------
// Block transactions.
//
// ------------------------------


internal fun PersistenceWrapper.getBlockByBlockHeight(
    chainHash: Hash,
    height: Long
): Outcome<Block, LoadFailure> =
    BlockStorageAdapter.let {
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


internal fun PersistenceWrapper.getBlockByHeaderHash(
    chainHash: Hash,
    hash: Hash
): Outcome<Block, LoadFailure> =
    BlockStorageAdapter.let {
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


internal fun PersistenceWrapper.getBlockByPrevHeaderHash(
    chainHash: Hash,
    hash: Hash
): Outcome<Block, LoadFailure> =
    BlockStorageAdapter.let {
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


internal fun PersistenceWrapper.getLatestBlock(
    chainHash: Hash
): Outcome<Block, LoadFailure> =
    BlockStorageAdapter.let {
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

internal fun PersistenceWrapper.getBlockListByBlockHeightInterval(
    chainHash: Hash,
    startInclusive: Long,
    endInclusive: Long
): Outcome<Sequence<Block>, LoadFailure> =
    BlockStorageAdapter.let {
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

internal fun PersistenceWrapper.getBlockListByHash(
    chainHash: Hash, start: Hash,
    chunkSize: Long
): Outcome<Sequence<Block>, LoadFailure> =
    BlockStorageAdapter.let {
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

