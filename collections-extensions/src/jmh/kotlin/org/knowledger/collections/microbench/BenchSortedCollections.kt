package org.knowledger.collections.microbench

import org.knowledger.collections.toSortedList
import org.knowledger.testing.core.random
import org.knowledger.testing.ledger.SmallData
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Warmup
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
open class BenchSortedCollections {
    private fun extractArray(state: SortedCollectionState, extractSize: Int): Array<SmallData> {
        val minBound = random.randomInt(state.totalSize - extractSize)
        val maxBound = minBound + extractSize
        return state.base.sliceArray(minBound until maxBound)
    }

    @Warmup(
        iterations = 10, time = 50,
        timeUnit = TimeUnit.MILLISECONDS, batchSize = 1
    )
    @Measurement(
        iterations = 25, time = 50,
        timeUnit = TimeUnit.MILLISECONDS, batchSize = 1
    )
    @Benchmark
    fun benchSmallList(state: SortedCollectionState) {
        extractArray(state, state.smallSize).toSortedList()
    }


    @Warmup(
        iterations = 10, time = 150,
        timeUnit = TimeUnit.MILLISECONDS, batchSize = 1
    )
    @Measurement(
        iterations = 25, time = 150,
        timeUnit = TimeUnit.MILLISECONDS, batchSize = 1
    )
    @Benchmark
    fun benchMediumList(state: SortedCollectionState) {
        extractArray(state, state.mediumSize).toSortedList()
    }

    @Warmup(
        iterations = 10, time = 1,
        timeUnit = TimeUnit.SECONDS, batchSize = 1
    )
    @Measurement(
        iterations = 25, time = 1,
        timeUnit = TimeUnit.SECONDS, batchSize = 1
    )
    @Benchmark
    fun benchLargeList(state: SortedCollectionState) {
        extractArray(state, state.largeSize).toSortedList()
    }

    @Warmup(
        iterations = 3, time = 10,
        timeUnit = TimeUnit.SECONDS, batchSize = 1
    )
    @Measurement(
        iterations = 8, time = 10,
        timeUnit = TimeUnit.SECONDS, batchSize = 1
    )
    @Benchmark
    fun benchVeryLargeList(state: SortedCollectionState) {
        extractArray(state, state.veryLargeSize).toSortedList()
    }


    @Warmup(
        iterations = 10, time = 50,
        timeUnit = TimeUnit.MILLISECONDS, batchSize = 1
    )
    @Measurement(
        iterations = 25, time = 50,
        timeUnit = TimeUnit.MILLISECONDS, batchSize = 1
    )
    @Benchmark
    fun benchSmallSet(state: SortedCollectionState) {
        extractArray(state, state.smallSize).toSortedSet()
    }


    @Warmup(
        iterations = 10, time = 150,
        timeUnit = TimeUnit.MILLISECONDS, batchSize = 1
    )
    @Measurement(
        iterations = 25, time = 150,
        timeUnit = TimeUnit.MILLISECONDS, batchSize = 1
    )
    @Benchmark
    fun benchMediumSet(state: SortedCollectionState) {
        extractArray(state, state.mediumSize).toSortedSet()
    }

    @Warmup(
        iterations = 10, time = 1,
        timeUnit = TimeUnit.SECONDS, batchSize = 1
    )
    @Measurement(
        iterations = 25, time = 1,
        timeUnit = TimeUnit.SECONDS, batchSize = 1
    )
    @Benchmark
    fun benchLargeSet(state: SortedCollectionState) {
        extractArray(state, state.largeSize).toSortedSet()
    }

    @Warmup(
        iterations = 3, time = 10,
        timeUnit = TimeUnit.SECONDS, batchSize = 1
    )
    @Measurement(
        iterations = 8, time = 10,
        timeUnit = TimeUnit.SECONDS, batchSize = 1
    )
    @Benchmark
    fun benchVeryLargeSet(state: SortedCollectionState) {
        extractArray(state, state.veryLargeSize).toSortedSet()
    }

}