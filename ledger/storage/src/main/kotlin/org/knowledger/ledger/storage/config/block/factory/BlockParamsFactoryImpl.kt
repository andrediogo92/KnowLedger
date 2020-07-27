package org.knowledger.ledger.storage.config.block.factory

import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.config.block.ImmutableBlockParams

internal class BlockParamsFactoryImpl : BlockParamsFactory {
    override fun create(
        blockMemorySize: Int, blockLength: Int
    ): ImmutableBlockParams =
        ImmutableBlockParams(blockMemorySize, blockLength)

    override fun create(other: BlockParams): BlockParams =
        with(other) { create(blockMemorySize, blockLength) }
}