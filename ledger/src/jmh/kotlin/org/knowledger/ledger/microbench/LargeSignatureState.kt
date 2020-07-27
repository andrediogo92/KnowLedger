@file:UseSerializers(HashSerializer::class)

package org.knowledger.ledger.microbench

import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.storage.transaction.ImmutableTransaction
import org.knowledger.testing.ledger.RandomData
import org.knowledger.testing.storage.immutableTransactionGenerator
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import org.tinylog.kotlin.Logger

@State(Scope.Benchmark)
open class LargeSignatureState : BaseState() {
    var resultSize: Double = 0.0
    private val stringFactor = 32
    private val smallSize = 20
    private val mediumSize = 200
    private val largeSize = 2000
    val smallSequence: Sequence<ImmutableTransaction> =
        immutableTransactionGenerator(id = id) {
            RandomData(stringFactor, smallSize)
        }
    val mediumSequence: Sequence<ImmutableTransaction> =
        immutableTransactionGenerator(id = id) {
            RandomData(stringFactor, mediumSize)
        }
    val largeSequence: Sequence<ImmutableTransaction> =
        immutableTransactionGenerator(id = id) {
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
}