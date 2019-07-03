package pt.um.masb.ledger.service.transactions

import pt.um.masb.common.database.query.UnspecificQuery
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.ledger.service.results.LoadFailure
import pt.um.masb.ledger.storage.BlockHeader
import pt.um.masb.ledger.storage.adapters.BlockHeaderStorageAdapter


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
                    WHERE hashId = :hashId 
                        AND chainId.hashId = :chainHash
                    """.trimIndent(),
                mapOf(
                    "hashId" to hash.bytes,
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
    BlockHeaderStorageAdapter.let {
        queryUniqueResult(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id} 
                    WHERE blockheight = :blockheight 
                        AND chainId.hashId = :chainHash
                """.trimIndent(),
                mapOf(
                    "blockheight" to height,
                    "chainHash" to chainHash.bytes
                )
            ),
            it
        )
    }


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
                        AND chainId.hashId = :chainHash
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
    BlockHeaderStorageAdapter.let {
        queryUniqueResult(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id} 
                    WHERE blockheight = max(blockheight) 
                        AND chainId.hashId = :chainHash
                """.trimIndent(),
                mapOf(
                    "chainHash" to chainHash.bytes
                )
            ),
            it
        )
    }