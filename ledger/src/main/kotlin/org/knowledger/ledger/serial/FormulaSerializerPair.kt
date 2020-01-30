package org.knowledger.ledger.serial

import kotlinx.serialization.KSerializer
import org.knowledger.ledger.data.DataFormula
import kotlin.reflect.KClass

internal data class FormulaSerializerPair<T : DataFormula> constructor(
    val clazz: KClass<T>,
    val serializer: KSerializer<T>
) {
    @Suppress("UNCHECKED_CAST")
    constructor(
        instance: T,
        serializer: KSerializer<T>
    ) : this(instance::class as KClass<T>, serializer)
}