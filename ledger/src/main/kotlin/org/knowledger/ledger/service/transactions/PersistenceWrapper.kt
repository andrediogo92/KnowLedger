package org.knowledger.ledger.service.transactions

import org.knowledger.ledger.adapters.EagerStorable
import org.knowledger.ledger.config.adapters.BlockParamsStorageAdapter
import org.knowledger.ledger.config.adapters.ChainIdStorageAdapter
import org.knowledger.ledger.config.adapters.LedgerIdStorageAdapter
import org.knowledger.ledger.config.adapters.LedgerParamsStorageAdapter
import org.knowledger.ledger.core.database.ManagedSchemas
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.database.query.GenericQuery
import org.knowledger.ledger.core.database.query.UnspecificQuery
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.allValues
import org.knowledger.ledger.core.results.peekFailure
import org.knowledger.ledger.core.results.peekSuccess
import org.knowledger.ledger.core.storage.LedgerContract
import org.knowledger.ledger.core.storage.adapters.Loadable
import org.knowledger.ledger.core.storage.adapters.SchemaProvider
import org.knowledger.ledger.core.storage.results.DataFailure
import org.knowledger.ledger.core.storage.results.QueryFailure
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.data.LedgerData
import org.knowledger.ledger.data.adapters.DummyDataStorageAdapter
import org.knowledger.ledger.data.adapters.PhysicalDataStorageAdapter
import org.knowledger.ledger.results.tryOrDataUnknownFailure
import org.knowledger.ledger.results.tryOrLedgerUnknownFailure
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.results.tryOrQueryUnknownFailure
import org.knowledger.ledger.results.tryOrUpdateUnknownFailure
import org.knowledger.ledger.results.use
import org.knowledger.ledger.service.Identity
import org.knowledger.ledger.service.ServiceClass
import org.knowledger.ledger.service.adapters.ChainHandleStorageAdapter
import org.knowledger.ledger.service.adapters.IdentityStorageAdapter
import org.knowledger.ledger.service.adapters.LedgerConfigStorageAdapter
import org.knowledger.ledger.service.adapters.ServiceLoadable
import org.knowledger.ledger.service.adapters.TransactionPoolStorageAdapter
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.handles.builder.LedgerConfig
import org.knowledger.ledger.service.results.LedgerFailure
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.adapters.*


/**
 * A Thread-safe wrapper into a DB context
 * for a ledger.
 */
internal data class PersistenceWrapper(
    private val ledgerHash: Hash,
    private val session: ManagedSession
) : EntityStore, ServiceClass {
    private val schemas = session.managedSchemas
    internal val isClosed
        get() = session.isClosed

    internal fun registerDefaultSchemas(
    ) {
        val schemas: Set<SchemaProvider<out Any>> =
            setOf(
                //Configuration Adapters
                BlockParamsStorageAdapter,
                ChainIdStorageAdapter,
                CoinbaseStorageAdapter,
                LedgerConfigStorageAdapter,
                LedgerIdStorageAdapter,
                LedgerParamsStorageAdapter,
                //ServiceAdapters
                ChainHandleStorageAdapter,
                IdentityStorageAdapter,
                TransactionPoolStorageAdapter,
                //StorageAdapters
                BlockHeaderStorageAdapter,
                BlockStorageAdapter,
                CoinbaseStorageAdapter,
                MerkleTreeStorageAdapter,
                PhysicalDataStorageAdapter,
                TransactionOutputStorageAdapter,
                TransactionStorageAdapter,
                //DataAdapters
                DummyDataStorageAdapter
            )
        schemas.forEach {
            registerSchema(
                it
            )
        }
    }

    internal fun registerSchema(
        schemaProvider: SchemaProvider<out Any>
    ): PersistenceWrapper =
        apply {
            if (!schemas.hasSchema(schemaProvider.id)) {
                createSchema(
                    schemas,
                    schemaProvider
                )
            } else {
                replaceSchema(
                    schemas,
                    schemaProvider
                )
            }
        }

    private fun createSchema(
        schema: ManagedSchemas,
        provider: SchemaProvider<out Any>
    ) {
        val cl = schema.createSchema(provider.id)
        cl?.let {
            provider.properties.forEach {
                cl.createProperty(it.key, it.value)
            }
        }
    }


    private fun replaceSchema(
        schema: ManagedSchemas,
        provider: SchemaProvider<out Any>
    ) {
        val cl = schema.getSchema(provider.id)
        cl?.let {
            val (propsIn, propsNotIn) =
                cl.declaredPropertyNames().partition {
                    it in provider.properties.keys
                }

            if (propsNotIn.isNotEmpty()) {
                //Drop properties that no longer exist in provider.
                propsNotIn.forEach(cl::dropProperty)
            }

            if (propsIn.size != provider.properties.keys.size) {
                //New properties are those in provider that are not already present.
                provider
                    .properties
                    .keys
                    .filter {
                        it !in propsIn
                    }.forEach {
                        cl.createProperty(
                            it,
                            provider.properties.getValue(it)
                        )
                    }
            }
        }
    }

    private fun beginTransaction() =
        apply {
            session.begin()
        }

    private fun commitTransaction() =
        apply {
            session.commit()
        }

    private fun rollbackTransaction() =
        apply {
            session.rollback()
        }


    internal fun closeCurrentSession(): PersistenceWrapper =
        apply {
            session.close()
        }

    internal fun getInstanceSession(): NewInstanceSession = session

    /**
     * Requires:
     * - A [query] with the command to execute
     * and it's arguments.
     * - A [Loadable] that converts from documents to
     * a usable user-typeId that implements [LedgerData].
     *
     * Returns an [Outcome] with a possible [DataFailure].
     */
    private fun <T : LedgerData> queryUniqueResult(
        query: GenericQuery,
        loader: Loadable<T>
    ): Outcome<T, DataFailure> =
        tryOrDataUnknownFailure {
            session.query(query).use {
                if (hasNext()) {
                    loader.load(next().element)
                } else {
                    Outcome.Error<DataFailure>(
                        DataFailure.NonExistentData(
                            "Empty ResultSet for ${query.query}"
                        )
                    )
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
    internal fun <T : LedgerContract> queryUniqueResult(
        query: GenericQuery,
        loader: StorageLoadable<T>
    ): Outcome<T, LoadFailure> =
        tryOrLoadUnknownFailure {
            session.query(query).use {
                if (hasNext()) {
                    loader.load(
                        ledgerHash,
                        next().element
                    )
                } else {
                    Outcome.Error<LoadFailure>(
                        LoadFailure.NonExistentData(
                            "Empty ResultSet for ${query.query}"
                        )
                    )
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
     *
     * *Note:* An extra argument is required for any
     * query over a [ServiceLoadable]:
     * - The common wrapper itself.
     *
     * Returns an [Outcome] with a possible [LedgerFailure].
     */
    internal fun <T : ServiceClass> queryUniqueResult(
        query: GenericQuery,
        loader: ServiceLoadable<T>
    ): Outcome<T, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            session.query(query).use {
                if (hasNext()) {
                    loader.load(
                        ledgerHash, next().element
                    )
                } else {
                    Outcome.Error<LedgerFailure>(
                        LedgerFailure.NonExistentData(
                            query.query
                        )
                    )
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
    private fun <T : Any> queryUniqueResult(
        query: GenericQuery,
        loader: QueryLoadable<T>
    ): Outcome<T, QueryFailure> =
        tryOrQueryUnknownFailure {
            session.query(query).use {
                if (hasNext()) {
                    loader.load(next().element)
                } else {
                    Outcome.Error<QueryFailure>(
                        QueryFailure.NonExistentData(
                            "Empty ResultSet for ${query.query}"
                        )
                    )
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
    private fun <T : LedgerData> queryResults(
        query: GenericQuery,
        loader: Loadable<T>
    ): Outcome<Sequence<T>, DataFailure> =
        tryOrDataUnknownFailure {
            session.query(query).use {
                asSequence().map {
                    loader.load(it.element)
                }.allValues()
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
    internal fun <T : LedgerContract> queryResults(
        query: GenericQuery,
        loader: StorageLoadable<T>
    ): Outcome<Sequence<T>, LoadFailure> =
        tryOrLoadUnknownFailure {
            session.query(query).use {
                asSequence().map {
                    loader.load(ledgerHash, it.element)
                }.allValues()
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
     * *Note:* One extra argument is required for any
     * query over a [ServiceLoadable]:
     * - The common wrapper itself.
     *
     *
     * Returns an [Outcome] with a possible [LedgerFailure]
     * over a [Sequence].
     */
    internal fun <T : ServiceClass> queryResults(
        query: GenericQuery,
        loader: ServiceLoadable<T>
    ): Outcome<Sequence<T>, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            session.query(query).use {
                asSequence().map {
                    loader.load(ledgerHash, it.element)
                }.allValues()
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
    internal fun <T : Any> queryResults(
        query: GenericQuery,
        loader: QueryLoadable<T>
    ): Outcome<Sequence<T>, QueryFailure> =
        tryOrQueryUnknownFailure {
            session.query(query).use {
                asSequence().map {
                    loader.load(it.element)
                }.allValues()
            }
        }


    /**
     * Should be called when querying is finished to
     * reset the persistence context.
     */
    internal fun clearTransactionsContext() =
        closeCurrentSession()


    /**
     * Persists an [element] to an active [ManagedSession]
     * in a synchronous manner, in a transaction context.
     *
     * Returns an [Outcome] with a possible [QueryFailure]
     * over a [StorageID].
     */
    @Synchronized
    internal fun <T> persistEntity(
        element: T,
        storable: EagerStorable<T>
    ): Outcome<StorageID, QueryFailure> =
        tryOrQueryUnknownFailure {
            beginTransaction()
            val elem = storable.persist(element, session)
            val r = session.save(elem)
            if (r != null) {
                commitTransaction()
                Outcome.Ok(r.identity)
            } else {
                rollbackTransaction()
                Outcome.Error<QueryFailure>(
                    QueryFailure.NonExistentData(
                        "Failed to save element ${elem.json}"
                    )
                )
            }
        }

    /**
     * Persists an [element] to an active [ManagedSession]
     * in a synchronous manner.
     *
     * Returns an [Outcome] with a possible [QueryFailure]
     * over a [StorageID].
     */
    @Synchronized
    internal fun <T> persistEntity(
        element: T,
        storable: EagerStorable<T>,
        cluster: String
    ): Outcome<StorageID, QueryFailure> =
        tryOrQueryUnknownFailure {
            beginTransaction()
            val elem = storable.persist(element, session)
            val r = session.save(elem, cluster)
            if (r != null) {
                commitTransaction()
                Outcome.Ok(r.identity)
            } else {
                rollbackTransaction()
                Outcome.Error<QueryFailure>(
                    QueryFailure.NonExistentData(
                        "Failed to save element ${elem.json}"
                    )
                )
            }
        }


    /**
     * Updates an [element] in place with its invalidated
     * fields in a synchronous manner, in a transaction
     * context.
     *
     * Returns an [Outcome] with a possible [UpdateFailure]
     * over a [StorageID].
     */
    @Synchronized
    internal fun <T> updateEntity(
        element: StorageAware<T>
    ): Outcome<StorageID, UpdateFailure> =
        tryOrUpdateUnknownFailure {
            beginTransaction()
            element.update(session).peekSuccess {
                commitTransaction()
            }.peekFailure {
                rollbackTransaction()
            }
        }


    // ------------------------------
    // Identity transaction.
    //
    // ------------------------------


    internal fun getLedgerIdentityByTag(
        id: String
    ): Outcome<Identity, LoadFailure> =
        IdentityStorageAdapter.let {
            queryUniqueResult(
                UnspecificQuery(
                    """
                        SELECT 
                        FROM ${it.id}
                        WHERE id = :id
                    """.trimIndent(),
                    mapOf(
                        "id" to id
                    )
                ),
                it
            )
        }

    internal fun getLedgerHandleByHash(
        hash: Hash
    ): Outcome<LedgerConfig, LedgerHandle.Failure> =
        session.query(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${LedgerConfigStorageAdapter.id}
                    WHERE hashId = :hashId
                """.trimIndent(),
                mapOf(
                    "hashId" to hash.bytes
                )
            )
        ).let {
            if (it.hasNext()) {
                LedgerConfigStorageAdapter.load(
                    hash, it.next().element
                )
            } else {
                Outcome.Error(
                    LedgerHandle.Failure.NonExistentLedger
                )
            }
        }
}
