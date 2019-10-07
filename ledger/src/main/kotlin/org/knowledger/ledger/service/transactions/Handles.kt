package org.knowledger.ledger.service.transactions

import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.database.query.UnspecificQuery
import org.knowledger.ledger.core.misc.base64Encoded
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.storage.results.QueryFailure
import org.knowledger.ledger.data.Tag
import org.knowledger.ledger.service.adapters.ChainHandleStorageAdapter
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
