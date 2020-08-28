package org.knowledger.ledger.storage.merkletree

import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.cache.StorageAware

internal interface StorageAwareMerkleTree : MutableMerkleTree, StorageAware {
    val merkleTree: MutableMerkleTree
}