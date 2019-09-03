package org.knowledger.ledger.serial

import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.chainid.StorageAwareChainId
import org.knowledger.ledger.config.chainid.StorageUnawareChainId
import org.knowledger.ledger.core.data.DataFormula
import org.knowledger.ledger.core.data.DefaultDiff
import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.misc.mapToArray
import org.knowledger.ledger.crypto.serial.DefaultDataFormulaSerializer
import org.knowledger.ledger.crypto.storage.MerkleTree
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.data.DummyData
import org.knowledger.ledger.service.pools.transaction.StorageAwareTransactionPool
import org.knowledger.ledger.service.pools.transaction.TransactionPool
import org.knowledger.ledger.service.pools.transaction.TransactionPoolImpl
import org.knowledger.ledger.storage.block.Block
import org.knowledger.ledger.storage.block.BlockImpl
import org.knowledger.ledger.storage.block.StorageAwareBlock
import org.knowledger.ledger.storage.blockheader.HashedBlockHeader
import org.knowledger.ledger.storage.blockheader.HashedBlockHeaderImpl
import org.knowledger.ledger.storage.blockheader.StorageAwareBlockHeader
import org.knowledger.ledger.storage.coinbase.HashedCoinbase
import org.knowledger.ledger.storage.coinbase.HashedCoinbaseImpl
import org.knowledger.ledger.storage.coinbase.StorageAwareCoinbase
import org.knowledger.ledger.storage.merkletree.StorageAwareMerkleTree

fun <T> Array<T>.serial() where T : SerialEnum, T : Enum<T> =
    mapToArray { it.serialName }

fun <T> Array<T>.lowercase() where T : Enum<T> =
    mapToArray { it.name.toLowerCase() }

interface SerialEnum {
    val serialName: String
}

val baseModule: SerialModule = SerializersModule {
    polymorphic(Block::class) {
        BlockImpl::class with BlockImpl.serializer()
        StorageAwareBlock::class with StorageAwareBlock.serializer()
    }
    polymorphic(HashedBlockHeader::class) {
        HashedBlockHeaderImpl::class with HashedBlockHeaderImpl.serializer()
        StorageAwareBlockHeader::class with StorageAwareBlockHeader.serializer()
    }
    polymorphic(MerkleTree::class) {
        MerkleTreeImpl::class with MerkleTreeImpl.serializer()
        StorageAwareMerkleTree::class with StorageAwareMerkleTree.serializer()
    }
    polymorphic(HashedCoinbase::class) {
        HashedCoinbaseImpl::class with HashedCoinbaseImpl.serializer()
        StorageAwareCoinbase::class with StorageAwareCoinbase.serializer()
    }
    polymorphic(ChainId::class) {
        StorageAwareChainId::class with StorageAwareChainId.serializer()
        StorageUnawareChainId::class with StorageUnawareChainId.serializer()
    }
    polymorphic(TransactionPool::class) {
        StorageAwareTransactionPool::class with StorageAwareTransactionPool.serializer()
        TransactionPoolImpl::class with TransactionPoolImpl.serializer()
    }
    /*
    polymorphic(BlockPool::class) {
        StorageAwareBlockPool::class with StorageAwareBlockPool.serializer()
        StorageUnawareBlockPool::class with StorageUnawareBlockPool.serializer()
    }
    */
}

inline fun SerialModule.withLedger(
    crossinline with: PolymorphicModuleBuilder<Any>.() -> Unit
): SerialModule =
    overwriteWith(SerializersModule {
        polymorphic(LedgerData::class) {
            with()
            DummyData::class with DummyDataSerializer
        }
    })

inline fun SerialModule.withDataFormulas(
    crossinline with: PolymorphicModuleBuilder<Any>.() -> Unit
): SerialModule =
    overwriteWith(SerializersModule {
        polymorphic(DataFormula::class) {
            with()
            DefaultDiff::class with DefaultDataFormulaSerializer
        }
    })