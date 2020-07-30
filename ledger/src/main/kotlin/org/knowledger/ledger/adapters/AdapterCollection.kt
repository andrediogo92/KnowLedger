package org.knowledger.ledger.adapters

import org.knowledger.collections.SortedList
import org.knowledger.ledger.adapters.service.ServiceStorageAdapter
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.service.handles.ChainHandle
import org.knowledger.ledger.storage.*

internal interface AdapterCollection {
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
    val ledgerIdStorageAdapter: LedgerStorageAdapter<LedgerId>
    val ledgerParamsStorageAdapter: LedgerStorageAdapter<LedgerParams>
    val merkleTreeStorageAdapter: LedgerStorageAdapter<MutableMerkleTree>
    val physicalDataStorageAdapter: LedgerStorageAdapter<PhysicalData>
    val poolTransactionStorageAdapter: LedgerStorageAdapter<PoolTransaction>
    val transactionStorageAdapter: LedgerStorageAdapter<MutableTransaction>
    val transactionOutputStorageAdapter: LedgerStorageAdapter<TransactionOutput>
    val transactionPoolStorageAdapter: LedgerStorageAdapter<MutableTransactionPool>
    val witnessStorageAdapter: LedgerStorageAdapter<MutableWitness>
    val defaultSchemas: SortedList<SchemaProvider>
}