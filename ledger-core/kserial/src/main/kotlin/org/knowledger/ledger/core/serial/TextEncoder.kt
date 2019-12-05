package org.knowledger.ledger.core.serial

interface TextEncoder {
    fun <T> stringify(serializer: LedgerSerializer<T>): String
    fun <T> parse(serializer: LedgerSerializer<T>, string: String): T
}