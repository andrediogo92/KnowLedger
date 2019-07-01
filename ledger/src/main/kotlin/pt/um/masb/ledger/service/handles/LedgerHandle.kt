package pt.um.masb.ledger.service.handles

import pt.um.masb.common.data.DataFormula
import pt.um.masb.common.data.LedgerData
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageID
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.misc.base64Encode
import pt.um.masb.common.misc.stringToPrivateKey
import pt.um.masb.common.misc.stringToPublicKey
import pt.um.masb.common.results.Failable
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.results.fold
import pt.um.masb.common.storage.adapters.AbstractStorageAdapter
import pt.um.masb.common.storage.results.QueryFailure
import pt.um.masb.ledger.data.adapters.DummyDataStorageAdapter
import pt.um.masb.ledger.results.intoLedger
import pt.um.masb.ledger.service.Identity
import pt.um.masb.ledger.service.LedgerConfig
import pt.um.masb.ledger.service.LedgerContainer
import pt.um.masb.ledger.service.ServiceClass
import pt.um.masb.ledger.service.handles.builder.AbstractLedgerBuilder
import pt.um.masb.ledger.service.handles.builder.LedgerByHash
import pt.um.masb.ledger.service.handles.builder.LedgerByTag
import pt.um.masb.ledger.service.results.LedgerFailure
import pt.um.masb.ledger.storage.transactions.PersistenceWrapper
import pt.um.masb.ledger.storage.transactions.getChainHandle
import pt.um.masb.ledger.storage.transactions.getKnownChainHandleIDs
import pt.um.masb.ledger.storage.transactions.getKnownChainHandleTypes
import pt.um.masb.ledger.storage.transactions.getKnownChainHandles
import pt.um.masb.ledger.storage.transactions.tryAddChainHandle
import java.security.KeyPair


/**
 * Create a geographically unbounded ledger.
 */
class LedgerHandle internal constructor(
    builder: AbstractLedgerBuilder
) : ServiceClass {
    private val pw: PersistenceWrapper = builder.persistenceWrapper
    val ledgerConfig: LedgerConfig = builder.ledgerConfig
    val hasher: Hasher = builder.hasher
    val isClosed: Boolean
        get() = pw.isClosed

    fun close() {
        pw.closeCurrentSession()
    }

    //TODO: efficiently retrieve chains registered for this ledger.
    val knownChainTypes: Outcome<Sequence<String>, QueryFailure>
        get() = pw.getKnownChainHandleTypes()

    internal val knownChainIDs: Outcome<Sequence<StorageID>, QueryFailure>
        get() = pw.getKnownChainHandleIDs()

    val knownChains: Outcome<Sequence<ChainHandle>, LedgerFailure>
        get() = pw.getKnownChainHandles(ledgerConfig.ledgerId.hashId)

    fun getIdentById(id: String): Identity? {
        val ident: StorageElement? = pw.getIdent(id)
        return if (ident != null) {
            val keyPair = KeyPair(
                stringToPublicKey(ident.getStorageProperty("publicKey")),
                stringToPrivateKey(ident.getStorageProperty("privateKey"))
            )
            Identity(id, keyPair)
        } else {
            null
        }
    }

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
                ledgerConfig.ledgerId.hashId,
                clazz
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

        internal fun getContainer(hash: Hash): LedgerContainer? =
            containers[base64Encode(hash)]


        internal fun getHasher(ledgerHash: Hash): Hasher? =
            containers[base64Encode(ledgerHash)]?.hasher

        internal fun getFormula(ledgerHash: Hash): DataFormula? =
            containers[base64Encode(ledgerHash)]?.formula
    }
}
