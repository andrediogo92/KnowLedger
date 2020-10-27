package org.knowledger.ledger.chain

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.SerializersModule
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.DataFormula
import org.knowledger.ledger.storage.DefaultDiff
import org.knowledger.ledger.storage.Factories
import org.knowledger.ledger.storage.config.LedgerId

@OptIn(ExperimentalSerializationApi::class)
data class LedgerInfo constructor(
    val ledgerId: LedgerId,
    val hashers: Hashers,
    val serializersModule: SerializersModule,
    val factories: Factories,
    val formula: DataFormula = DefaultDiff(hashers),
    val encoder: BinaryFormat = Cbor { this.serializersModule = serializersModule },
)