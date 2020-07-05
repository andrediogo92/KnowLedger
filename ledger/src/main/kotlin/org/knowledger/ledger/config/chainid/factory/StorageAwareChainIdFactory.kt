package org.knowledger.ledger.config.chainid.factory

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.chainid.StorageAwareChainId
import org.knowledger.ledger.config.chainid.StorageAwareChainIdImpl
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.Tag

internal class StorageAwareChainIdFactory(
    private val factory: ChainIdFactory = ChainIdFactoryImpl
) : ChainIdFactory {

    private fun createSA(chainId: ChainId): StorageAwareChainId =
        StorageAwareChainIdImpl(chainId)

    override fun create(
        tag: Tag, ledgerHash: Hash, hash: Hash
    ): StorageAwareChainId = createSA(
        factory.create(tag, ledgerHash, hash)
    )

    override fun create(
        hasher: Hashers, encoder: BinaryFormat,
        tag: Tag, ledgerHash: Hash
    ): StorageAwareChainId = createSA(
        factory.create(hasher, encoder, tag, ledgerHash)
    )

    override fun create(other: ChainId): StorageAwareChainId =
        createSA(factory.create(other))
}