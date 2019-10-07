package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.data.adapters.PhysicalDataStorageAdapter
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.TransactionOutput

internal fun PhysicalData.persist(
    session: ManagedSession
): StorageElement =
    PhysicalDataStorageAdapter.persist(
        this, session
    )

internal fun StorageElement.loadPhysicalData(
    ledgerHash: Hash
): Outcome<PhysicalData, LoadFailure> =
    PhysicalDataStorageAdapter.load(
        ledgerHash, this
    )

internal fun Transaction.persist(
    session: ManagedSession
): StorageElement =
    TransactionStorageAdapter.persist(
        this, session
    )

internal fun StorageElement.loadTransaction(
    ledgerHash: Hash
): Outcome<Transaction, LoadFailure> =
    TransactionStorageAdapter.load(
        ledgerHash, this
    )

internal fun Coinbase.persist(
    session: ManagedSession
): StorageElement =
    CoinbaseStorageAdapter.persist(
        this, session
    )

internal fun StorageElement.loadCoinbase(
    ledgerHash: Hash
): Outcome<Coinbase, LoadFailure> =
    CoinbaseStorageAdapter.load(
        ledgerHash, this
    )


internal fun TransactionOutput.persist(
    session: ManagedSession
): StorageElement =
    TransactionOutputStorageAdapter.persist(
        this, session
    )

internal fun StorageElement.loadTransactionOutput(
    ledgerHash: Hash
): Outcome<TransactionOutput, LoadFailure> =
    TransactionOutputStorageAdapter.load(
        ledgerHash, this
    )

internal fun BlockHeader.persist(
    session: ManagedSession
): StorageElement =
    BlockHeaderStorageAdapter.persist(
        this, session
    )

internal fun StorageElement.loadBlockHeader(
    ledgerHash: Hash
): Outcome<BlockHeader, LoadFailure> =
    BlockHeaderStorageAdapter.load(
        ledgerHash, this
    )


internal fun Block.persist(
    session: ManagedSession
): StorageElement =
    BlockStorageAdapter.persist(
        this, session
    )

internal fun StorageElement.loadBlock(
    ledgerHash: Hash
): Outcome<Block, LoadFailure> =
    BlockStorageAdapter.load(
        ledgerHash, this
    )

internal fun MerkleTree.persist(
    session: ManagedSession
): StorageElement =
    MerkleTreeStorageAdapter.persist(
        this, session
    )

internal fun StorageElement.loadMerkleTree(
    ledgerHash: Hash
): Outcome<MerkleTree, LoadFailure> =
    MerkleTreeStorageAdapter.load(
        ledgerHash, this
    )