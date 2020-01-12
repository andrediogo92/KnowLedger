package org.knowledger.ledger.service.transactions

import org.knowledger.base64.base64Encoded
import org.knowledger.ledger.data.Tag
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.database.query.UnspecificQuery
import org.knowledger.ledger.database.results.QueryFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.handles.ChainHandle
import org.knowledger.ledger.service.results.LedgerFailure
import org.knowledger.ledger.storage.adapters.QueryLoadable

//-------------------------
// LedgerHandle Transactions
//-------------------------
internal fun PersistenceWrapper.getChainHandle(
    id: Tag
): Outcome<ChainHandle, LedgerFailure> =
    getChainHandle(id.base64Encoded())

internal fun PersistenceWrapper.getChainHandle(
    id: String
): Outcome<ChainHandle, LedgerFailure> =
    chainHandleStorageAdapter.let {
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
        chainHandleStorageAdapter
    )

internal fun PersistenceWrapper.getKnownChainHandleTypes(
): Outcome<Sequence<String>, QueryFailure> =
    queryResults(
        UnspecificQuery(
            """
                SELECT id.tag as tag 
                FROM ${chainHandleStorageAdapter.id}
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
                FROM ${chainHandleStorageAdapter.id}
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
    chainHandleStorageAdapter.let {
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
