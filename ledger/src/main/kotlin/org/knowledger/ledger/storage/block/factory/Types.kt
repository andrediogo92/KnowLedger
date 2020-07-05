package org.knowledger.ledger.storage.block.factory

import org.knowledger.ledger.crypto.storage.MerkleTreeFactory
import org.knowledger.ledger.storage.block.header.factory.HashedBlockHeaderFactory
import org.knowledger.ledger.storage.coinbase.factory.CoinbaseFactory
import org.knowledger.ledger.storage.transaction.factory.HashedTransactionFactory

internal typealias FactoryConstructor = (
    CoinbaseFactory,
    HashedTransactionFactory,
    HashedBlockHeaderFactory,
    MerkleTreeFactory
) -> BlockFactory