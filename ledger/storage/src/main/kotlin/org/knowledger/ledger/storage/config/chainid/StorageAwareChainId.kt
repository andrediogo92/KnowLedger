package org.knowledger.ledger.storage.config.chainid

import org.knowledger.ledger.storage.cache.StorageAware

internal interface StorageAwareChainId : StorageAware,
                                         ChainId {
    val chainId: ChainId
}