package org.knowledger.ledger.chain.service

import org.knowledger.ledger.adapters.LedgerAdaptersProvider
import org.knowledger.ledger.chain.DefaultLedgerAdapters
import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.crypto.Hashers
import org.knowledger.ledger.data.adapters.DummyDataStorageAdapter
import org.knowledger.ledger.storage.DataFormula
import org.knowledger.ledger.storage.DefaultDiff
import org.knowledger.ledger.storage.Factories
import org.knowledger.ledger.storage.saFactories
import kotlin.reflect.KClass

object LedgerConfigurationService : LedgerService {
    private val registeredAdapters: MutableList<KClass<*>> =
        mutableListOf(DummyDataStorageAdapter::class)
    private val registeredFormulas: MutableSet<KClass<*>> =
        mutableSetOf(DefaultDiff::class)
    internal val ledgerAdapters: LedgerAdaptersProvider by lazy { DefaultLedgerAdapters() }
    internal val factories: Factories by lazy { saFactories }

    override fun <T : AbstractStorageAdapter<*>> registerAdapter(adapterClazz: KClass<T>) {
        registeredAdapters.add(adapterClazz)
    }

    override fun <T : DataFormula> registerFormula(clazz: KClass<T>) {
        registeredFormulas.add(clazz)
    }

    override fun calculateAdapters(hashers: Hashers): Iterable<AbstractStorageAdapter<*>> =
        registeredAdapters.map {
            it.constructors.first().call(hashers) as AbstractStorageAdapter<*>
        }

    override fun calculateFormulas(hashers: Hashers): Iterable<DataFormula> =
        registeredFormulas.map {
            it.constructors.first().call(hashers) as DataFormula
        }
}