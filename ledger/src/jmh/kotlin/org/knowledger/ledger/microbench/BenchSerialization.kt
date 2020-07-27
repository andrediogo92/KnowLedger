package org.knowledger.ledger.microbench

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@Warmup(
    iterations = 25, time = 50,
    timeUnit = TimeUnit.MILLISECONDS, batchSize = 4
)
@Measurement(
    iterations = 250, time = 30,
    timeUnit = TimeUnit.MILLISECONDS, batchSize = 1
)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
open class BenchSerialization {
    @Benchmark
    fun `encode block`(
        state: SerializationState, blackhole: Blackhole
    ) {
        blackhole.consume(
            state.binarySerializer.encodeBlock(state.block)
        )
    }
}




