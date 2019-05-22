package pt.um.masb.common.database

import java.util.*
import java.util.function.Consumer

interface StorageResults : Spliterator<StorageResult>,
                           Iterator<StorageResult> {
    override fun forEachRemaining(action: Consumer<in StorageResult>) {
        while (hasNext()) {
            action.accept(next())
        }
    }
}