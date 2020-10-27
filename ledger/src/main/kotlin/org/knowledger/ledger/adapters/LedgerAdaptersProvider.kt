package org.knowledger.ledger.adapters

import org.knowledger.collections.SortedList
import org.knowledger.ledger.adapters.service.HandleStorageAdapter
import org.knowledger.ledger.adapters.service.ServiceLoadableCollection
import org.knowledger.ledger.adapters.service.ServiceStorageAdapter
import org.knowledger.ledger.chain.handles.ChainHandle
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.ChainId
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.Identity
import org.knowledger.ledger.storage.LedgerId
import org.knowledger.ledger.storage.LedgerParams
import org.knowledger.ledger.storage.MutableBlock
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.MutableBlockPool
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.MutableTransactionPool
import org.knowledger.ledger.storage.MutableWitness
import org.knowledger.ledger.storage.PhysicalData
import org.knowledger.ledger.storage.PoolTransaction
import org.knowledger.ledger.storage.TransactionOutput

internal interface LedgerAdaptersProvider : ServiceLoadableCollection {
    val blockStorageAdapter: LedgerStorageAdapter<MutableBlock>
    val blockHeaderStorageAdapter: LedgerStorageAdapter<MutableBlockHeader>
    val blockParamsStorageAdapter: LedgerStorageAdapter<BlockParams>
    val blockPoolStorageAdapter: LedgerStorageAdapter<MutableBlockPool>
    val chainIdStorageAdapter: LedgerStorageAdapter<ChainId>
    val chainHandleStorageAdapter: ServiceStorageAdapter<ChainHandle>
    val coinbaseStorageAdapter: LedgerStorageAdapter<MutableCoinbase>
    val coinbaseHeaderStorageAdapter: LedgerStorageAdapter<MutableCoinbaseHeader>
    val coinbaseParamsStorageAdapter: LedgerStorageAdapter<CoinbaseParams>
    val identityStorageAdapter: LedgerStorageAdapter<Identity>
    val ledgerIdStorageAdapter: HandleStorageAdapter<LedgerId>
    val ledgerParamsStorageAdapter: HandleStorageAdapter<LedgerParams>
    val merkleTreeStorageAdapter: LedgerStorageAdapter<MutableMerkleTree>
    val physicalDataStorageAdapter: LedgerStorageAdapter<PhysicalData>
    val poolTransactionStorageAdapter: LedgerStorageAdapter<PoolTransaction>
    val transactionStorageAdapter: LedgerStorageAdapter<MutableTransaction>
    val transactionOutputStorageAdapter: LedgerStorageAdapter<TransactionOutput>
    val transactionPoolStorageAdapter: LedgerStorageAdapter<MutableTransactionPool>
    val witnessStorageAdapter: LedgerStorageAdapter<MutableWitness>
    val defaultSchemas: SortedList<SchemaProvider>
}