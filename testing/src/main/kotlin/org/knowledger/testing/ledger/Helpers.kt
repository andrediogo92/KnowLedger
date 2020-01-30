package org.knowledger.testing.ledger

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.UpdateMode
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerializersModule
import org.knowledger.collections.mapToArray
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.hash.Hashing
import org.knowledger.ledger.data.LedgerData
import org.knowledger.ledger.data.TemperatureData
import org.knowledger.ledger.data.TrafficFlowData
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageResult
import org.knowledger.ledger.database.StorageResults
import org.knowledger.ledger.database.query.GenericQuery
import org.knowledger.ledger.results.Failure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.peekFailure
import org.knowledger.ledger.results.unwrap
import org.tinylog.kotlin.Logger
import java.util.concurrent.atomic.AtomicInteger

val testCounter: AtomicInteger = AtomicInteger(0)

val testHasher: Hashers = Hashers.DEFAULT_HASHER


val testSerialModule: SerialModule by lazy {
    SerializersModule {
        polymorphic(LedgerData::class) {
            TemperatureData::class with TemperatureData.serializer()
            TrafficFlowData::class with TrafficFlowData.serializer()
        }
    }
}

val testEncoder: BinaryFormat by lazy {
    Cbor(
        UpdateMode.OVERWRITE, true,
        testSerialModule
    )
}


@UnstableDefault
val testJson: Json = Json(
    configuration = JsonConfiguration.Default.copy(prettyPrint = true),
    context = testSerialModule
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


fun logActualToExpectedHashing(
    explanationActual: String,
    actualList: List<Hashing>,
    explanationExpected: String,
    expectedList: List<Hashing>
) {
    logActualToExpected(
        explanationActual,
        actualList.mapToArray { it.hash.truncatedHexString() },
        explanationExpected,
        expectedList.mapToArray { it.hash.truncatedHexString() }
    )
}

fun logActualToExpectedHashing(
    explanationActual: String,
    actualList: Array<Hashing>,
    explanationExpected: String,
    expectedList: Array<Hashing>
) {
    logActualToExpected(
        explanationActual,
        actualList.mapToArray { it.hash.truncatedHexString() },
        explanationExpected,
        expectedList.mapToArray { it.hash.truncatedHexString() }
    )
}


fun logActualToExpectedHashing(
    explanationActual: String,
    actualList: Iterable<Hashing>,
    explanationExpected: String,
    expectedList: Iterable<Hashing>
) {
    logActualToExpected(
        explanationActual,
        actualList.map { it.hash.truncatedHexString() },
        explanationExpected,
        expectedList.map { it.hash.truncatedHexString() }
    )
}

fun logActualToExpectedHashes(
    explanationActual: String,
    actualList: Iterable<Hash>,
    explanationExpected: String,
    expectedList: Iterable<Hash>
) {
    logActualToExpected(
        explanationActual,
        actualList.map { it.truncatedHexString() },
        explanationExpected,
        expectedList.map { it.truncatedHexString() }
    )
}

fun logActualToExpectedHashes(
    explanationActual: String,
    actualList: Array<Hash>,
    explanationExpected: String,
    expectedList: Array<Hash>
) {
    logActualToExpected(
        explanationActual,
        actualList.mapToArray { it.truncatedHexString() },
        explanationExpected,
        expectedList.mapToArray { it.truncatedHexString() }
    )
}

fun logActualToExpectedHashes(
    explanationActual: String,
    actualList: List<Hash>,
    explanationExpected: String,
    expectedList: List<Hash>
) {
    logActualToExpected(
        explanationActual,
        actualList.mapToArray { it.truncatedHexString() },
        explanationExpected,
        expectedList.mapToArray { it.truncatedHexString() }
    )
}

fun logActualToExpected(
    explanationActual: String,
    actualList: Iterable<String>,
    explanationExpected: String,
    expectedList: Iterable<String>
) {
    Logger.info {
        """
            |
            |$explanationActual
            |${actualList.joinToString(
            """,
                |
            """.trimMargin()
        ) { it }}
            |
            |$explanationExpected
            |${expectedList.joinToString(
            """,
                |
            """.trimMargin()
        ) { it }}
        """.trimMargin()
    }
}

fun logActualToExpected(
    explanationActual: String,
    actualList: Array<String>,
    explanationExpected: String,
    expectedList: Array<String>
) {
    Logger.info {
        """
            |
            |$explanationActual
            |${actualList.joinToString(
            """,
                |
            """.trimMargin()
        ) { it }}
            |
            |$explanationExpected
            |${expectedList.joinToString(
            """,
                |
            """.trimMargin()
        ) { it }}
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