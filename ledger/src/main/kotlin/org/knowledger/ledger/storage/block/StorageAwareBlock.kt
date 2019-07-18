package org.knowledger.ledger.storage.block

import com.squareup.moshi.JsonClass
import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageID
import org.knowledger.common.results.Outcome
import org.knowledger.common.results.flatMapSuccess
import org.knowledger.common.results.peekSuccess
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.adapters.BlockHeaderStorageAdapter
import org.knowledger.ledger.storage.adapters.CoinbaseStorageAdapter
import org.knowledger.ledger.storage.adapters.MerkleTreeStorageAdapter
import org.knowledger.ledger.storage.commonUpdate
import org.knowledger.ledger.storage.updateLinked

@JsonClass(generateAdapter = true)
internal data class StorageAwareBlock(
    internal val block: StorageUnawareBlock
) : Block by block,
    StorageAware<Block> {
    override val invalidated: Map<String, Any>
        get() = invalidatedFields

    @Transient
    override var id: StorageID? = null

    @Transient
    internal var invalidatedFields =
        mutableMapOf<String, Any>()


    private fun updateCoinbase(
        session: NewInstanceSession
    ) =
        updateLinked(
            session, "coinbase",
            coinbase, invalidatedFields,
            CoinbaseStorageAdapter
        )

    private fun updateHeader(
        session: NewInstanceSession
    ) =
        updateLinked(
            session, "header",
            header, invalidatedFields,
            BlockHeaderStorageAdapter
        )

    private fun updateMerkleTree(
        session: NewInstanceSession
    ) =
        updateLinked(
            session, "merkleTree",
            merkleTree, invalidatedFields,
            MerkleTreeStorageAdapter
        )

    override fun update(
        session: NewInstanceSession
    ): Outcome<StorageID, UpdateFailure> =
        commonUpdate { elem ->
            updateCoinbase(session)
                .flatMapSuccess {
                    updateHeader(session)
                }.flatMapSuccess {
                    updateMerkleTree(session)
                }.peekSuccess {
                    for (entry in invalidatedFields) {
                        val value = entry.value
                        when (value) {
                            is StorageElement -> elem.setLinked(entry.key, value)
                            else -> elem.setStorageProperty(entry.key, value)
                        }
                    }
                    invalidatedFields.clear()
                }
        }

    override fun plus(transaction: Transaction): Boolean {
        val result = block + transaction
        if (result && id != null) {
            invalidatedFields.putIfAbsent("data", data)
        }
        return result
    }

    override fun updateHashes() {
        block.updateHashes()
        if (id != null) {
            invalidatedFields.putIfAbsent("header", header)
            invalidatedFields.putIfAbsent("merkleTree", merkleTree)
        }
    }

    override fun clone(): Block {
        return StorageUnawareBlock(
            data.asSequence()
                .toSortedSet(),
            coinbase.clone(),
            header.clone(),
            merkleTree.clone()
        )
    }
}