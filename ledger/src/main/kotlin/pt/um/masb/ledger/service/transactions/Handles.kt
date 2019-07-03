package pt.um.masb.ledger.service.transactions

import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageID
import pt.um.masb.common.database.query.UnspecificQuery
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.storage.results.QueryFailure
import pt.um.masb.ledger.service.adapters.ChainHandleStorageAdapter
import pt.um.masb.ledger.service.handles.ChainHandle
import pt.um.masb.ledger.service.results.LedgerFailure
import pt.um.masb.ledger.storage.adapters.QueryLoadable

//-------------------------
// LedgerHandle Transactions
//-------------------------
internal fun PersistenceWrapper.getChainHandle(
    id: String
): Outcome<ChainHandle, LedgerFailure> =
    ChainHandleStorageAdapter.let {
        queryUniqueResult(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id} 
                    WHERE id.tag = :id
                """.trimIndent(),
                mapOf("id" to id)
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
        UnspecificQuery(
            """
                SELECT id.tag as tag 
                FROM ${ChainHandleStorageAdapter.id}
            """.trimIndent()
        ),
        object : QueryLoadable<String> {
            override fun load(
                element: StorageElement
            ): Outcome<String, QueryFailure> =
                Outcome.Ok(
                    element.getStorageProperty("tag")
                )
        }
    )

internal fun PersistenceWrapper.getKnownChainHandleIDs(
): Outcome<Sequence<StorageID>, QueryFailure> =
    queryResults(
        UnspecificQuery(
            """
                SELECT 
                FROM ${ChainHandleStorageAdapter.id}
            """.trimIndent()
        ),
        object : QueryLoadable<StorageID> {
            override fun load(
                element: StorageElement
            ): Outcome<StorageID, QueryFailure> =
                Outcome.Ok(element.identity)
        }
    )


internal fun PersistenceWrapper.getKnownChainHandles(
): Outcome<Sequence<ChainHandle>, LedgerFailure> =
    ChainHandleStorageAdapter.let {
        queryResults(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id}
                """.trimIndent()
            ),
            it
        )
    }
