package org.knowledger.ledger.storage.witness

import org.knowledger.collections.toMutableSortedListFromPreSorted
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.allValues
import org.knowledger.ledger.results.mapSuccess
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import org.knowledger.ledger.storage.adapters.TransactionOutputStorageAdapter

internal class SUWitnessStorageAdapter(
    private val container: LedgerInfo,
    private val transactionOutputStorageAdapter: TransactionOutputStorageAdapter
) : LedgerStorageAdapter<HashedWitnessImpl> {
    override val id: String
        get() = "TransactionOutput"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "publicKey" to StorageType.BYTES,
            "previousWitnessIndex" to StorageType.INTEGER,
            "previousCoinbase" to StorageType.HASH,
            "hash" to StorageType.HASH,
            "payout" to StorageType.PAYOUT,
            "transactionOutputs" to StorageType.LIST
        )


    override fun store(
        toStore: HashedWitnessImpl, session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setStorageProperty(
                "publicKey", toStore.publicKey.bytes
            ).setStorageProperty(
                "previousWitnessIndex", toStore.previousWitnessIndex
            ).setHashProperty(
                "previousCoinbase", toStore.previousCoinbase
            ).setHashProperty("hash", toStore.hash)
            .setPayoutProperty("payout", toStore.payout)
            .setElementList(
                "transactionOutputs",
                toStore.transactionOutputs.map {
                    transactionOutputStorageAdapter.persist(it, session)
                }
            )

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<HashedWitnessImpl, LoadFailure> =
        tryOrLoadUnknownFailure {
            val publicKey = EncodedPublicKey(
                element.getStorageProperty("publicKey")
            )
            val previousWitnessIndex: Int =
                element.getStorageProperty("previousWitnessIndex")
            val previousCoinbase =
                element.getHashProperty("previousCoinbase")
            val hash =
                element.getHashProperty("hash")
            val payout =
                element.getPayoutProperty("payout")
            element.getElementList("transactionOutputs").map {
                transactionOutputStorageAdapter.load(
                    ledgerHash, it
                )
            }.allValues().mapSuccess {
                HashedWitnessImpl(
                    publicKey = publicKey,
                    previousWitnessIndex = previousWitnessIndex,
                    previousCoinbase = previousCoinbase,
                    payout = payout,
                    transactionOutputs = it.toMutableSortedListFromPreSorted(),
                    hash = hash, hasher = container.hasher,
                    encoder = container.encoder
                )

            }

        }
}