package org.knowledger.database.orient

import com.orientechnologies.orient.core.sql.executor.OResultSet
import org.knowledger.ledger.database.StorageResult
import org.knowledger.ledger.database.StorageResults
import java.util.*
import java.util.function.Consumer

internal inline class DocumentResults(
    internal val results: OResultSet
) : StorageResults {
    override fun close() {
        results.close()
    }

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