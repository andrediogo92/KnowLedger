package org.knowledger.collections.microbench

import org.knowledger.testing.ledger.SmallData
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State

@State(Scope.Benchmark)
open class SortedCollectionState {
    val smallSize = 20
    val mediumSize = 2000
    val largeSize = 200000
    val veryLargeSize = 2000000
    val totalSize = veryLargeSize * 2


    var base: Array<SmallData> = Array(totalSize) { SmallData() }
}