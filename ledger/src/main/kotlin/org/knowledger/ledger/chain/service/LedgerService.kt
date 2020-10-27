package org.knowledger.ledger.chain.service

import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.crypto.Hashers
import org.knowledger.ledger.storage.DataFormula
import kotlin.reflect.KClass

internal interface LedgerService {
    fun <T : AbstractStorageAdapter<*>> registerAdapter(adapterClazz: KClass<T>)

    fun <T : DataFormula> registerFormula(clazz: KClass<T>)
    fun calculateAdapters(hashers: Hashers): Iterable<AbstractStorageAdapter<*>>
    fun calculateFormulas(hashers: Hashers): Iterable<DataFormula>
}