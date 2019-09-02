@file:UseSerializers(InstantSerializer::class, UUIDSerializer::class)

package org.knowledger.ledger.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.hash.Hashing
import org.knowledger.ledger.core.serial.HashSerializable
import org.knowledger.ledger.core.serial.InstantSerializer
import org.knowledger.ledger.core.serial.UUIDSerializer
import org.knowledger.ledger.service.ServiceClass
import java.time.Instant
import java.util.*

@Serializable
data class LedgerId(
    val tag: String,
    override val hash: Hash
) : Hashing, ServiceClass {

    internal constructor(
        tag: String,
        hasher: Hasher,
        cbor: Cbor
    ) : this(tag, generateLedgerHandleHash(hasher, cbor, tag))

    @Serializable
    private data class LedgerBuilder(
        val tag: String, val uuid: UUID,
        val instant: Instant
    ) : HashSerializable {
        override fun serialize(cbor: Cbor): ByteArray =
            cbor.dump(serializer(), this)
    }

    companion object {
        private fun generateLedgerHandleHash(
            hasher: Hasher,
            cbor: Cbor,
            tag: String
        ): Hash =
            LedgerBuilder(
                tag,
                UUID.randomUUID(),
                Instant.now()
            ).digest(hasher, cbor)
    }
}