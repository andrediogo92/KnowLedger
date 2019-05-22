package pt.um.masb.common.database.orient

import com.orientechnologies.orient.core.sql.executor.OResultSet
import pt.um.masb.common.database.StorageResult
import pt.um.masb.common.database.StorageResults
import java.util.*
import java.util.function.Consumer

internal inline class DocumentResults constructor(
    internal val results: OResultSet
) : StorageResults {
    override fun hasNext(): Boolean =
        results.hasNext()

    override fun next(): StorageResult =
        DocumentResult(results.next())

    override fun estimateSize(): Long =
        results.estimateSize()

    override fun characteristics(): Int =
        results.characteristics()

    override fun tryAdvance(action: Consumer<in StorageResult>): Boolean =
        results.tryAdvance {
            action.accept(DocumentResult(it))
        }

    override fun trySplit(): Spliterator<StorageResult> =
        DocumentResults(results.trySplit())
}