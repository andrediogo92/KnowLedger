@file:UseSerializers(
    InstantSerializer::class, UUIDSerializer::class, HashSerializer::class
)

package org.knowledger.ledger.storage.config

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.Instant
import org.knowledger.ledger.core.UUID
import org.knowledger.ledger.core.calculateHash
import org.knowledger.ledger.core.nowUTC
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.core.serial.InstantSerializer
import org.knowledger.ledger.core.serial.UUIDSerializer
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashers
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.storage.HashSerializable
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.LedgerParams
import org.knowledger.ledger.storage.config.ledger.ImmutableLedgerParams
import org.knowledger.ledger.storage.immutableCopy

@OptIn(ExperimentalSerializationApi::class)
data class LedgerId(
    val tag: String, override val hash: Hash, val uuid: UUID,
    val instant: Instant, val ledgerParams: LedgerParams,
) : Hashing, LedgerContract {
    constructor(
        tag: String, hashers: Hashers, encoder: BinaryFormat, ledgerParams: LedgerParams,
    ) : this(
        tag, encoder, Builder(
            tag, hashers, UUID.randomUUID(), nowUTC(), ledgerParams.immutableCopy()
        )
    )

    private constructor(tag: String, encoder: BinaryFormat, builder: Builder) : this(
        tag, builder.calculateHash(builder.hashers, encoder),
        builder.uuid, builder.instant, builder.ledgerParams
    )

    @Serializable
    private data class Builder(
        val tag: String, val hashers: Hashers, val uuid: UUID,
        val instant: Instant, val ledgerParams: ImmutableLedgerParams,
    ) : HashSerializable {
        override fun serialize(encoder: BinaryFormat): ByteArray =
            encoder.encodeToByteArray(serializer(), this)
    }
}