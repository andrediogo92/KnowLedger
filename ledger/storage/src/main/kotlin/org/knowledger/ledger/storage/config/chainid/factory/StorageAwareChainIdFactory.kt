package org.knowledger.ledger.storage.config.chainid.factory

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.ChainId
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.Tag
import org.knowledger.ledger.storage.config.chainid.StorageAwareChainIdImpl

internal class StorageAwareChainIdFactory(
    private val factory: ChainIdFactory = ChainIdFactoryImpl()
) : ChainIdFactory {

    private fun createSA(chainId: ChainId): StorageAwareChainIdImpl =
        StorageAwareChainIdImpl(chainId)

    override fun create(
        hash: Hash, ledgerHash: Hash, tag: Tag,
        blockParams: BlockParams, coinbaseParams: CoinbaseParams
    ): StorageAwareChainIdImpl = createSA(
        factory.create(hash, ledgerHash, tag, blockParams, coinbaseParams)
    )

    override fun create(
        ledgerHash: Hash, tag: Tag,
        hasher: Hashers, encoder: BinaryFormat,
        blockParams: BlockParams, coinbaseParams: CoinbaseParams
    ): StorageAwareChainIdImpl = createSA(
        factory.create(ledgerHash, tag, hasher, encoder, blockParams, coinbaseParams)
    )

    override fun create(other: ChainId): StorageAwareChainIdImpl =
        createSA(factory.create(other))
}