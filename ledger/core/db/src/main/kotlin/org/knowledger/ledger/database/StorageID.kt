package org.knowledger.ledger.database

interface StorageID {
    val element: StorageElement
    val bytes: StorageBytes
    val key: String
}