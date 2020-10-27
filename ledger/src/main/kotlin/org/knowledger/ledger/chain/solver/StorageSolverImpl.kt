package org.knowledger.ledger.chain.solver

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getError
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onFailure
import org.knowledger.ledger.adapters.AdaptersCollection
import org.knowledger.ledger.adapters.EagerStorable
import org.knowledger.ledger.chain.handles.ChainHandle
import org.knowledger.ledger.chain.solver.trackers.CollectionTracker
import org.knowledger.ledger.chain.solver.trackers.IndexTracker
import org.knowledger.ledger.chain.solver.trackers.ResultTracker
import org.knowledger.ledger.chain.solver.trackers.StorageTracker
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.database.adapters.Storable
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.err
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.ChainId
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.Identity
import org.knowledger.ledger.storage.LedgerData
import org.knowledger.ledger.storage.LedgerId
import org.knowledger.ledger.storage.LedgerParams
import org.knowledger.ledger.storage.MutableBlock
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.MutableBlockPool
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.MutableTransactionPool
import org.knowledger.ledger.storage.MutableWitness
import org.knowledger.ledger.storage.PhysicalData
import org.knowledger.ledger.storage.PoolTransaction
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.cache.LinkedPairs
import org.knowledger.ledger.storage.cache.LockState
import org.knowledger.ledger.storage.cache.StorageAware
import org.knowledger.ledger.storage.cache.StoragePairs
import org.tinylog.kotlin.Logger

@OptIn(ExperimentalStdlibApi::class)
internal class StorageSolverImpl(val session: ManagedSession) : StorageSolver {

    override fun <T : Any, Store> primeStart(
        state: StorageSolverState, storable: Store, element: T,
    ) where Store : SchemaProvider, Store : EagerStorable<T> {
        cacheStore(state, storable, "", element, StorageTracker.Status.Agnostic)
    }

    //TODO: Eager resolves percolate up the store graph. No resolved stack needed since
    //each document will contain all its links in the end. Just needs to guarantee correct
    //linkage + locks to unlock for elements that appear along call graph.
    override fun resolve(
        state: StorageSolverState, adapters: AdaptersCollection,
    ): Outcome<ResultTracker, DataFailure> {
        var tracker: StorageTracker = state.advanceNext()
        do {
            resolveTracker(state, tracker, adapters).map { storageElement ->
                if (storageElement != null) {
                    state.addResolvedElement(storageElement)
                }
            }.onFailure { failure ->
                state.purge()
                return@resolve failure.err()
            }
            tracker = state.advanceNext()
        } while (state.hasElementsToStore())
        val element = tracker as StorageTracker.Start
        state.addResolvedElement(element.element)
        state.unlockElements()
        return ResultTracker(state.resolvedIterator()).ok()
    }


    private fun resolveTracker(
        state: StorageSolverState, tracker: StorageTracker, adapters: AdaptersCollection,
    ): Outcome<StorageElement?, DataFailure> =
        when (tracker) {
            is StorageTracker.Start -> resolveStart(state, tracker)
            is StorageTracker.Collection -> resolveCollection(state, tracker)
            is StorageTracker.Element -> resolveElement(state, tracker, adapters)?.err() ?: Ok(null)
            is StorageTracker.Error -> DataFailure.UnknownFailure(tracker.key, null).err()
        }


    private fun resolveStart(
        state: StorageSolverState, tracker: StorageTracker.Start,
    ): Outcome<StorageElement?, DataFailure> = when (tracker.status) {
        //Must build and advance twice in indexes
        StorageTracker.Status.InCollection -> {
            Logger.info {
                "Linked in collection is tracked: ${tracker.key}"
            }
            resolveStartLinked(state, tracker).also {
                state.removeLinked()
            }
        }
        //Can just build and advance once
        StorageTracker.Status.Agnostic -> {
            Logger.info {
                "Linked in collection was consumed: ${tracker.key}"
            }
            resolveStartLinked(state, tracker)
        }
    }

    private fun resolveStartLinked(
        state: StorageSolverState, tracker: StorageTracker.Start,
    ): Outcome<StorageElement?, DataFailure> {
        state.removeLinked()
        return state.setToStart(tracker)
    }

    private fun resolveCollection(
        state: StorageSolverState, tracker: StorageTracker.Collection,
    ): Outcome<StorageElement?, DataFailure> =
        when (tracker.status) {
            StorageTracker.Status.InCollection -> {
                Logger.info {
                    "Nested Collection was consumed: ${tracker.key}"
                }
                resolveCollectionLinked(state, tracker)
            }
            StorageTracker.Status.Agnostic -> {
                Logger.info {
                    "Collection was consumed: ${tracker.key}"
                }
                resolveCollectionLinked(state, tracker)
            }
        }

    private fun resolveCollectionLinked(
        state: StorageSolverState, tracker: StorageTracker.Collection,
    ): Outcome<StorageElement?, DataFailure> =
        state.setToCollection().map {
            @Suppress("UNCHECKED_CAST")
            when (tracker.tracker.type) {
                CollectionTracker.Type.List -> it.setElementList(
                    tracker.key, tracker.tracker.collection as List<StorageElement>
                )
                CollectionTracker.Type.Set -> it.setElementSet(
                    tracker.key, tracker.tracker.collection as Set<StorageElement>
                )
            }
            null
        }

    private fun resolveElement(
        state: StorageSolverState, element: StorageTracker.Element, adapters: AdaptersCollection,
    ): DataFailure? = when (val stored = element.store) {
        is StoragePairs.HashList -> resolveHashList(state, stored)
        is StoragePairs.HashSet -> resolveHashSet(state, stored)
        is StoragePairs.Blob -> resolveBlob(state, stored)
        is StoragePairs.LinkedDifficulty -> resolveDifficulty(state, stored)
        is StoragePairs.LinkedHash -> resolveHash(state, stored)
        is StoragePairs.LinkedPayout -> resolvePayout(state, stored)
        is StoragePairs.Native -> resolveNative(state, stored)
        is StoragePairs.LinkedList<*> -> resolveLinkedList(state, adapters, stored)
        is StoragePairs.LinkedSet<*> -> resolveLinkedSet(state, adapters, stored)
        is StoragePairs.Linked<*> -> resolveLinkedElement(state, adapters, stored)
    }

    private fun <T : Any, Store> cacheStore(
        state: StorageState, storageAdapter: Store, key: String,
        element: T, status: StorageTracker.Status,
    ): DataFailure? where Store : EagerStorable<T>, Store : SchemaProvider {
        val result = (element as? StorageAware)?.let { aware ->
            if (aware.lock.state == LockState.Locked) {
                aware.id?.let { storageElement ->
                    state.pushBeginToStore(key, storageElement, status, LockState.Locked).ok()
                } ?: DataFailure.NonExistentData("Element is locked but has no Id").err()
            } else {
                //Is storage aware and just needs to update
                aware.id?.let { storageElement ->
                    state.pushBeginAndLock(key, storageElement, status, aware)
                    storageAdapter.update(element, state)
                } ?: session.newInstance(storageAdapter.id).let { storageElement ->
                    aware.id = storageElement
                    state.pushBeginAndLock(key, storageElement, status, aware)
                    storageAdapter.store(element, state)
                }

            }
        } ?: store(state, storageAdapter, key, element, status)
        return result.getError()
    }

    private fun <T : Any, Store> store(
        state: StorageState, storageAdapter: Store, key: String,
        element: T, status: StorageTracker.Status,
    ): Outcome<Unit, DataFailure> where Store : EagerStorable<T>, Store : SchemaProvider =
        session.newInstance(storageAdapter.id).let { storageElement ->
            state.pushBeginToStore(key, storageElement, status, LockState.Unlocked)
            storageAdapter.store(element, state)
        }

    private fun <T : Any, Store> storeLedger(
        state: StorageState, storageAdapter: Store, key: String,
        element: T, status: StorageTracker.Status,
    ): DataFailure? where Store : Storable<T>, Store : SchemaProvider {
        state.pushBeginToStore(
            key, storageAdapter.store(element, session), status, LockState.Unlocked
        )
        return null
    }

    private fun resolveLinkedElement(
        state: StorageSolverState, adapters: AdaptersCollection, stored: StoragePairs.Linked<*>,
        status: StorageTracker.Status = StorageTracker.Status.Agnostic,
    ): DataFailure? = when (stored.adapterId) {
        AdapterIds.Block -> cacheStore(
            state, adapters.blockStorageAdapter, stored.key,
            stored.element as MutableBlock, status
        )
        AdapterIds.BlockHeader -> cacheStore(
            state, adapters.blockHeaderStorageAdapter, stored.key,
            stored.element as MutableBlockHeader, status
        )
        AdapterIds.BlockParams -> cacheStore(
            state, adapters.blockParamsStorageAdapter, stored.key,
            stored.element as BlockParams, status
        )
        AdapterIds.BlockPool -> cacheStore(
            state, adapters.blockPoolStorageAdapter, stored.key,
            stored.element as MutableBlockPool, status
        )
        AdapterIds.ChainId -> cacheStore(
            state, adapters.chainIdStorageAdapter, stored.key,
            stored.element as ChainId, status
        )
        AdapterIds.ChainHandle -> cacheStore(
            state, adapters.chainHandleStorageAdapter, stored.key,
            stored.element as ChainHandle, status
        )
        AdapterIds.Coinbase -> cacheStore(
            state, adapters.coinbaseStorageAdapter, stored.key,
            stored.element as MutableCoinbase, status
        )
        AdapterIds.CoinbaseHeader -> cacheStore(
            state, adapters.coinbaseHeaderStorageAdapter, stored.key,
            stored.element as MutableCoinbaseHeader, status
        )
        AdapterIds.CoinbaseParams -> cacheStore(
            state, adapters.coinbaseParamsStorageAdapter, stored.key,
            stored.element as CoinbaseParams, status
        )
        AdapterIds.Identity -> store(
            state, adapters.identityStorageAdapter, stored.key,
            stored.element as Identity, status
        ).getError()
        AdapterIds.LedgerData ->
            adapters.findAdapter(stored.element::class)?.let { adapter ->
                storeLedger(state, adapter, stored.key, stored.element as LedgerData, status)
            }
        AdapterIds.LedgerId -> cacheStore(
            state, adapters.ledgerIdStorageAdapter, stored.key,
            stored.element as LedgerId, status
        )
        AdapterIds.LedgerParams -> cacheStore(
            state, adapters.ledgerParamsStorageAdapter, stored.key,
            stored.element as LedgerParams, status
        )
        AdapterIds.MerkleTree -> cacheStore(
            state, adapters.merkleTreeStorageAdapter, stored.key,
            stored.element as MutableMerkleTree, status
        )
        AdapterIds.PhysicalData -> store(
            state, adapters.physicalDataStorageAdapter, stored.key,
            stored.element as PhysicalData, status
        ).getError()
        AdapterIds.PoolTransaction -> cacheStore(
            state, adapters.poolTransactionStorageAdapter, stored.key,
            stored.element as PoolTransaction, status
        )
        AdapterIds.Transaction -> cacheStore(
            state, adapters.transactionStorageAdapter, stored.key,
            stored.element as MutableTransaction, status
        )
        AdapterIds.TransactionOutput -> cacheStore(
            state, adapters.transactionOutputStorageAdapter, stored.key,
            stored.element as TransactionOutput, status
        )
        AdapterIds.TransactionPool -> cacheStore(
            state, adapters.transactionPoolStorageAdapter, stored.key,
            stored.element as MutableTransactionPool, status
        )
        AdapterIds.Witness -> cacheStore(
            state, adapters.witnessStorageAdapter, stored.key,
            stored.element as MutableWitness, status
        )
    }

    private fun resolveLinkedCollection(
        state: StorageSolverState, adapters: AdaptersCollection,
        stored: LinkedPairs, index: IndexTracker, elements: Iterable<Any>,
    ): DataFailure? = elements.mapNotNull { elem ->
        state.trackCollection(index)
        val pairs = StoragePairs.Linked<Any>("", stored.adapterId)
        pairs.replace(elem)
        resolveLinkedElement(state, adapters, pairs, StorageTracker.Status.InCollection)
    }.firstOrNull()

    private fun resolveLinkedList(
        state: StorageSolverState, adapters: AdaptersCollection,
        stored: StoragePairs.LinkedList<*>,
    ): DataFailure? = resolveLinkedCollection(
        state, adapters, stored,
        state.pushCollectionToStore(stored.key, CollectionTracker.Type.List),
        stored.element.asIterable()
    )

    private fun resolveLinkedSet(
        state: StorageSolverState, adapters: AdaptersCollection,
        stored: StoragePairs.LinkedSet<*>,
    ): DataFailure? = resolveLinkedCollection(
        state, adapters, stored,
        state.pushCollectionToStore(stored.key, CollectionTracker.Type.Set),
        stored.element.asIterable()
    )

    private inline fun resolveNative(
        state: StorageSolverState, resolver: StorageElement.() -> Unit,
    ): DataFailure? {
        state.extractStart().resolver()
        return null
    }

    private fun resolveHashList(
        state: StorageSolverState, stored: StoragePairs.HashList,
    ): DataFailure? = resolveNative(state) { setHashList(stored.key, stored.element) }

    private fun resolveHashSet(
        state: StorageSolverState, stored: StoragePairs.HashSet,
    ): DataFailure? = resolveNative(state) { setHashSet(stored.key, stored.element) }

    private fun resolveBlob(state: StorageSolverState, stored: StoragePairs.Blob): DataFailure? =
        resolveNative(state) { setStorageBytes(stored.key, session.newInstance(stored.element)) }

    private fun resolveDifficulty(
        state: StorageSolverState, stored: StoragePairs.LinkedDifficulty,
    ): DataFailure? = resolveNative(state) {
        setStorageProperty(stored.key, stored.element.bytes)
    }

    private fun resolveHash(
        state: StorageSolverState, stored: StoragePairs.LinkedHash,
    ): DataFailure? = resolveNative(state) { setHashProperty(stored.key, stored.element) }

    private fun resolvePayout(
        state: StorageSolverState, stored: StoragePairs.LinkedPayout,
    ): DataFailure? = resolveNative(state) { setPayoutProperty(stored.key, stored.element) }

    private fun resolveNative(
        state: StorageSolverState, stored: StoragePairs.Native,
    ): DataFailure? = resolveNative(state) { setStorageProperty(stored.key, stored.element) }


}