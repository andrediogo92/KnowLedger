package pt.um.masb.ledger.service.transactions

import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageID
import pt.um.masb.common.database.query.Filters
import pt.um.masb.common.database.query.GenericSelect
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.storage.results.QueryFailure
import pt.um.masb.ledger.service.adapters.ChainHandleStorageAdapter
import pt.um.masb.ledger.service.handles.ChainHandle
import pt.um.masb.ledger.service.results.LedgerFailure
import pt.um.masb.ledger.storage.adapters.QueryLoadable

//-------------------------
// LedgerHandle Transactions
//-------------------------
internal fun <T> PersistenceWrapper.getChainHandle(
    ledgerHash: Hash,
    clazz: Class<in T>
): Outcome<ChainHandle, LedgerFailure> =
    ChainHandleStorageAdapter.let {
        queryUniqueResult(
            ledgerHash,
            GenericSelect(
                it.id
            ).withSimpleFilter(
                Filters.WHERE,
                "clazz",
                "clazz",
                clazz.name
            ),
            it
        )
    }

internal fun PersistenceWrapper.tryAddChainHandle(
    chainHandle: ChainHandle
): Outcome<StorageID, QueryFailure> =
    persistEntity(
        chainHandle,
        ChainHandleStorageAdapter
    )

internal fun PersistenceWrapper.getKnownChainHandleTypes(
): Outcome<Sequence<String>, QueryFailure> =
    queryResults(
        GenericSelect(
            ChainHandleStorageAdapter.id
        ).withProjection(
            "clazz"
        ),
        object : QueryLoadable<String> {
            override fun load(
                element: StorageElement
            ): Outcome<String, QueryFailure> =
                Outcome.Ok(
                    element.getStorageProperty("clazz")
                )
        }
    )

internal fun PersistenceWrapper.getKnownChainHandleIDs(
): Outcome<Sequence<StorageID>, QueryFailure> =
    queryResults(
        GenericSelect(
            ChainHandleStorageAdapter.id
        ),
        object : QueryLoadable<StorageID> {
            override fun load(
                element: StorageElement
            ): Outcome<StorageID, QueryFailure> =
                Outcome.Ok(element.identity)
        }
    )


internal fun PersistenceWrapper.getKnownChainHandles(
    ledgerHash: Hash
): Outcome<Sequence<ChainHandle>, LedgerFailure> =
    ChainHandleStorageAdapter.let {
        queryResults(
            ledgerHash,
            GenericSelect(
                it.id
            ),
            it
        )
    }
