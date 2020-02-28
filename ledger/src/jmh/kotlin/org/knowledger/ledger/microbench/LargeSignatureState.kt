@file:UseSerializers(HashSerializer::class)

package org.knowledger.ledger.microbench

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.UpdateMode
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.SerializersModule
import org.knowledger.ledger.core.base.data.SelfInterval
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.data.LedgerData
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.test.transactionGenerator
import org.knowledger.testing.core.random
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import org.tinylog.kotlin.Logger
import java.math.BigDecimal

@State(Scope.Benchmark)
open class LargeSignatureState : BaseState() {
    var resultSize: Double = 0.0
    val encoder = Cbor(
        UpdateMode.OVERWRITE, true,
        SerializersModule {
            polymorphic(LedgerData::class) {
                RandomData::class with RandomData.serializer()
            }
        })
    val stringFactor = 32
    val smallSize = 20
    val mediumSize = 200
    val largeSize = 2000
    val smallSequence: Sequence<Transaction>
        get() = transactionGenerator(id = id, encoder = encoder) {
            RandomData(stringFactor, smallSize)
        }
    val mediumSequence: Sequence<Transaction>
        get() = transactionGenerator(id = id, encoder = encoder) {
            RandomData(stringFactor, mediumSize)
        }
    val largeSequence: Sequence<Transaction>
        get() = transactionGenerator(id = id, encoder = encoder) {
            RandomData(stringFactor, largeSize)
        }

    @Setup
    fun setup() {
        resultSize = 0.0
    }

    @TearDown
    fun tearDown() {
        Logger.debug {
            "Average Signature size :> ${resultSize / 150}"
        }
        resultSize = 0.0
    }

    @Serializable
    data class RandomData(
        val randomLongs: List<Long>,
        val randomStrings: List<String>,
        val randomHashes: List<Hash>
    ) : LedgerData {
        constructor(stringFactor: Int, size: Int) : this(
            randomLongs = random.randomLongs().take(size).toList(),
            randomStrings = random.randomStrings(stringFactor).take(size).toList(),
            randomHashes = random.random256Hashes().take(size).toList()
        )

        override fun clone(): LedgerData =
            copy()

        override fun calculateDiff(previous: SelfInterval): BigDecimal =
            BigDecimal.ONE

        override fun serialize(encoder: BinaryFormat): ByteArray =
            ByteArray(0)

    }
}