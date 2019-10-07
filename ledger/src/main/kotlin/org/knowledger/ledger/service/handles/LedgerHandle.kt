package org.knowledger.ledger.service.handles

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.core.data.DefaultDiff
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.misc.base64DecodedToHash
import org.knowledger.ledger.core.misc.base64Encoded
import org.knowledger.ledger.core.misc.classDigest
import org.knowledger.ledger.core.results.Failable
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.fold
import org.knowledger.ledger.core.storage.adapters.AbstractStorageAdapter
import org.knowledger.ledger.core.storage.results.QueryFailure
import org.knowledger.ledger.data.DataFormula
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.data.Hashers
import org.knowledger.ledger.data.LedgerData
import org.knowledger.ledger.data.Tag
import org.knowledger.ledger.data.adapters.DummyDataStorageAdapter
import org.knowledger.ledger.results.intoLedger
import org.knowledger.ledger.service.Identity
import org.knowledger.ledger.service.LedgerContainer
import org.knowledger.ledger.service.ServiceClass
import org.knowledger.ledger.service.handles.builder.AbstractLedgerBuilder
import org.knowledger.ledger.service.handles.builder.LedgerByHash
import org.knowledger.ledger.service.handles.builder.LedgerByTag
import org.knowledger.ledger.service.handles.builder.LedgerConfig
import org.knowledger.ledger.service.results.LedgerFailure
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.service.transactions.PersistenceWrapper
import org.knowledger.ledger.service.transactions.getChainHandle
import org.knowledger.ledger.service.transactions.getKnownChainHandleIDs
import org.knowledger.ledger.service.transactions.getKnownChainHandleTypes
import org.knowledger.ledger.service.transactions.getKnownChainHandles
import org.knowledger.ledger.service.transactions.tryAddChainHandle
import org.knowledger.ledger.core.results.Failure as CoreFailure


/**
 * Create a geographically unbounded ledger.
 */
class LedgerHandle internal constructor(
    builder: AbstractLedgerBuilder
) : ServiceClass {
    private val pw: PersistenceWrapper = builder.persistenceWrapper
    val ledgerConfig: LedgerConfig = builder.ledgerConfig
    val ledgerHash = ledgerConfig.ledgerId.hash
    val hasher: Hashers = builder.hasher
    val encoder: BinaryFormat = builder.encoder
    val isClosed: Boolean
        get() = pw.isClosed

    fun close() {
        pw.closeCurrentSession()
        containers.remove(ledgerHash)
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
        dataAdapters.add(adapter).also {
            if (it) {
                pw.registerSchema(adapter)
            }
        }


    fun <T : LedgerData> getChainHandleOf(
        clazz: Class<in T>
    ): Outcome<ChainHandle, LedgerFailure> =
        if (dataAdapters.any { it.clazz == clazz }) {
            pw.getChainHandle(clazz.classDigest(hasher))
        } else {
            Outcome.Error(
                LedgerFailure.NoKnownStorageAdapter(
                    "No known storage adapter for ${clazz.name}"
                )
            )
        }

    fun <T : LedgerData> getChainHandleOf(
        adapter: AbstractStorageAdapter<out T>
    ): Outcome<ChainHandle, LedgerFailure> =
        if (dataAdapters.any { it.id == adapter.id }) {
            pw.getChainHandle(
                adapter.id
            )
        } else {
            Outcome.Error(
                LedgerFailure.NoKnownStorageAdapter(
                    "No known storage adapter for $adapter"
                )
            )
        }


    fun <T : LedgerData> registerNewChainHandleOf(
        adapter: AbstractStorageAdapter<out T>
    ): Outcome<ChainHandle, LedgerFailure> =
        ChainHandle(
            adapter.id.base64DecodedToHash(),
            ledgerConfig.ledgerId.hash,
            hasher, encoder
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
        fun withLedgerIdentity(
            identity: String
        ): Outcome<LedgerByTag, Failure> =
            if (identity == "") {
                Outcome.Error(
                    Failure.NoIdentitySupplied
                )
            } else {
                Outcome.Ok(LedgerByTag(identity))
            }

        fun byHashRetrieval(
            hash: Hash
        ): Outcome<LedgerByHash, Failure> =
            if (hash == Hash.emptyHash) {
                Outcome.Error(
                    Failure.NoIdentitySupplied
                )
            } else {
                Outcome.Ok(LedgerByHash(hash))
            }
    }

    sealed class Failure : CoreFailure {
        class PathCannotResolveAsDirectory(
            cause: String
        ) : Failure() {
            override val failable: Failable.LightFailure =
                Failable.LightFailure(cause)
        }

        object NoIdentitySupplied : Failure() {
            override val failable: Failable.LightFailure =
                Failable.LightFailure(
                    "No hash or identity supplied to builder."
                )
        }

        object NonExistentLedger : Failure() {
            override val failable: Failable.LightFailure =
                Failable.LightFailure(
                    "No ledger matching hash in DB"
                )
        }

        class NotRegisteredDataFormula(
            cause: String
        ) : Failure() {
            override val failable: Failable.LightFailure =
                Failable.LightFailure(cause)
        }

        class UnknownFailure(
            cause: String,
            exception: Exception?
        ) : Failure() {
            override val failable: Failable.HardFailure =
                Failable.HardFailure(cause, exception)
        }

        class Propagated(
            pointOfFailure: String,
            failable: Failable
        ) : Failure() {
            override val failable: Failable.PropagatedFailure =
                Failable.PropagatedFailure(pointOfFailure, failable)
        }
    }


    companion object {
        private val knownFormulas =
            mutableSetOf<DataFormula>(DefaultDiff)

        private val dataAdapters =
            mutableSetOf<AbstractStorageAdapter<out LedgerData>>(
                DummyDataStorageAdapter
            )

        internal val containers =
            mutableMapOf<Hash, LedgerContainer>()

        fun getStorageAdapter(
            dataName: Tag
        ): AbstractStorageAdapter<out LedgerData>? =
            dataAdapters.find {
                it.id == dataName.base64Encoded()
            }

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

        internal fun getContainer(
            ledgerHash: Hash
        ): LedgerContainer? =
            containers[ledgerHash]


        internal fun getHasher(
            ledgerHash: Hash
        ): Hashers? =
            containers[ledgerHash]?.hasher

        internal fun getFormula(
            ledgerHash: Hash
        ): DataFormula? =
            containers[ledgerHash]?.formula

        fun registerFormula(
            formula: DataFormula
        ) {
            knownFormulas += formula
        }

        internal fun findFormula(
            formula: Hash, hasher: Hashers
        ): DataFormula? =
            knownFormulas.find {
                it.classDigest(hasher) == (formula)
            }
    }
}
