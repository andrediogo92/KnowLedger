package org.knowledger.ledger.service.transactions

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.query.UnspecificQuery
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.BlockHeader


// ------------------------------
// Blockheader transactions.
//
// ------------------------------


internal fun QueryManager.getBlockHeaderByHash(
    chainHash: Hash,
    hash: Hash
): Outcome<BlockHeader, LoadFailure> =
    blockHeaderStorageAdapter.let {
        queryUniqueResult(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id} 
                    WHERE hash = :hash 
                        AND chainId.hash = :chainHash
                    """.trimIndent(),
                mapOf(
                    "hash" to hash.bytes,
                    "chainHash" to chainHash.bytes
                )
            ),
            it
        )
    }


internal fun QueryManager.getBlockHeaderByBlockHeight(
    chainHash: Hash,
    height: Long
): Outcome<BlockHeader, LoadFailure> =
    queryUniqueResult(
        UnspecificQuery(
            """
                SELECT header
                FROM ${blockStorageAdapter.id} 
                WHERE coinbase.blockheight = :blockheight 
                    AND chainId.hash = :chainHash
            """.trimIndent(),
            mapOf(
                "blockheight" to height,
                "chainHash" to chainHash.bytes
            )
        ),
        blockHeaderStorageAdapter
    )


internal fun QueryManager.getBlockHeaderByPrevHeaderHash(
    chainHash: Hash,
    hash: Hash
): Outcome<BlockHeader, LoadFailure> =
    blockHeaderStorageAdapter.let {
        queryUniqueResult(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id} 
                    WHERE previousHash = :previousHash 
                        AND chainId.hash = :chainHash
                """.trimIndent(),
                mapOf(
                    "previousHash" to hash.bytes,
                    "chainHash" to chainHash.bytes
                )
            ),
            it
        )

    }

internal fun QueryManager.getLatestBlockHeader(
    chainHash: Hash
): Outcome<BlockHeader, LoadFailure> =
    queryUniqueResult(
        UnspecificQuery(
            """
                SELECT 
                FROM ${blockStorageAdapter.id} 
                WHERE coinbase.blockheight = max(coinbase.blockheight) 
                    AND chainId.hash = :chainHash
            """.trimIndent(),
            mapOf(
                "chainHash" to chainHash.bytes
            )
        ),
        blockHeaderStorageAdapter
    )