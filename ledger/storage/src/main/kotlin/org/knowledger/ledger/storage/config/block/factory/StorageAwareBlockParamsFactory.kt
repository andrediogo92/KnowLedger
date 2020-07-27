package org.knowledger.ledger.storage.config.block.factory

import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.config.block.StorageAwareBlockParamsImpl

internal class StorageAwareBlockParamsFactory(
    private val factory: BlockParamsFactory = BlockParamsFactoryImpl()
) : BlockParamsFactory {
    private fun createSA(blockParams: BlockParams): StorageAwareBlockParamsImpl =
        StorageAwareBlockParamsImpl(blockParams)

    override fun create(blockMemorySize: Int, blockLength: Int): StorageAwareBlockParamsImpl =
        createSA(factory.create(blockMemorySize, blockLength))


    override fun create(other: BlockParams): StorageAwareBlockParamsImpl =
        createSA(factory.create(other))
}