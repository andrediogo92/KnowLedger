package org.knowledger.ledger.core

/**
 * Report an approximate size in bytes
 * of the underlying object.

 * @property approximateSize The approximate size of the instance in bytes.
 */
interface Sizeable {
    val approximateSize: Long?
}
