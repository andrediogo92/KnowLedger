package org.knowledger.ledger.core.serial

interface BinaryEncoder {
    fun <T> dump(serializer: LedgerSerializer<T>): ByteArray
    fun <T> load(serializer: LedgerSerializer<T>, bytes: ByteArray): T
}