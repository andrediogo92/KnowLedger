package org.knowledger.ledger.chain.service

import org.knowledger.ledger.chain.ChainInfo
import org.knowledger.ledger.chain.data.WitnessReference
import org.knowledger.ledger.storage.MutableCoinbase

interface CoinbaseService {
    fun recalculateMerkleTree(
        coinbase: MutableCoinbase, reference: WitnessReference, txIndex: Int, chainInfo: ChainInfo,
    )
}