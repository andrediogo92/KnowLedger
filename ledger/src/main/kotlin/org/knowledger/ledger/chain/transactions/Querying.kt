package org.knowledger.ledger.chain.transactions

import com.github.michaelbull.result.combine
import org.knowledger.ledger.adapters.QueryLoadable
import org.knowledger.ledger.adapters.StorageLoadable
import org.knowledger.ledger.adapters.service.ServiceLoadable
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.ServiceClass
import org.knowledger.ledger.core.tryOrDataUnknownFailure
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.adapters.Loadable
import org.knowledger.ledger.database.query.GenericQuery
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.database.results.QueryFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.err
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.LedgerData
import org.knowledger.ledger.storage.results.LedgerFailure
import org.knowledger.ledger.storage.results.LoadFailure
import org.knowledger.ledger.storage.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.storage.results.use

internal interface Querying {
    val ledgerHash: Hash
    val session: ManagedSession
    val context: PersistenceContext

    /**
     * Requires:
     * - A [query] with the command to execute
     * and it's arguments.
     * - A [Loadable] that converts from documents to
     * a usable user-typeId that implements [LedgerData].
     *
     * Returns an [Outcome] with a possible [DataFailure].
     */
    fun <T : LedgerData> queryUniqueResult(
        query: GenericQuery, loader: Loadable<T>,
    ): Outcome<T, DataFailure> = tryOrDataUnknownFailure {
        session.query(query).use {
            if (hasNext()) {
                loader.load(next().element)
            } else {
                DataFailure.NonExistentData("Empty ResultSet for ${query.query}").err()
            }
        }
    }

    /**
     * Requires:
     * - A [query] with the command to execute
     * and it's arguments.
     * - A [StorageLoadable] to load ledger domain elements from
     * the first applicable database element. [StorageLoadable]s
     * apply *exclusively* to [LedgerContract] classes.
     *
     * Returns an [Outcome] with a possible [LoadFailure].
     */
    fun <T : LedgerContract> queryUniqueResult(
        query: GenericQuery, loader: StorageLoadable<T>,
    ): Outcome<T, LoadFailure> = tryOrLoadUnknownFailure {
        session.query(query).use {
            if (hasNext()) {
                loader.load(ledgerHash, next().element, context)
            } else {
                LoadFailure.NonExistentData("Empty ResultSet for ${query.query}").err()
            }
        }
    }

    /**
     * Requires:
     * - A [query] with the command to execute
     * and it's arguments.
     * - A [ServiceLoadable] to load ledger service objects
     * from a database element.
     * [ServiceLoadable]s apply *exclusively*
     * to [ServiceClass] classes.
     *
     * Returns an [Outcome] with a possible [LedgerFailure].
     */
    fun <T : ServiceClass> queryUniqueResult(
        query: GenericQuery, loader: ServiceLoadable<T>,
    ): Outcome<T, LoadFailure> = tryOrLoadUnknownFailure {
        session.query(query).use {
            if (hasNext()) {
                loader.load(ledgerHash, next().element, context)
            } else {
                LoadFailure.NonExistentData("Empty ResultSet for ${query.query}").err()
            }
        }
    }


    /**
     * Requires:
     * - The [session] in which to execute the query.
     * - A [query] with the command to execute
     * and it's arguments.
     * - A [QueryLoadable] to load an arbitrary typeId
     * through application of a reduction to the
     * underlying database element.
     *
     *
     * Returns an [Outcome] with a possible [QueryFailure].
     */
    fun <T : Any> queryUniqueResult(
        query: GenericQuery, loader: QueryLoadable<T>,
    ): Outcome<T, LoadFailure> =
        tryOrLoadUnknownFailure {
            session.query(query).use {
                if (hasNext()) {
                    loader.load(ledgerHash, next().element, context)
                } else {
                    LoadFailure.NonExistentData("Empty ResultSet for ${query.query}").err()
                }
            }
        }


    /**
     * Requires:
     * - A [query] with the command to execute
     * and it's arguments.
     * - A [Loadable] that converts from documents to
     * a usable user-typeId that implements [LedgerData].
     *
     *
     * Returns an [Outcome] with a possible [DataFailure]
     * over a [Sequence].
     */
    fun <T : LedgerData> queryResults(
        query: GenericQuery, loader: Loadable<T>,
    ): Outcome<List<T>, DataFailure> = tryOrDataUnknownFailure {
        session.query(query).use {
            asSequence().map { loader.load(it.element) }.asIterable().combine()
        }
    }

    /**
     * Requires:
     * - A [query] with the command to execute
     * and it's arguments.
     * - A [StorageLoadable] to load ledger domain elements from
     * the first applicable database element. [StorageLoadable]s
     * apply *exclusively* to [LedgerContract] classes.
     *
     *
     * Returns an [Outcome] with a possible [LoadFailure]
     * over a [Sequence].
     */
    fun <T : LedgerContract> queryResults(
        query: GenericQuery, loader: StorageLoadable<T>,
    ): Outcome<List<T>, LoadFailure> = tryOrLoadUnknownFailure {
        session.query(query).use {
            asSequence().map { loader.load(ledgerHash, it.element, context) }.asIterable().combine()
        }
    }

    /**
     * Requires:
     * - A [query] with the command to execute
     * and it's arguments.
     * - A [ServiceLoadable] to load ledger service objects
     * from a database element.
     * [ServiceLoadable]s apply *exclusively*
     * to [ServiceClass] classes.
     *
     *
     * Returns an [Outcome] with a possible [LedgerFailure]
     * over a [Sequence].
     */
    fun <T : ServiceClass> queryResults(
        query: GenericQuery, loader: ServiceLoadable<T>,
    ): Outcome<List<T>, LoadFailure> = tryOrLoadUnknownFailure {
        session.query(query).use {
            asSequence().map { loader.load(ledgerHash, it.element, context) }.asIterable().combine()
        }
    }


    /**
     * Not to be used directly.
     * Requires knowledge of inner workings of DB.
     *
     * Requires:
     * - A [query] with the command to execute
     * and it's arguments.
     * - A [QueryLoadable] to transform the element into a
     * usable typeId.
     *
     * Returns an [Outcome] with a possible [QueryFailure]
     * over a [Sequence].
     */
    fun <T : Any> queryResults(
        query: GenericQuery, loader: QueryLoadable<T>,
    ): Outcome<List<T>, LoadFailure> = tryOrLoadUnknownFailure {
        session.query(query).use {
            asSequence().map { loader.load(ledgerHash, it.element, context) }.asIterable().combine()
        }
    }
}