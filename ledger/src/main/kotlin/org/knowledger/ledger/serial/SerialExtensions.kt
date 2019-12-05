package org.knowledger.ledger.serial

import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import org.knowledger.collections.mapToArray
import org.knowledger.ledger.core.base.data.DefaultDiff
import org.knowledger.ledger.crypto.serial.DefaultDataFormulaSerializer
import org.knowledger.ledger.data.DataFormula
import org.knowledger.ledger.data.DummyData
import org.knowledger.ledger.data.LedgerData

fun <T> Array<T>.serial(): Array<String>
        where T : SerialEnum, T : Enum<T> =
    mapToArray { it.serialName }

fun <T> Array<T>.lowercase(): Array<String>
        where T : Enum<T> =
    mapToArray { it.name.toLowerCase() }

interface SerialEnum {
    val serialName: String
}

val baseModule: SerialModule = SerializersModule {}

inline fun SerialModule.withLedger(
    crossinline with: PolymorphicModuleBuilder<Any>.() -> Unit
): SerialModule =
    overwriteWith(SerializersModule {
        polymorphic(LedgerData::class) {
            with()
            DummyData::class with DummyDataSerializer
        }
    })

inline fun SerialModule.withDataFormulas(
    crossinline with: PolymorphicModuleBuilder<Any>.() -> Unit
): SerialModule =
    overwriteWith(SerializersModule {
        polymorphic(DataFormula::class) {
            with()
            DefaultDiff::class with DefaultDataFormulaSerializer
        }
    })