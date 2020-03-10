package org.knowledger.ledger.adapters

import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.data.LedgerData
import org.knowledger.ledger.data.adapters.PhysicalDataStorageAdapter
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.service.adapters.ChainHandleStorageAdapter
import org.knowledger.ledger.service.adapters.PoolTransactionStorageAdapter
import org.knowledger.ledger.service.adapters.TransactionPoolStorageAdapter
import org.knowledger.ledger.service.adapters.TransactionWithBlockHashStorageLoadable
import org.knowledger.ledger.storage.adapters.BlockHeaderStorageAdapter
import org.knowledger.ledger.storage.adapters.BlockStorageAdapter
import org.knowledger.ledger.storage.adapters.CoinbaseStorageAdapter
import org.knowledger.ledger.storage.adapters.MerkleTreeStorageAdapter
import org.knowledger.ledger.storage.adapters.TransactionOutputStorageAdapter
import org.knowledger.ledger.storage.adapters.TransactionStorageAdapter
import org.knowledger.ledger.storage.adapters.WitnessStorageAdapter

internal interface AdapterCollection {
    //Storage Adapters
    val blockStorageAdapter: BlockStorageAdapter
    val blockHeaderStorageAdapter: BlockHeaderStorageAdapter
    val coinbaseStorageAdapter: CoinbaseStorageAdapter
    val merkleTreeStorageAdapter: MerkleTreeStorageAdapter
    val physicalDataStorageAdapter: PhysicalDataStorageAdapter
    val transactionStorageAdapter: TransactionStorageAdapter
    val transactionOutputStorageAdapter: TransactionOutputStorageAdapter
    val transactionWithBlockHashStorageLoadable: TransactionWithBlockHashStorageLoadable
    val witnessStorageAdapter: WitnessStorageAdapter

    //Service Adapters
    val poolTransactionStorageAdapter: PoolTransactionStorageAdapter
    val transactionPoolStorageAdapter: TransactionPoolStorageAdapter
    val chainHandleStorageAdapter: ChainHandleStorageAdapter

    //Data Adapters
    val dataAdapters: Set<AbstractStorageAdapter<out LedgerData>>

    val defaultSchemas: MutableSet<SchemaProvider>

    val allSchemaProviders: Set<SchemaProvider>
        get() = defaultSchemas + dataAdapters
}