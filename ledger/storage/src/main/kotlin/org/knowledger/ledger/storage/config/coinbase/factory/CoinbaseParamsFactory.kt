package org.knowledger.ledger.storage.config.coinbase.factory

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.digest.classDigest
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.CloningFactory
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.DefaultDiff

interface CoinbaseParamsFactory : CloningFactory<CoinbaseParams> {
    fun create(
        hashSize: Int, timeIncentive: Long = 5, valueIncentive: Long = 2,
        baseIncentive: Long = 3, dividingThreshold: Long = 100000,
        formula: Hash = classDigest<DefaultDiff>(Hashers.SHA3512Hasher),
    ): CoinbaseParams
}