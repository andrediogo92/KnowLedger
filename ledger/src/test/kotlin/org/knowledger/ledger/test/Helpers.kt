package org.knowledger.ledger.test

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.StringFormat
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.knowledger.ledger.adapters.LedgerAdaptersProvider
import org.knowledger.ledger.chain.service.LedgerConfigurationService
import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.data.TemperatureData
import org.knowledger.ledger.data.TemperatureUnit
import org.knowledger.ledger.data.TrafficFlowData
import org.knowledger.ledger.storage.Factories
import org.knowledger.testing.core.random
import org.knowledger.testing.storage.defaultEncoder
import org.knowledger.testing.storage.defaultJson
import java.math.BigDecimal


val testSerializersModule: SerializersModule by lazy {
    SerializersModule {
        polymorphic(LedgerData::class, null) {
            subclass(TemperatureData::class, TemperatureData.serializer())
            subclass(TrafficFlowData::class, TrafficFlowData.serializer())
        }
    }
}


@OptIn(ExperimentalSerializationApi::class)
val testJson: StringFormat by lazy {
    defaultJson(testSerializersModule)
}

@OptIn(ExperimentalSerializationApi::class)
val testEncoder: BinaryFormat by lazy {
    defaultEncoder(testSerializersModule)
}

internal val factories: Factories by lazy {
    LedgerConfigurationService.factories
}

internal val LEDGER_ADAPTERS: LedgerAdaptersProvider by lazy {
    LedgerConfigurationService.ledgerAdapters
}

fun temperature(): LedgerData = TemperatureData(
    BigDecimal(random.nextDouble() * 100), TemperatureUnit.Celsius
)

fun trafficFlow(): LedgerData = TrafficFlowData(
    "FRC" + random.nextInt(6),
    random.nextInt(125), random.nextInt(125),
    random.nextInt(3000), random.nextInt(3000),
    random.nextDouble() * 34,
    random.nextDouble() * 12
)