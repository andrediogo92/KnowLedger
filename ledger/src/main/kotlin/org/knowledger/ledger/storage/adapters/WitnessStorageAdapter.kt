package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.MutableWitness
import org.knowledger.ledger.storage.witness.SUWitnessStorageAdapter
import org.knowledger.ledger.storage.witness.StorageAwareWitness
import org.knowledger.ledger.storage.witness.factory.StorageAwareWitnessFactory

internal class WitnessStorageAdapter(
    ledgerInfo: LedgerInfo,
    saWitnessFactory: StorageAwareWitnessFactory
) : LedgerStorageAdapter<MutableWitness> {
    private val suWitnessStorageAdapter = SUWitnessStorageAdapter(
        ledgerInfo, saWitnessFactory,
        saWitnessFactory.transactionOutputStorageAdapter
    )

    override val id: String
        get() = suWitnessStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = suWitnessStorageAdapter.properties


    override fun store(
        toStore: MutableWitness,
        session: ManagedSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareWitness -> session.cacheStore(
                suWitnessStorageAdapter, toStore,
                toStore.witness
            )
            else -> suWitnessStorageAdapter.store(toStore, session)
        }

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<StorageAwareWitness, LoadFailure> =
        element.cachedLoad(
            ledgerHash, suWitnessStorageAdapter
        )
}