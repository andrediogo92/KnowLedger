package org.knowledger.ledger.adapters

import org.knowledger.base64.Base64String
import org.knowledger.base64.base64Encoded
import org.knowledger.collections.SortedList
import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.LedgerData

interface DataAdapters {
    //Data Adapters
    val dataAdapters: SortedList<AbstractStorageAdapter<out LedgerData>>

    fun findAdapter(tag: Base64String): AbstractStorageAdapter<out LedgerData>?
    fun findAdapter(clazz: Class<*>): AbstractStorageAdapter<out LedgerData>?
    fun findAdapter(tag: Hash): AbstractStorageAdapter<out LedgerData>? =
        findAdapter(tag.base64Encoded())

    fun addAdapter(adapter: AbstractStorageAdapter<out LedgerData>): Boolean
    fun addAdapters(types: Iterable<AbstractStorageAdapter<*>>)

    fun hasAdapter(tag: String): Boolean
    fun hasAdapter(tag: Hash): Boolean
    fun hasAdapter(clazz: Class<*>): Boolean
}