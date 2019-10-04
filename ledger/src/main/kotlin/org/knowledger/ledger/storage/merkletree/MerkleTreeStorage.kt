package org.knowledger.ledger.storage.merkletree

import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.MerkleTree

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