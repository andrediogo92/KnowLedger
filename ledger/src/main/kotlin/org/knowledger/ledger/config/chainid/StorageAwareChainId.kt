package org.knowledger.ledger.config.chainid

import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.storage.StorageAware

internal interface StorageAwareChainId : StorageAware, ChainId {
    val chainId: ChainId
}