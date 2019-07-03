package org.knowledger.ledger.json

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import org.knowledger.common.data.LedgerData
import org.knowledger.ledger.data.DummyData


fun Moshi.Builder.addLedgerAdapters(
    ledgerDataTypes: Map<Class<out LedgerData>, String>
): Moshi.Builder {
    var poly = PolymorphicJsonAdapterFactory
        .of(LedgerData::class.java, "type")
        .withSubtype(DummyData::class.java, "Dummy")
    ledgerDataTypes.forEach { entry ->
        poly = poly.withSubtype(entry.key, entry.value)
    }
    return add(HashJsonAdapter())
        .add(PublicKeyJsonAdapter())
        .add(InstantJsonAdapter())
        .add(BigDecimalJsonAdapter())
        .add(BigIntegerJsonAdapter())
        .add(poly)
        .add(SortedSetJsonAdapterFactory)

}
