package org.knowledger.ledger.database

import java.util.*
import java.util.function.Consumer

interface StorageResults : Spliterator<StorageResult>,
                           Iterator<StorageResult>,
                           AutoCloseable {
    override fun forEachRemaining(action: Consumer<in StorageResult>) {
        while (hasNext()) {
            action.accept(next())
        }
    }
}