package org.knowledger.ledger.serial

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import org.knowledger.collections.mapToArray
import org.knowledger.ledger.core.base.data.DefaultDiff
import org.knowledger.ledger.crypto.serial.DefaultDataFormulaSerializer
import org.knowledger.ledger.data.DataFormula
import org.knowledger.ledger.data.DummyData
import org.knowledger.ledger.data.LedgerData
import kotlin.reflect.KClass

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

internal fun SerialModule.withLedger(
    types: Array<out DataSerializerPair<*>>
): SerialModule =
    overwriteWith(SerializersModule {
        polymorphic(LedgerData::class) {
            types.forEach {
                //this is a valid cast because serializer pair classes force
                //construction of properties with correct LedgerData upper
                //type bound.
                (it.clazz as KClass<Any>) with (it.serializer as KSerializer<Any>)
            }
            DummyData::class with DummyDataSerializer
        }
    })

internal fun SerialModule.withDataFormulas(
    types: Array<out FormulaSerializerPair<*>>
): SerialModule =
    overwriteWith(SerializersModule {
        polymorphic(DataFormula::class) {
            types.forEach {
                //this is a valid cast because serializer pair classes force
                //construction of properties with correct LedgerData upper
                //type bound.
                @Suppress("UNCHECKED_CAST")
                (it.clazz as KClass<Any>) with (it.serializer as KSerializer<Any>)
            }
            DefaultDiff::class with DefaultDataFormulaSerializer
        }
    })

internal infix fun <T : LedgerData> KClass<T>.with(serializer: KSerializer<T>): DataSerializerPair<T> =
    DataSerializerPair(this, serializer)

internal infix fun <T : LedgerData> Class<T>.with(serializer: KSerializer<T>): DataSerializerPair<T> =
    DataSerializerPair(this.kotlin, serializer)

@Suppress("UNCHECKED_CAST")
internal infix fun <T : LedgerData> T.with(serializer: KSerializer<T>): DataSerializerPair<T> =
//this is a valid cast because this is upper bounded by LedgerData
    //and will be subsequently cast more generically.
    DataSerializerPair(this::class as KClass<T>, serializer)

internal infix fun <T : DataFormula> KClass<T>.with(serializer: KSerializer<T>): FormulaSerializerPair<T> =
    FormulaSerializerPair(this, serializer)

internal infix fun <T : DataFormula> Class<T>.with(serializer: KSerializer<T>): FormulaSerializerPair<T> =
    FormulaSerializerPair(this.kotlin, serializer)

@Suppress("UNCHECKED_CAST")
internal infix fun <T : DataFormula> T.with(serializer: KSerializer<T>): FormulaSerializerPair<T> =
//this is a valid cast because this is upper bounded by LedgerData
    //and will be subsequently cast more generically.
    FormulaSerializerPair(this::class as KClass<T>, serializer)