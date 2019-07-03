package org.knowledger.ledger.service.transactions

import org.knowledger.common.database.query.UnspecificQuery
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.adapters.BlockStorageAdapter

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
                    WHERE header.blockheight = :height 
                        AND chainId.hashId = :chainHash
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
                    WHERE header.chainId.hashId = :chainHash 
                        AND header.hashId = :hashId
                """.trimIndent(),
                mapOf(
                    "hashId" to hash.bytes,
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
                    WHERE header.chainId.hashId = :chainHash 
                        AND header.previousHash = :hashId
                """.trimIndent(),
                mapOf(
                    "hashId" to hash.bytes,
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
                    WHERE header.chainId.hashId = :chainHash
                        AND header.blockheight = max(header.blockheight)
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
                    WHERE header.chainId.hashId = :chainHash 
                        AND header.blockheight BETWEEN :start AND :end
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

