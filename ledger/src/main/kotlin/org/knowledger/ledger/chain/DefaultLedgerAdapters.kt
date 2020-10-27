package org.knowledger.ledger.chain

import org.knowledger.collections.SortedList
import org.knowledger.collections.sortedListOf
import org.knowledger.ledger.adapters.LedgerAdaptersProvider
import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.adapters.config.BlockParamsStorageAdapter
import org.knowledger.ledger.adapters.config.ChainIdStorageAdapter
import org.knowledger.ledger.adapters.config.CoinbaseParamsStorageAdapter
import org.knowledger.ledger.adapters.config.LedgerIdStorageAdapter
import org.knowledger.ledger.adapters.config.LedgerParamsStorageAdapter
import org.knowledger.ledger.adapters.pools.BlockPoolStorageAdapter
import org.knowledger.ledger.adapters.pools.PoolTransactionStorageAdapter
import org.knowledger.ledger.adapters.pools.TransactionPoolStorageAdapter
import org.knowledger.ledger.adapters.service.ChainHandleStorageAdapter
import org.knowledger.ledger.adapters.service.HandleStorageAdapter
import org.knowledger.ledger.adapters.service.IdentityStorageAdapter
import org.knowledger.ledger.adapters.service.ServiceLoadable
import org.knowledger.ledger.adapters.service.ServiceStorageAdapter
import org.knowledger.ledger.adapters.service.loadables.TransactionWithBlockHashStorageLoadable
import org.knowledger.ledger.adapters.service.loadables.WitnessInfoServiceLoadable
import org.knowledger.ledger.adapters.storage.BlockHeaderStorageAdapter
import org.knowledger.ledger.adapters.storage.BlockStorageAdapter
import org.knowledger.ledger.adapters.storage.CoinbaseHeaderStorageAdapter
import org.knowledger.ledger.adapters.storage.CoinbaseStorageAdapter
import org.knowledger.ledger.adapters.storage.MerkleTreeStorageAdapter
import org.knowledger.ledger.adapters.storage.PhysicalDataStorageAdapter
import org.knowledger.ledger.adapters.storage.TransactionOutputStorageAdapter
import org.knowledger.ledger.adapters.storage.TransactionStorageAdapter
import org.knowledger.ledger.adapters.storage.WitnessStorageAdapter
import org.knowledger.ledger.chain.data.TransactionWithBlockHash
import org.knowledger.ledger.chain.data.WitnessInfo
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

internal class DefaultLedgerAdapters : LedgerAdaptersProvider {
    override val blockStorageAdapter: LedgerStorageAdapter<MutableBlock> =
        BlockStorageAdapter()
    override val blockHeaderStorageAdapter: LedgerStorageAdapter<MutableBlockHeader> =
        BlockHeaderStorageAdapter()
    override val blockParamsStorageAdapter: LedgerStorageAdapter<BlockParams> =
        BlockParamsStorageAdapter()
    override val blockPoolStorageAdapter: LedgerStorageAdapter<MutableBlockPool> =
        BlockPoolStorageAdapter()
    override val chainIdStorageAdapter: LedgerStorageAdapter<ChainId> =
        ChainIdStorageAdapter()
    override val chainHandleStorageAdapter: ServiceStorageAdapter<ChainHandle> =
        ChainHandleStorageAdapter()
    override val coinbaseStorageAdapter: LedgerStorageAdapter<MutableCoinbase> =
        CoinbaseStorageAdapter()
    override val coinbaseHeaderStorageAdapter: LedgerStorageAdapter<MutableCoinbaseHeader> =
        CoinbaseHeaderStorageAdapter()
    override val coinbaseParamsStorageAdapter: LedgerStorageAdapter<CoinbaseParams> =
        CoinbaseParamsStorageAdapter()
    override val identityStorageAdapter: LedgerStorageAdapter<Identity> =
        IdentityStorageAdapter()
    override val ledgerIdStorageAdapter: HandleStorageAdapter<LedgerId> =
        LedgerIdStorageAdapter()
    override val ledgerParamsStorageAdapter: HandleStorageAdapter<LedgerParams> =
        LedgerParamsStorageAdapter()
    override val merkleTreeStorageAdapter: LedgerStorageAdapter<MutableMerkleTree> =
        MerkleTreeStorageAdapter()
    override val physicalDataStorageAdapter: LedgerStorageAdapter<PhysicalData> =
        PhysicalDataStorageAdapter()
    override val poolTransactionStorageAdapter: LedgerStorageAdapter<PoolTransaction> =
        PoolTransactionStorageAdapter()
    override val transactionStorageAdapter: LedgerStorageAdapter<MutableTransaction> =
        TransactionStorageAdapter()
    override val transactionOutputStorageAdapter: LedgerStorageAdapter<TransactionOutput> =
        TransactionOutputStorageAdapter()
    override val transactionPoolStorageAdapter: LedgerStorageAdapter<MutableTransactionPool> =
        TransactionPoolStorageAdapter()
    override val witnessStorageAdapter: LedgerStorageAdapter<MutableWitness> =
        WitnessStorageAdapter()
    override val transactionWithBlockHashStorageLoadable: ServiceLoadable<TransactionWithBlockHash> =
        TransactionWithBlockHashStorageLoadable()
    override val witnessInfoServiceLoadable: ServiceLoadable<WitnessInfo> =
        WitnessInfoServiceLoadable()

    override val defaultSchemas: SortedList<SchemaProvider> = sortedListOf(
        blockStorageAdapter, blockHeaderStorageAdapter, blockParamsStorageAdapter,
        blockPoolStorageAdapter, chainIdStorageAdapter, chainHandleStorageAdapter,
        coinbaseStorageAdapter, coinbaseHeaderStorageAdapter, coinbaseParamsStorageAdapter,
        identityStorageAdapter, ledgerIdStorageAdapter, ledgerParamsStorageAdapter,
        merkleTreeStorageAdapter, physicalDataStorageAdapter, poolTransactionStorageAdapter,
        transactionStorageAdapter, transactionOutputStorageAdapter, transactionPoolStorageAdapter,
        witnessStorageAdapter
    )
}