package org.knowledger.ledger.storage.merkletree

import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.storage.MerkleTree
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.results.LoadFailure

internal fun MerkleTree.store(
    session: ManagedSession
): StorageElement =
    when (this) {
        is StorageAwareMerkleTree ->
            SAMerkleTreeStorageAdapter.store(this, session)
        is MerkleTreeImpl ->
            SUMerkleTreeStorageAdapter.store(this, session)
        else -> deadCode()
    }

internal fun StorageElement.loadMerkleTreeByImpl(
    ledgerHash: Hash
): Outcome<MerkleTree, LoadFailure> =
    SAMerkleTreeStorageAdapter.load(ledgerHash, this)