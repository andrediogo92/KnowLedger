package org.knowledger.ledger.storage.merkletree

import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.StorageAware

internal interface StorageAwareMerkleTree : MutableMerkleTree,
                                            StorageAware {
    val merkleTree: MutableMerkleTree
}