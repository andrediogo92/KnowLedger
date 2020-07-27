package org.knowledger.ledger.storage.config.chainid.factory

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.core.calculateHash
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.ChainId
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.config.chainid.ChainIdBuilder
import org.knowledger.ledger.storage.config.chainid.ImmutableChainId

internal class ChainIdFactoryImpl : ChainIdFactory {
    private fun generateChainHandleHash(
        ledgerHash: Hash, tag: Hash,
        hasher: Hashers, encoder: BinaryFormat,
        blockParams: BlockParams, coinbaseParams: CoinbaseParams
    ): Hash = ChainIdBuilder(
        ledgerHash, tag, blockParams, coinbaseParams
    ).calculateHash(hasher, encoder)

    override fun create(
        hash: Hash, ledgerHash: Hash, tag: Hash,
        blockParams: BlockParams, coinbaseParams: CoinbaseParams
    ): ImmutableChainId = ImmutableChainId(
        hash, ledgerHash, tag, blockParams, coinbaseParams
    )

    override fun create(
        ledgerHash: Hash, tag: Hash,
        hasher: Hashers, encoder: BinaryFormat,
        blockParams: BlockParams, coinbaseParams: CoinbaseParams
    ): ImmutableChainId = create(
        generateChainHandleHash(
            ledgerHash, tag, hasher, encoder,
            blockParams, coinbaseParams
        ), ledgerHash, tag, blockParams, coinbaseParams
    )

    override fun create(other: ChainId): ImmutableChainId =
        with(other) {
            create(hash, ledgerHash, tag, blockParams, coinbaseParams)
        }

}