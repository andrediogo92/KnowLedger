package pt.um.masb.ledger.service.transactions

import pt.um.masb.common.data.LedgerData
import pt.um.masb.common.database.ManagedSchemas
import pt.um.masb.common.database.ManagedSession
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageID
import pt.um.masb.common.database.query.Filters
import pt.um.masb.common.database.query.GenericQuery
import pt.um.masb.common.database.query.GenericSelect
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.results.allValues
import pt.um.masb.common.storage.LedgerContract
import pt.um.masb.common.storage.adapters.Loadable
import pt.um.masb.common.storage.adapters.SchemaProvider
import pt.um.masb.common.storage.adapters.Storable
import pt.um.masb.common.storage.results.DataFailure
import pt.um.masb.common.storage.results.QueryFailure
import pt.um.masb.ledger.config.adapters.BlockParamsStorageAdapter
import pt.um.masb.ledger.config.adapters.ChainIdStorageAdapter
import pt.um.masb.ledger.config.adapters.LedgerIdStorageAdapter
import pt.um.masb.ledger.config.adapters.LedgerParamsStorageAdapter
import pt.um.masb.ledger.data.adapters.DummyDataStorageAdapter
import pt.um.masb.ledger.data.adapters.MerkleTreeStorageAdapter
import pt.um.masb.ledger.data.adapters.PhysicalDataStorageAdapter
import pt.um.masb.ledger.results.tryOrDataUnknownFailure
import pt.um.masb.ledger.results.tryOrLedgerUnknownFailure
import pt.um.masb.ledger.results.tryOrLoadUnknownFailure
import pt.um.masb.ledger.results.tryOrQueryUnknownFailure
import pt.um.masb.ledger.service.Identity
import pt.um.masb.ledger.service.LedgerConfig
import pt.um.masb.ledger.service.ServiceClass
import pt.um.masb.ledger.service.adapters.ChainHandleStorageAdapter
import pt.um.masb.ledger.service.adapters.IdentityStorageAdapter
import pt.um.masb.ledger.service.adapters.LedgerConfigStorageAdapter
import pt.um.masb.ledger.service.adapters.ServiceLoadable
import pt.um.masb.ledger.service.handles.LedgerHandle
import pt.um.masb.ledger.service.results.LedgerFailure
import pt.um.masb.ledger.service.results.LoadFailure
import pt.um.masb.ledger.storage.adapters.BlockHeaderStorageAdapter
import pt.um.masb.ledger.storage.adapters.BlockStorageAdapter
import pt.um.masb.ledger.storage.adapters.CoinbaseStorageAdapter
import pt.um.masb.ledger.storage.adapters.QueryLoadable
import pt.um.masb.ledger.storage.adapters.StorageLoadable
import pt.um.masb.ledger.storage.adapters.TransactionOutputStorageAdapter
import pt.um.masb.ledger.storage.adapters.TransactionStorageAdapter


/**
 * A Thread-safe wrapper into a DB context
 * for the ledger library.
 */
internal data class PersistenceWrapper(
    private val session: ManagedSession
) : EntityStore, ServiceClass {
    private val schemas = session.managedSchemas
    internal val isClosed
        get() = session.isClosed

    init {
        registerDefaultSchemas()
    }

    private fun registerDefaultSchemas(
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
            val (propsIn, propsNotIn) = cl
                .declaredPropertyNames()
                .partition {
                    it in provider.properties.keys
                }

            if (propsNotIn.isNotEmpty()) {
                //Drop properties that no longer exist in provider.
                propsNotIn.forEach {
                    cl.dropProperty(it)
                }
            }

            if (propsIn.size != provider.properties.keys.size) {
                //New properties are those in provider that are not already present.
                provider
                    .properties
                    .keys
                    .asSequence()
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
            val res = session.query(
                query.query,
                query.params
            )
            if (res.hasNext()) {
                loader.load(res.next().element)
            } else {
                Outcome.Error<DataFailure>(
                    DataFailure.NonExistentData(
                        "Empty ResultSet for ${query.query}"
                    )
                )
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
        ledgerHash: Hash,
        query: GenericQuery,
        loader: StorageLoadable<T>
    ): Outcome<T, LoadFailure> =
        tryOrLoadUnknownFailure {
            val res = session.query(
                query.query,
                query.params
            )
            if (res.hasNext()) {
                loader.load(
                    ledgerHash,
                    res.next().element
                )
            } else {
                Outcome.Error<LoadFailure>(
                    LoadFailure.NonExistentData(
                        "Empty ResultSet for ${query.query}"
                    )
                )
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
        ledgerHash: Hash,
        query: GenericQuery,
        loader: ServiceLoadable<T>
    ): Outcome<T, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            val res = session.query(
                query.query,
                query.params
            )
            if (res.hasNext()) {
                loader.load(
                    ledgerHash, res.next().element
                )
            } else {
                Outcome.Error<LedgerFailure>(
                    LedgerFailure.NonExistentData(
                        "Empty ResultSet for ${query.query}"
                    )
                )
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
            val res = session.query(
                query.query,
                query.params
            )
            if (res.hasNext()) {
                loader.load(res.next().element)
            } else {
                Outcome.Error<QueryFailure>(
                    QueryFailure.NonExistentData(
                        "Empty ResultSet for ${query.query}"
                    )
                )
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
            session
                .query(query.query, query.params)
                .asSequence()
                .map {
                    loader.load(it.element)
                }.allValues()
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
        ledgerHash: Hash,
        query: GenericQuery,
        loader: StorageLoadable<T>
    ): Outcome<Sequence<T>, LoadFailure> =
        tryOrLoadUnknownFailure {
            session
                .query(query.query, query.params)
                .asSequence()
                .map {
                    loader.load(ledgerHash, it.element)
                }.allValues()
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
        ledgerHash: Hash,
        query: GenericQuery,
        loader: ServiceLoadable<T>
    ): Outcome<Sequence<T>, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            session
                .query(query.query, query.params)
                .asSequence()
                .map {
                    loader.load(ledgerHash, it.element)
                }.allValues()
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
            session
                .query(query.query, query.params)
                .asSequence()
                .map {
                    loader.load(it.element)
                }.allValues()
        }


    /**
     * Should be called when querying is finished to
     * reset the persistence context.
     */
    internal fun clearTransactionsContext() =
        closeCurrentSession()


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
        storable: Storable<T>
    ): Outcome<StorageID, QueryFailure> =
        tryOrQueryUnknownFailure {
            val elem = storable.store(element, session)
            val r = session.save(elem)
            if (r != null) {
                Outcome.Ok(r.identity)
            } else {
                Outcome.Error<QueryFailure>(
                    QueryFailure.NonExistentData(
                        "Failed to save element ${elem.print()}"
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
        storable: Storable<T>,
        cluster: String
    ): Outcome<StorageID, QueryFailure> =
        tryOrQueryUnknownFailure {
            val elem = storable.store(element, session)
            val r = session.save(elem, cluster)
            if (r != null) {
                Outcome.Ok(r.identity)
            } else {
                Outcome.Error<QueryFailure>(
                    QueryFailure.NonExistentData(
                        "Failed to save element ${elem.print()}"
                    )
                )
            }
        }


    // ------------------------------
    // Identity transaction.
    //
    // ------------------------------


    internal fun getLedgerIdentityByTag(
        ledgerHash: Hash,
        id: String
    ): Outcome<Identity, LoadFailure> =
        IdentityStorageAdapter.let {
            queryUniqueResult(
                ledgerHash,
                GenericSelect(
                    it.id
                ).withSimpleFilter(
                    Filters.WHERE,
                    "id",
                    "id",
                    id
                ),
                it
            )
        }


    internal fun getLedgerHandleByHash(
        hashId: Hash
    ): Outcome<LedgerConfig, LedgerHandle.Failure> =
        session.query(
            "SELECT * FROM ${LedgerConfigStorageAdapter.id} WHERE hashId = :hashId",
            mapOf(
                "hashId" to hashId
            )
        ).let {
            if (it.hasNext()) {
                LedgerConfigStorageAdapter.load(hashId, it.next().element)
            } else {
                Outcome.Error(
                    LedgerHandle.Failure.NonExistentLedger
                )
            }
        }

}
