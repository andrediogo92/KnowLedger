package org.knowledger.ledger.storage.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.peekSuccess
import org.knowledger.ledger.core.results.zip
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.adapters.BlockHeaderStorageAdapter
import org.knowledger.ledger.storage.adapters.CoinbaseStorageAdapter
import org.knowledger.ledger.storage.adapters.MerkleTreeStorageAdapter
import org.knowledger.ledger.storage.addOrReplaceInstance
import org.knowledger.ledger.storage.addOrReplaceInstances
import org.knowledger.ledger.storage.commonUpdate
import org.knowledger.ledger.storage.transaction.HashedTransaction
import org.knowledger.ledger.storage.updateLinked

@Serializable
@SerialName("StorageBlockWrapper")
internal data class StorageAwareBlock(
    internal val block: BlockImpl
) : Block by block,
    StorageAware<Block> {
    override val invalidated: List<StoragePairs>
        get() = invalidatedFields

    @Transient
    override var id: StorageID? = null

    @Transient
    internal var invalidatedFields =
        mutableListOf<StoragePairs>()


    private fun updateCoinbase(
        session: ManagedSession
    ) =
        updateLinked(
            session, "coinbase",
            coinbase, invalidatedFields,
            CoinbaseStorageAdapter
        )

    private fun updateHeader(
        session: ManagedSession
    ) =
        updateLinked(
            session, "header",
            header, invalidatedFields,
            BlockHeaderStorageAdapter
        )

    private fun updateMerkleTree(
        session: ManagedSession
    ) =
        updateLinked(
            session, "merkleTree",
            merkleTree, invalidatedFields,
            MerkleTreeStorageAdapter
        )

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        commonUpdate { elem ->
            zip(
                updateCoinbase(session),
                updateHeader(session),
                updateMerkleTree(session)
            ) { s1, s2, s3 ->
                assert(s1 == s2 && s2 == s3)
                s1
            }.peekSuccess {
                for (entry in invalidatedFields) {
                    when (val value = entry.value) {
                        is StorageElement -> elem.setLinked(entry.key, value)
                        else -> elem.setStorageProperty(entry.key, value)
                    }
                }
                invalidatedFields.clear()
            }
        }

    override fun plus(transaction: HashedTransaction): Boolean {
        val result = block + transaction
        if (result && id != null) {
            invalidatedFields.addOrReplaceInstance(
                "data", StoragePairs.Element.Native(data)
            )
        }
        return result
    }

    override fun updateHashes() {
        block.updateHashes()
        if (id != null) {
            invalidatedFields.addOrReplaceInstances(
                arrayOf("header", "merkleTree"),
                arrayOf(
                    StoragePairs.Element.Native(header),
                    StoragePairs.Element.Native(merkleTree)
                )
            )
        }
    }

    override fun clone(): Block {
        return BlockImpl(
            data.asSequence()
                .toSortedSet(),
            coinbase.clone(),
            header.clone(),
            merkleTree.clone()
        )
    }
}