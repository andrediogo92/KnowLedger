package org.knowledger.ledger.database

interface NewInstanceSession {
    fun newInstance(): StorageElement

    fun newInstance(className: String): StorageElement

    fun newInstance(bytes: ByteArray): StorageBytes
}