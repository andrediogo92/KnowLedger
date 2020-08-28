package org.knowledger.ledger.storage.serial

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.knowledger.ledger.results.Failable
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.err
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.results.deadCode
import org.knowledger.ledger.results.Failure as CoreFailure

@OptIn(ExperimentalSerializationApi::class)
sealed class LedgerSerializer {

    data class Text(val serializer: StringFormat) : LedgerSerializer(), SerializableAs<String> {
        override fun <R> encode(strategy: SerializationStrategy<R>, element: R): String =
            serializer.encodeToString(strategy, element)

        override fun <R> decode(strategy: DeserializationStrategy<R>, element: String): R =
            serializer.decodeFromString(strategy, element)
    }

    data class Binary(val serializer: BinaryFormat) : LedgerSerializer(),
                                                      SerializableAs<ByteArray> {
        override fun <R> encode(strategy: SerializationStrategy<R>, element: R): ByteArray =
            serializer.encodeToByteArray(strategy, element)

        override fun <R> decode(strategy: DeserializationStrategy<R>, element: ByteArray): R =
            serializer.decodeFromByteArray(strategy, element)
    }

    sealed class Failure : CoreFailure {
        class NoEncoderSupplied(module: SerializersModule?, encoder: SerialFormat?) : Failure() {
            override val failable: Failable =
                Failable.LightFailure(
                    "Module or Encoder not supplied: Module -> $module, Encoder -> $encoder"
                )
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
class LedgerTextSerializerBuilder {
    var encoder: StringFormat? = null
    var shouldPrettyPrint: Boolean = false
    var module: SerializersModule? = null

    fun build(): Outcome<LedgerSerializer.Text, LedgerSerializer.Failure> =
        when {
            module != null && encoder == null -> {
                encoder = Json {
                    prettyPrint = shouldPrettyPrint
                    serializersModule = module as SerializersModule
                }

                LedgerSerializer.Text(encoder!!).ok()
            }
            encoder != null ->
                LedgerSerializer.Text(encoder!!).ok()
            module == null && encoder == null ->
                LedgerSerializer.Failure.NoEncoderSupplied(module, encoder).err()
            else -> deadCode()
        }
}


@OptIn(ExperimentalSerializationApi::class)
class LedgerBinarySerializerBuilder {
    var encoder: BinaryFormat? = null
    var module: SerializersModule? = null

    fun build(): Outcome<LedgerSerializer.Binary, LedgerSerializer.Failure> =
        when {
            module != null && encoder == null -> {
                encoder = Cbor {
                    encodeDefaults = false
                    serializersModule = module as SerializersModule
                }
                LedgerSerializer.Binary(encoder!!).ok()
            }
            encoder != null ->
                LedgerSerializer.Binary(encoder!!).ok()
            module == null && encoder == null ->
                LedgerSerializer.Failure.NoEncoderSupplied(module, encoder).err()
            else -> deadCode()
        }
}


inline fun ledgerTextSerializer(
    setup: LedgerTextSerializerBuilder.() -> Unit
): Outcome<LedgerSerializer.Text, LedgerSerializer.Failure> {
    val builder = LedgerTextSerializerBuilder()
    builder.setup()
    return builder.build()
}

inline fun ledgerBinarySerializer(
    setup: LedgerBinarySerializerBuilder.() -> Unit
): Outcome<LedgerSerializer.Binary, LedgerSerializer.Failure> {
    val builder = LedgerBinarySerializerBuilder()
    builder.setup()
    return builder.build()
}