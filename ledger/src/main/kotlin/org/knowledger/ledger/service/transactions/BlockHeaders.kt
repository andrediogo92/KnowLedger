package org.knowledger.ledger.service.transactions

import org.knowledger.ledger.core.database.query.UnspecificQuery
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.adapters.BlockHeaderStorageAdapter
import org.knowledger.ledger.storage.adapters.BlockStorageAdapter


// ------------------------------
// Blockheader transactions.
//
// ------------------------------


internal fun PersistenceWrapper.getBlockHeaderByHash(
    chainHash: Hash,
    hash: Hash
): Outcome<BlockHeader, LoadFailure> =
    BlockHeaderStorageAdapter.let {
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


internal fun PersistenceWrapper.getBlockHeaderByBlockHeight(
    chainHash: Hash,
    height: Long
): Outcome<BlockHeader, LoadFailure> =
    queryUniqueResult(
        UnspecificQuery(
            """
                SELECT header
                FROM ${BlockStorageAdapter.id} 
                WHERE coinbase.blockheight = :blockheight 
                    AND chainId.hash = :chainHash
            """.trimIndent(),
            mapOf(
                "blockheight" to height,
                "chainHash" to chainHash.bytes
            )
        ),
        BlockHeaderStorageAdapter
    )


internal fun PersistenceWrapper.getBlockHeaderByPrevHeaderHash(
    chainHash: Hash,
    hash: Hash
): Outcome<BlockHeader, LoadFailure> =
    BlockHeaderStorageAdapter.let {
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

internal fun PersistenceWrapper.getLatestBlockHeader(
    chainHash: Hash
): Outcome<BlockHeader, LoadFailure> =
    queryUniqueResult(
        UnspecificQuery(
            """
                SELECT 
                FROM ${BlockStorageAdapter.id} 
                WHERE coinbase.blockheight = max(coinbase.blockheight) 
                    AND chainId.hash = :chainHash
            """.trimIndent(),
            mapOf(
                "chainHash" to chainHash.bytes
            )
        ),
        BlockHeaderStorageAdapter
    )