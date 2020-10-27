package org.knowledger.ledger.microbench

import org.knowledger.ledger.storage.Transaction
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Warmup
import java.util.concurrent.TimeUnit

@Warmup(
    iterations = 25, time = 400,
    timeUnit = TimeUnit.MILLISECONDS, batchSize = 2
)
@Measurement(
    iterations = 150, time = 400,
    timeUnit = TimeUnit.MILLISECONDS, batchSize = 1
)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
open class BenchLargeSignatures {
    private fun runWith(state: SignatureState, transactions: Sequence<Transaction>) {
        state.resultSize += consumeSignatures(transactions)
            .map { it.signature.bytes.size }
            .average()
    }

    private fun consumeSignatures(
        sequence: Sequence<Transaction>,
    ): Iterable<Transaction> = sequence.take(20).asIterable()

    @Benchmark
    fun smallSignatures(state: SignatureState) {
        runWith(state, state.smallSequence)
    }

    @Benchmark
    fun mediumSignatures(state: SignatureState) {
        runWith(state, state.mediumSequence)
    }

    @Benchmark
    fun largeSignatures(state: SignatureState) {
        runWith(state, state.largeSequence)
    }
}

