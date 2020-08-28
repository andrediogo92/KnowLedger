package org.knowledger.ledger.storage.config.block.factory

import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.CloningFactory

interface BlockParamsFactory : CloningFactory<BlockParams> {
    fun create(blockMemorySize: Int = 2097152, blockLength: Int = 512): BlockParams
}