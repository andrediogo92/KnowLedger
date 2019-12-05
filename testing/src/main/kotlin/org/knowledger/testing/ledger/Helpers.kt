package org.knowledger.testing.ledger

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.UpdateMode
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerialModule
import org.knowledger.ledger.core.results.Failure
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.peekFailure
import org.knowledger.ledger.core.results.unwrap
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.TemperatureData
import org.knowledger.ledger.data.TrafficFlowData
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageResult
import org.knowledger.ledger.database.StorageResults
import org.knowledger.ledger.database.query.GenericQuery
import org.knowledger.ledger.serial.baseModule
import org.knowledger.ledger.serial.withDataFormulas
import org.knowledger.ledger.serial.withLedger
import org.tinylog.kotlin.Logger

val testHasher: Hashers = Hashers.DEFAULT_HASHER


val serialModule: SerialModule by lazy {
    baseModule.withLedger {
        TemperatureData::class with TemperatureData.serializer()
        TrafficFlowData::class with TrafficFlowData.serializer()
    }.withDataFormulas {}
}

val encoder: BinaryFormat by lazy {
    Cbor(
        UpdateMode.OVERWRITE, true,
        serialModule
    )
}


@UnstableDefault
val json: Json = Json(
    configuration = JsonConfiguration.Default.copy(prettyPrint = true),
    context = serialModule
)


fun StorageResults.toList(): List<StorageElement> =
    asSequence().map(StorageResult::element).toList()


fun ManagedSession.queryToList(
    query: String
): List<StorageElement> =
    query(query).toList()

fun ManagedSession.queryToList(
    query: GenericQuery
): List<StorageElement> =
    query(query).toList()


fun logActualToExpectedLists(
    explanationActual: String,
    actualList: List<Any>,
    explanationExpected: String,
    expectedList: List<Any>
) {
    Logger.info {
        """
            |
            |$explanationActual
            |${actualList.joinToString(
            """,
                |
            """.trimMargin()
        ) { it.toString() }}
            |
            |$explanationExpected
            |${expectedList.joinToString(
            """,
                |
            """.trimMargin()
        ) { it.toString() }}
        """.trimMargin()
    }
}

fun StringBuilder.appendByLine(toPrint: Collection<String>): StringBuilder =
    apply {
        toPrint.forEach { thing ->
            append(System.lineSeparator())
            append('\t')
            append(thing)
            append(',')
        }
    }

fun <T> Outcome<T, Failure>.failOnError() {
    peekFailure {
        it.unwrap()
    }
}