package org.knowledger.ledger.chain

import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.SortedList
import org.knowledger.collections.mutableSortedListOf
import org.knowledger.ledger.adapters.AdaptersCollection
import org.knowledger.ledger.adapters.LedgerAdaptersProvider
import org.knowledger.ledger.chain.service.LedgerConfigurationService
import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.data.adapters.DummyDataStorageAdapter
import org.knowledger.ledger.storage.Factories
import org.knowledger.ledger.storage.LedgerData

internal class PersistenceContext(
    internal val ledgerInfo: LedgerInfo,
    private val adapters: MutableSortedList<AbstractStorageAdapter<out LedgerData>> =
        mutableSortedListOf(DummyDataStorageAdapter(ledgerInfo.hashers)),
    internal val factories: Factories = LedgerConfigurationService.factories,
    private val defaultLedgerAdapters: LedgerAdaptersProvider = LedgerConfigurationService.ledgerAdapters,
) : LedgerAdaptersProvider by defaultLedgerAdapters, Factories by factories, AdaptersCollection {
    override val dataAdapters: SortedList<AbstractStorageAdapter<out LedgerData>>
        get() = adapters

    override fun addAdapter(adapter: AbstractStorageAdapter<out LedgerData>): Boolean =
        adapters.add(adapter)

    override fun addAdapters(types: Iterable<AbstractStorageAdapter<*>>) {
        adapters.addAll(types)
    }
}