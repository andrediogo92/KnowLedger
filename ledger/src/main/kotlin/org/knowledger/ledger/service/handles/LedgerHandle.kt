package org.knowledger.ledger.service.handles

import org.knowledger.common.data.DataFormula
import org.knowledger.common.data.LedgerData
import org.knowledger.common.database.StorageID
import org.knowledger.common.hash.Hash
import org.knowledger.common.hash.Hasher
import org.knowledger.common.misc.base64Encode
import org.knowledger.common.misc.extractIdFromClass
import org.knowledger.common.results.Failable
import org.knowledger.common.results.Outcome
import org.knowledger.common.results.fold
import org.knowledger.common.storage.adapters.AbstractStorageAdapter
import org.knowledger.common.storage.results.QueryFailure
import org.knowledger.ledger.data.adapters.DummyDataStorageAdapter
import org.knowledger.ledger.results.intoLedger
import org.knowledger.ledger.service.Identity
import org.knowledger.ledger.service.LedgerConfig
import org.knowledger.ledger.service.LedgerContainer
import org.knowledger.ledger.service.ServiceClass
import org.knowledger.ledger.service.handles.builder.AbstractLedgerBuilder
import org.knowledger.ledger.service.handles.builder.LedgerByHash
import org.knowledger.ledger.service.handles.builder.LedgerByTag
import org.knowledger.ledger.service.results.LedgerFailure
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.service.transactions.PersistenceWrapper
import org.knowledger.ledger.service.transactions.getChainHandle
import org.knowledger.ledger.service.transactions.getKnownChainHandleIDs
import org.knowledger.ledger.service.transactions.getKnownChainHandleTypes
import org.knowledger.ledger.service.transactions.getKnownChainHandles
import org.knowledger.ledger.service.transactions.tryAddChainHandle


/**
 * Create a geographically unbounded ledger.
 */
class LedgerHandle internal constructor(
    builder: AbstractLedgerBuilder
) : ServiceClass {
    private val pw: PersistenceWrapper = builder.persistenceWrapper
    val ledgerConfig: LedgerConfig = builder.ledgerConfig
    val ledgerHash = ledgerConfig.ledgerId.hashId
    val hasher: Hasher = builder.hasher
    val isClosed: Boolean
        get() = pw.isClosed

    fun close() {
        pw.closeCurrentSession()
        containers.remove(ledgerHash.base64Encode())
    }

    val knownChainTypes: Outcome<Sequence<String>, QueryFailure>
        get() = pw.getKnownChainHandleTypes()

    internal val knownChainIDs: Outcome<Sequence<StorageID>, QueryFailure>
        get() = pw.getKnownChainHandleIDs()

    val knownChains: Outcome<Sequence<ChainHandle>, LedgerFailure>
        get() = pw.getKnownChainHandles()

    fun getIdentityByTag(
        tag: String
    ): Outcome<Identity, LoadFailure> =
        pw.getLedgerIdentityByTag(tag)

    /**
     * Adds the specified adapter to known adapters and returns
     * true if the element has been added, false if the adapter
     * is already known.
     */
    fun addStorageAdapter(
        adapter: AbstractStorageAdapter<out LedgerData>
    ): Boolean =
        dataAdapters.add(adapter).apply {
            if (this) {
                pw.registerSchema(adapter)
            }
        }


    fun <T : LedgerData> getChainHandleOf(
        clazz: Class<in T>
    ): Outcome<ChainHandle, LedgerFailure> =
        if (dataAdapters.any { it.clazz == clazz }) {
            pw.getChainHandle(
                clazz.extractIdFromClass()
            )
        } else {
            Outcome.Error(
                LedgerFailure.NoKnownStorageAdapter(
                    "No known storage adapter for ${clazz.name}"
                )
            )
        }

    fun <T : LedgerData> registerNewChainHandleOf(
        adapter: AbstractStorageAdapter<out T>
    ): Outcome<ChainHandle, LedgerFailure> =
        ChainHandle(
            adapter.id, ledgerConfig.ledgerId.hashId, hasher
        ).let { ch ->
            addStorageAdapter(adapter)
            pw.tryAddChainHandle(ch).fold(
                {
                    Outcome.Error(
                        it.intoLedger()
                    )
                },
                {
                    Outcome.Ok(ch)
                }
            )
        }


    class Builder {
        fun withLedgerIdentity(identity: String): Outcome<LedgerByTag, Failure> =
            if (identity == "") {
                Outcome.Error(
                    Failure.NoIdentitySupplied
                )
            } else {
                Outcome.Ok(LedgerByTag(identity))
            }

        fun byHashRetrieval(hash: Hash): Outcome<LedgerByHash, Failure> =
            if (hash == Hash.emptyHash) {
                Outcome.Error(
                    Failure.NoIdentitySupplied
                )
            } else {
                Outcome.Ok(LedgerByHash(hash))
            }
    }

    sealed class Failure : Failable {
        data class PathCannotResolveAsDirectory(
            override val cause: String
        ) : Failure()

        object NoIdentitySupplied : Failure() {
            override val cause: String
                get() = "No hash or identity supplied to builder."
        }

        object NonExistentLedger : Failure() {
            override val cause: String
                get() = "No ledger matching hash in DB"
        }

        /**
         * Reserved for direct irrecoverable errors.
         * Query failures will wrap exceptions if thrown.
         */
        data class UnknownFailure(
            override val cause: String,
            val exception: Exception? = null
        ) : Failure()

        /**
         * Reserved for indirect irrecoverable errors propagated
         * by some internal result.
         */
        data class Propagated(
            val pointOfFailure: String,
            val failable: Failable
        ) : Failure() {
            override val cause: String
                get() = "$pointOfFailure: ${failable.cause}"
        }
    }


    companion object {
        private val dataAdapters =
            mutableSetOf<AbstractStorageAdapter<out LedgerData>>(
                DummyDataStorageAdapter
            )

        internal val containers =
            mutableMapOf<String, LedgerContainer>()

        fun getStorageAdapter(
            dataName: String
        ): AbstractStorageAdapter<out LedgerData>? =
            dataAdapters.find {
                it.id == dataName
            }

        fun getStorageAdapter(
            clazz: Class<out LedgerData>
        ): AbstractStorageAdapter<out LedgerData>? =
            dataAdapters.find {
                it.clazz == clazz
            }

        internal fun getContainer(ledgerHash: Hash): LedgerContainer? =
            containers[ledgerHash.base64Encode()]


        internal fun getHasher(ledgerHash: Hash): Hasher? =
            containers[ledgerHash.base64Encode()]?.hasher

        internal fun getFormula(ledgerHash: Hash): DataFormula? =
            containers[ledgerHash.base64Encode()]?.formula
    }
}
