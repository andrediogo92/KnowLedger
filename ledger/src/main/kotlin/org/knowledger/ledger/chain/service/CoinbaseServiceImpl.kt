package org.knowledger.ledger.chain.service

import org.knowledger.ledger.chain.ChainInfo
import org.knowledger.ledger.chain.data.WitnessReference
import org.knowledger.ledger.storage.MutableCoinbase

class CoinbaseServiceImpl : CoinbaseService {
    override fun recalculateMerkleTree(
        coinbase: MutableCoinbase, reference: WitnessReference, txIndex: Int, chainInfo: ChainInfo,
    ) {
        coinbase.merkleTree.applySingleDiff(coinbase.witnesses[reference.index], txIndex)
        coinbase.coinbaseHeader.updateMerkleRoot(coinbase.merkleTree.hash)
        val newHash = coinbase.coinbaseHeader.digest(chainInfo.hashers, chainInfo.encoder)
        coinbase.coinbaseHeader.updateHash(newHash)
    }
}