package org.knowledger.ledger.adapters

import org.knowledger.base64.Base64String
import org.knowledger.base64.base64Encoded
import org.knowledger.ledger.config.adapters.BlockParamsStorageAdapter
import org.knowledger.ledger.config.adapters.ChainIdStorageAdapter
import org.knowledger.ledger.config.adapters.LedgerIdStorageAdapter
import org.knowledger.ledger.config.adapters.LedgerParamsStorageAdapter
import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.data.LedgerData
import org.knowledger.ledger.data.Tag
import org.knowledger.ledger.data.adapters.DummyDataStorageAdapter
import org.knowledger.ledger.data.adapters.PhysicalDataStorageAdapter
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.service.adapters.ChainHandleStorageAdapter
import org.knowledger.ledger.service.adapters.IdentityStorageAdapter
import org.knowledger.ledger.service.adapters.LedgerConfigStorageAdapter
import org.knowledger.ledger.service.adapters.PoolTransactionStorageAdapter
import org.knowledger.ledger.service.adapters.TransactionPoolStorageAdapter
import org.knowledger.ledger.service.pools.transaction.SATransactionPoolStorageAdapter
import org.knowledger.ledger.service.pools.transaction.SUTransactionPoolStorageAdapter
import org.knowledger.ledger.storage.adapters.BlockHeaderStorageAdapter
import org.knowledger.ledger.storage.adapters.BlockStorageAdapter
import org.knowledger.ledger.storage.adapters.CoinbaseStorageAdapter
import org.knowledger.ledger.storage.adapters.MerkleTreeStorageAdapter
import org.knowledger.ledger.storage.adapters.TransactionOutputStorageAdapter
import org.knowledger.ledger.storage.adapters.TransactionStorageAdapter
import org.knowledger.ledger.storage.block.SABlockStorageAdapter
import org.knowledger.ledger.storage.block.SUBlockStorageAdapter
import org.knowledger.ledger.storage.blockheader.SABlockHeaderStorageAdapter
import org.knowledger.ledger.storage.blockheader.SUBlockHeaderStorageAdapter
import org.knowledger.ledger.storage.coinbase.SACoinbaseStorageAdapter
import org.knowledger.ledger.storage.coinbase.SUCoinbaseStorageAdapter
import org.knowledger.ledger.storage.merkletree.SAMerkleTreeStorageAdapter
import org.knowledger.ledger.storage.merkletree.SUMerkleTreeStorageAdapter
import org.knowledger.ledger.storage.transaction.SATransactionStorageAdapter
import org.knowledger.ledger.storage.transaction.SUTransactionStorageAdapter
import org.knowledger.ledger.storage.transaction.output.SATransactionOutputStorageAdapter
import org.knowledger.ledger.storage.transaction.output.SUTransactionOutputStorageAdapter

internal class AdapterManager(
    container: LedgerInfo,
    private val adapters: MutableSet<AbstractStorageAdapter<out LedgerData>> =
        mutableSetOf(DummyDataStorageAdapter(container.hasher))
) : AdapterCollection {
    override val physicalDataStorageAdapter: PhysicalDataStorageAdapter =
        PhysicalDataStorageAdapter(this)
    override val transactionStorageAdapter: TransactionStorageAdapter
    override val transactionOutputStorageAdapter: TransactionOutputStorageAdapter
    override val merkleTreeStorageAdapter: MerkleTreeStorageAdapter
    override val coinbaseStorageAdapter: CoinbaseStorageAdapter
    override val blockHeaderStorageAdapter: BlockHeaderStorageAdapter
    override val blockStorageAdapter: BlockStorageAdapter

    init {
        val suTransactionStorageAdapter =
            SUTransactionStorageAdapter(physicalDataStorageAdapter)
        transactionStorageAdapter = TransactionStorageAdapter(
            suTransactionStorageAdapter,
            SATransactionStorageAdapter(suTransactionStorageAdapter)
        )
    }

    init {
        val suTransactionOutputStorageAdapter =
            SUTransactionOutputStorageAdapter(container)
        transactionOutputStorageAdapter = TransactionOutputStorageAdapter(
            suTransactionOutputStorageAdapter,
            SATransactionOutputStorageAdapter(suTransactionOutputStorageAdapter)
        )
    }

    init {
        val suMerkleTreeStorageAdapter =
            SUMerkleTreeStorageAdapter(container.hasher)
        merkleTreeStorageAdapter = MerkleTreeStorageAdapter(
            suMerkleTreeStorageAdapter,
            SAMerkleTreeStorageAdapter(suMerkleTreeStorageAdapter)
        )
    }

    init {
        val suCoinbaseStorageAdapter = SUCoinbaseStorageAdapter(
            container, transactionOutputStorageAdapter
        )
        coinbaseStorageAdapter = CoinbaseStorageAdapter(
            suCoinbaseStorageAdapter,
            SACoinbaseStorageAdapter(suCoinbaseStorageAdapter)
        )
    }

    init {
        val suBlockHeaderStorageAdapter =
            SUBlockHeaderStorageAdapter(container)
        blockHeaderStorageAdapter = BlockHeaderStorageAdapter(
            suBlockHeaderStorageAdapter,
            SABlockHeaderStorageAdapter(suBlockHeaderStorageAdapter)
        )
    }

    init {
        val suBlockStorageAdapter = SUBlockStorageAdapter(
            transactionStorageAdapter, coinbaseStorageAdapter,
            blockHeaderStorageAdapter, merkleTreeStorageAdapter
        )
        blockStorageAdapter = BlockStorageAdapter(
            suBlockStorageAdapter,
            SABlockStorageAdapter(this, suBlockStorageAdapter)
        )
    }

    override val poolTransactionStorageAdapter: PoolTransactionStorageAdapter =
        PoolTransactionStorageAdapter(
            this, transactionStorageAdapter
        )

    override val transactionPoolStorageAdapter: TransactionPoolStorageAdapter


    init {
        val suTransactionPoolStorageAdapter = SUTransactionPoolStorageAdapter(
            this, poolTransactionStorageAdapter
        )
        transactionPoolStorageAdapter = TransactionPoolStorageAdapter(
            suTransactionPoolStorageAdapter,
            SATransactionPoolStorageAdapter(this, suTransactionPoolStorageAdapter)
        )
    }

    override val chainHandleStorageAdapter: ChainHandleStorageAdapter =
        ChainHandleStorageAdapter(this, container, transactionPoolStorageAdapter)

    override val dataAdapters: Set<AbstractStorageAdapter<out LedgerData>>
        get() = adapters

    override val defaultAdapters: MutableSet<SchemaProvider>
        get() = mutableSetOf(
            //Storage Adapters
            blockStorageAdapter,
            blockHeaderStorageAdapter,
            coinbaseStorageAdapter,
            merkleTreeStorageAdapter,
            physicalDataStorageAdapter,
            transactionStorageAdapter,
            transactionOutputStorageAdapter,
            //Configuration Adapters
            BlockParamsStorageAdapter,
            ChainIdStorageAdapter,
            LedgerConfigStorageAdapter,
            LedgerIdStorageAdapter,
            LedgerParamsStorageAdapter,
            //Service Adapters
            chainHandleStorageAdapter,
            poolTransactionStorageAdapter,
            transactionPoolStorageAdapter,
            IdentityStorageAdapter
        )


    fun findAdapter(tag: Tag): AbstractStorageAdapter<out LedgerData>? =
        findAdapter(tag.base64Encoded())

    fun findAdapter(tag: Base64String): AbstractStorageAdapter<out LedgerData>? =
        adapters.find {
            it.id == tag
        }

    fun findAdapter(clazz: Class<*>): AbstractStorageAdapter<out LedgerData>? =
        adapters.find {
            it.clazz == clazz
        }

    internal fun addAdapter(
        adapter: AbstractStorageAdapter<out LedgerData>
    ): Boolean =
        adapters.add(adapter)

    fun hasAdapter(tag: String): Boolean =
        adapters.any {
            it.id == tag
        }

    fun hasAdapter(tag: Tag): Boolean =
        hasAdapter(tag.base64Encoded())

    fun hasAdapter(clazz: Class<*>): Boolean =
        adapters.any {
            it.clazz == clazz
        }

}