package org.knowledger.ledger.storage

import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.Sizeable
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hashable
import org.knowledger.ledger.core.hash.Hashed
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.misc.bytes
import org.knowledger.ledger.core.misc.flattenBytes
import org.knowledger.ledger.core.storage.LedgerContract
import java.time.Instant

interface BlockHeader : Sizeable, Hashed, Cloneable,
                        Hashable, LedgerContract {
    val chainId: ChainId
    val hasher: Hasher
    // Difficulty is fixed at block generation time.
    val difficulty: Difficulty
    val blockheight: Long
    var merkleRoot: Hash
    val previousHash: Hash
    val params: BlockParams
    var timestamp: Instant
    var nonce: Long

    /**
     * Hash is a cryptographic digest calculated from previous hashId,
     * internalNonce, internalTimestamp, [MerkleTree]'s root
     * and each [Transaction]'s hashId.
     */
    fun updateHash()

    fun updateMerkleTree(newRoot: Hash)


    override fun digest(c: Hasher): Hash =
        c.applyHash(
            flattenBytes(
                chainId.hashId.bytes,
                difficulty.difficulty.toByteArray(),
                blockheight.bytes(),
                previousHash.bytes,
                nonce.bytes(),
                timestamp.bytes(),
                merkleRoot.bytes,
                params.blockLength.bytes(),
                params.blockMemSize.bytes()
            )
        )

    public override fun clone(): BlockHeader
}
