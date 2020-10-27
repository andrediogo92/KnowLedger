package org.knowledger.ledger.adapters

import org.knowledger.collections.SortedList
import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.core.adapters.Tag
import org.knowledger.ledger.core.toTag
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.LedgerData
import kotlin.reflect.KClass

interface DataAdapters {
    //Data Adapters
    val dataAdapters: SortedList<AbstractStorageAdapter<out LedgerData>>

    fun findAdapter(tag: Tag): AbstractStorageAdapter<out LedgerData>? =
        dataAdapters.find { it.tag == tag }

    fun findAdapter(clazz: KClass<*>): AbstractStorageAdapter<out LedgerData>? =
        dataAdapters.find { it.clazz == clazz }

    fun findAdapter(rawTag: Hash): AbstractStorageAdapter<out LedgerData>? =
        findAdapter(rawTag.toTag())

    fun hasAdapter(tag: Tag): Boolean =
        dataAdapters.any { it.tag == tag }

    fun hasAdapter(clazz: KClass<*>): Boolean =
        dataAdapters.any { it.clazz == clazz }

    fun hasAdapter(rawTag: Hash): Boolean =
        hasAdapter(rawTag.toTag())


    fun addAdapter(adapter: AbstractStorageAdapter<out LedgerData>): Boolean
    fun addAdapters(types: Iterable<AbstractStorageAdapter<*>>)
}