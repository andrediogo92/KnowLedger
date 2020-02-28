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
    private fun consumeSignatures(
        sequence: Sequence<Transaction>
    ): Iterable<Transaction> =
        sequence.take(20).asIterable()

    @Benchmark
    fun smallSignatures(state: LargeSignatureState) {
        state.resultSize += consumeSignatures(state.smallSequence)
            .map { it.signature.bytes.size }
            .average()
    }

    @Benchmark
    fun mediumSignatures(state: LargeSignatureState) {
        state.resultSize += consumeSignatures(state.mediumSequence)
            .map { it.signature.bytes.size }
            .average()
    }

    @Benchmark
    fun largeSignatures(state: LargeSignatureState) {
        state.resultSize += consumeSignatures(state.largeSequence)
            .map { it.signature.bytes.size }
            .average()
    }
}

