package org.knowledger.ledger.json

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import org.knowledger.common.data.LedgerData
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.StorageAwareChainId
import org.knowledger.ledger.config.StorageUnawareChainId
import org.knowledger.ledger.data.DummyData
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.StorageAwareBlock
import org.knowledger.ledger.storage.StorageUnawareBlock


fun Moshi.Builder.addLedgerAdapters(
    ledgerDataTypes: Map<Class<out LedgerData>, String>
): Moshi.Builder {
    var ledgerAdapter = PolymorphicJsonAdapterFactory
        .of(LedgerData::class.java, "type")
        .withSubtype(DummyData::class.java, "Dummy")
    ledgerDataTypes.forEach { entry ->
        ledgerAdapter = ledgerAdapter.withSubtype(entry.key, entry.value)
    }
    return add(ledgerAdapter)
        .add(HashJsonAdapter())
        .add(PublicKeyJsonAdapter())
        .add(InstantJsonAdapter())
        .add(BigDecimalJsonAdapter())
        .add(BigIntegerJsonAdapter())
        .add(
            PolymorphicJsonAdapterFactory
                .of(Block::class.java, "type")
                .withSubtype(StorageAwareBlock::class.java, "storageAware")
                .withSubtype(StorageUnawareBlock::class.java, "storageUnaware")
        )
        .add(
            PolymorphicJsonAdapterFactory
                .of(ChainId::class.java, "type")
                .withSubtype(StorageAwareChainId::class.java, "storageAware")
                .withSubtype(StorageUnawareChainId::class.java, "storageUnaware")
        )
        .add(SortedSetJsonAdapterFactory)

}
