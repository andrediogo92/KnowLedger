package org.knowledger.ledger.storage.coinbase

import org.knowledger.collections.toMutableSortedListFromPreSorted
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.allValues
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.results.zip
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableWitness
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import org.knowledger.ledger.storage.coinbase.factory.CoinbaseFactory

internal class SUCoinbaseStorageAdapter(
    private val coinbaseFactory: CoinbaseFactory,
    private val merkleTreeStorageAdapter: LedgerStorageAdapter<MutableMerkleTree>,
    private val coinbaseStorageAdapter: LedgerStorageAdapter<MutableCoinbaseHeader>,
    private val witnessStorageAdapter: LedgerStorageAdapter<MutableWitness>
) : LedgerStorageAdapter<MutableCoinbase> {
    override val id: String
        get() = "Coinbase"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "merkleTree" to StorageType.LINK,
            "header" to StorageType.LINK,
            "witnesses" to StorageType.LIST
        )

    override fun store(
        toStore: MutableCoinbase, session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setLinked(
                "merkleTree",
                merkleTreeStorageAdapter.persist(
                    toStore.merkleTree, session
                )
            ).setLinked(
                "header",
                coinbaseStorageAdapter.persist(
                    toStore.header, session
                )
            ).setElementList(
                "witnesses",
                toStore.mutableWitnesses.map {
                    witnessStorageAdapter.persist(
                        it, session
                    )
                }
            )

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<MutableCoinbase, LoadFailure> =
        tryOrLoadUnknownFailure {
            zip(
                merkleTreeStorageAdapter.load(
                    ledgerHash, element.getLinked("merkleTree")
                ), coinbaseStorageAdapter.load(
                    ledgerHash, element.getLinked("header")
                ), element
                    .getElementList("witnesses")
                    .map { element ->
                        witnessStorageAdapter.load(
                            ledgerHash, element
                        )
                    }.allValues()
            ) { merkleTree, coinbaseHeader, witnesses ->
                coinbaseFactory.create(
                    merkleTree = merkleTree, header = coinbaseHeader,
                    witnesses = witnesses.toMutableSortedListFromPreSorted()
                )
            }
        }
}