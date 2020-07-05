package org.knowledger.ledger.serial

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerialModule
import org.knowledger.ledger.results.Failable
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.results.Failure as CoreFailure

sealed class LedgerSerializer {

    data class Text(
        val serializer: StringFormat
    ) : LedgerSerializer(), SerializableAs<String> {
        override fun <R> encode(strategy: SerializationStrategy<R>, element: R): String =
            serializer.stringify(strategy, element)

        override fun <R> decode(strategy: DeserializationStrategy<R>, element: String): R =
            serializer.parse(strategy, element)
    }

    data class Binary(
        val serializer: BinaryFormat
    ) : LedgerSerializer(), SerializableAs<ByteArray> {
        override fun <R> encode(strategy: SerializationStrategy<R>, element: R): ByteArray =
            serializer.dump(strategy, element)

        override fun <R> decode(strategy: DeserializationStrategy<R>, element: ByteArray): R =
            serializer.load(strategy, element)
    }

    sealed class Failure : CoreFailure {
        class NoEncoderSupplied(module: SerialModule?, encoder: SerialFormat?) : Failure() {
            override val failable: Failable =
                Failable.LightFailure(
                    "Module or Encoder not supplied: Module -> $module, Encoder -> $encoder"
                )
        }
    }
}

class LedgerTextSerializerBuilder {
    var encoder: StringFormat? = null
    var prettyPrint: Boolean = false
    var module: SerialModule? = null

    @OptIn(UnstableDefault::class)
    fun build(): Outcome<LedgerSerializer.Text, LedgerSerializer.Failure> =
        when {
            module != null && encoder == null -> {
                encoder = Json(
                    configuration = JsonConfiguration(
                        prettyPrint = prettyPrint
                    ), context = module as SerialModule
                )
                Outcome.Ok(LedgerSerializer.Text(encoder!!))
            }
            module == null && encoder == null -> Outcome.Error(
                LedgerSerializer.Failure.NoEncoderSupplied(module, encoder)
            )
            encoder != null -> Outcome.Ok(
                LedgerSerializer.Text(encoder!!)
            )
            else -> deadCode()
        }
}


class LedgerBinarySerializerBuilder {
    var encoder: BinaryFormat? = null
    var module: SerialModule? = null

    fun build(): Outcome<LedgerSerializer.Binary, LedgerSerializer.Failure> =
        when {
            module != null && encoder == null -> {
                encoder = Cbor(
                    encodeDefaults = false,
                    context = module as SerialModule
                )
                Outcome.Ok(LedgerSerializer.Binary(encoder!!))
            }
            module == null && encoder == null -> Outcome.Error(
                LedgerSerializer.Failure.NoEncoderSupplied(module, encoder)
            )
            encoder != null -> Outcome.Ok(
                LedgerSerializer.Binary(encoder!!)
            )
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