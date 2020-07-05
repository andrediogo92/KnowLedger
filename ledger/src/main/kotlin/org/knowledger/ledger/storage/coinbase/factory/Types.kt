package org.knowledger.ledger.storage.coinbase.factory

import org.knowledger.ledger.crypto.storage.MerkleTreeFactory
import org.knowledger.ledger.storage.coinbase.header.factory.CoinbaseHeaderFactory
import org.knowledger.ledger.storage.witness.factory.HashedWitnessFactory

internal typealias FactoryConstructor = (MerkleTreeFactory, CoinbaseHeaderFactory, HashedWitnessFactory) -> CoinbaseFactory