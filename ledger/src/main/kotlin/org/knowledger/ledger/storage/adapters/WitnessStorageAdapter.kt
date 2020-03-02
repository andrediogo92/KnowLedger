package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.Witness
import org.knowledger.ledger.storage.witness.HashedWitnessImpl
import org.knowledger.ledger.storage.witness.SAWitnessStorageAdapter
import org.knowledger.ledger.storage.witness.SUWitnessStorageAdapter
import org.knowledger.ledger.storage.witness.StorageAwareWitness

internal class WitnessStorageAdapter(
    private val suWitnessStorageAdapter: SUWitnessStorageAdapter,
    private val saTransactionOutputStorageAdapter: SAWitnessStorageAdapter
) : LedgerStorageAdapter<Witness>,
    SchemaProvider by suWitnessStorageAdapter {
    override fun store(
        toStore: Witness,
        session: ManagedSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareWitness ->
                saTransactionOutputStorageAdapter.store(toStore, session)
            is HashedWitnessImpl ->
                suWitnessStorageAdapter.store(toStore, session)
            else -> deadCode()
        }

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<Witness, LoadFailure> =
        saTransactionOutputStorageAdapter.load(ledgerHash, element)
}