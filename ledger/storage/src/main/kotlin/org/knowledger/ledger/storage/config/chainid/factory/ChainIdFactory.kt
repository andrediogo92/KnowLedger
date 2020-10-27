package org.knowledger.ledger.storage.config.chainid.factory

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.ledger.core.adapters.Tag
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.ChainId
import org.knowledger.ledger.storage.CloningFactory
import org.knowledger.ledger.storage.CoinbaseParams

@OptIn(ExperimentalSerializationApi::class)
interface ChainIdFactory : CloningFactory<ChainId> {
    fun create(
        hash: Hash, ledgerHash: Hash, tag: Tag, rawTag: Hash,
        blockParams: BlockParams, coinbaseParams: CoinbaseParams,
    ): ChainId

    fun create(
        ledgerHash: Hash, tag: Tag, rawTag: Hash, hasher: Hashers, encoder: BinaryFormat,
        blockParams: BlockParams, coinbaseParams: CoinbaseParams,
    ): ChainId
}