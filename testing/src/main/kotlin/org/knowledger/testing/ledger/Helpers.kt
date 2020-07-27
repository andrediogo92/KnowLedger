package org.knowledger.testing.ledger

import com.github.michaelbull.result.onFailure
import org.knowledger.base64.base64Encoded
import org.knowledger.collections.mapToArray
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageResult
import org.knowledger.ledger.database.StorageResults
import org.knowledger.ledger.database.query.GenericQuery
import org.knowledger.ledger.results.Failure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.unwrap
import org.tinylog.kotlin.Logger
import java.util.concurrent.atomic.AtomicInteger


val testCounter: AtomicInteger = AtomicInteger(0)

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
    explanationActual: String, actualList: List<Hashing>,
    explanationExpected: String, expectedList: List<Hashing>
) {
    logActualToExpected(
        explanationActual, actualList.mapToArray {
            it.hash.base64Encoded()
        }, explanationExpected, expectedList.mapToArray {
            it.hash.base64Encoded()
        }
    )
}

fun logActualToExpectedHashing(
    explanationActual: String, actualList: Array<Hashing>,
    explanationExpected: String, expectedList: Array<Hashing>
) {
    logActualToExpected(
        explanationActual, actualList.mapToArray {
            it.hash.base64Encoded()
        }, explanationExpected, expectedList.mapToArray {
            it.hash.base64Encoded()
        }
    )
}


fun logActualToExpectedHashing(
    explanationActual: String, actualList: Iterable<Hashing>,
    explanationExpected: String, expectedList: Iterable<Hashing>
) {
    logActualToExpected(
        explanationActual, actualList.map {
            it.hash.base64Encoded()
        }, explanationExpected, expectedList.map {
            it.hash.base64Encoded()
        }
    )
}

fun logActualToExpectedHashes(
    explanationActual: String, actualList: Iterable<Hash>,
    explanationExpected: String, expectedList: Iterable<Hash>
) {
    logActualToExpected(
        explanationActual, actualList.map(Hash::base64Encoded),
        explanationExpected, expectedList.map(Hash::base64Encoded)
    )
}

fun logActualToExpectedHashes(
    explanationActual: String, actualList: Array<Hash>,
    explanationExpected: String, expectedList: Array<Hash>
) {
    logActualToExpected(
        explanationActual, actualList.mapToArray(Hash::base64Encoded),
        explanationExpected, expectedList.mapToArray(Hash::base64Encoded)
    )
}

fun logActualToExpectedHashes(
    explanationActual: String, actualList: List<Hash>,
    explanationExpected: String, expectedList: List<Hash>
) {
    logActualToExpected(
        explanationActual, actualList.mapToArray(Hash::base64Encoded),
        explanationExpected, expectedList.mapToArray(Hash::base64Encoded)
    )
}

fun logActualToExpected(
    explanationActual: String, actualList: Iterable<String>,
    explanationExpected: String, expectedList: Iterable<String>
) {
    Logger.info {
        """
            |
            |$explanationActual
            |${actualList.joinToString(System.lineSeparator()) { it }}
            |
            |$explanationExpected
            |${expectedList.joinToString(System.lineSeparator()) { it }}
        """.trimMargin()
    }
}

fun logActualToExpected(
    explanationActual: String, actualList: Array<String>,
    explanationExpected: String, expectedList: Array<String>
) {
    logActualToExpected(
        explanationActual, actualList.asIterable(),
        explanationExpected, expectedList.asIterable()
    )
}

fun StringBuilder.appendByLine(toPrint: Collection<String>): StringBuilder =
    apply {
        toPrint.forEach { thing ->
            appendln().append('\t').append(thing).append(',')
        }
    }

fun <T, U : Failure> Outcome<T, U>.failOnError(): Outcome<T, U> =
    onFailure { it.unwrap() }
