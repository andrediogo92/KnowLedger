package org.knowledger.ledger.core

/**
 * Report an approximate size in bytes
 * of the underlying object.
 */
interface Sizeable {
    /**
     * Calculates the approximate size of the transaction.
     *
     * @property approximateSize The size of the transaction in bytes.
     */
    val approximateSize: Long
}
