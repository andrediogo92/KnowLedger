package org.knowledger.ledger.chain.handles

import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.ledger.chain.LedgerInfo
import org.knowledger.ledger.chain.ServiceClass
import org.knowledger.ledger.chain.handles.builder.AbstractLedgerBuilder
import org.knowledger.ledger.chain.handles.builder.LedgerByHash
import org.knowledger.ledger.chain.handles.builder.LedgerByTag
import org.knowledger.ledger.chain.results.LedgerBuilderFailure
import org.knowledger.ledger.chain.transactions.PersistenceWrapper
import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.core.adapters.Tag
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.digest.classDigest
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.database.results.QueryFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.err
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.Identity
import org.knowledger.ledger.storage.LedgerData
import org.knowledger.ledger.storage.results.LedgerFailure
import org.knowledger.ledger.storage.results.LoadFailure
import org.knowledger.ledger.storage.results.intoLedger
import kotlin.reflect.KClass


/**
 * Create a geographically unbounded ledger.
 */
@OptIn(ExperimentalSerializationApi::class)
class LedgerHandle internal constructor(builder: AbstractLedgerBuilder) :
    ServiceClass {
    internal val pw: PersistenceWrapper = builder.persistenceWrapper
    val ledgerInfo: LedgerInfo = builder.ledgerInfo
    val ledgerHash = ledgerInfo.ledgerId.hash
    val hashers: Hashers = ledgerInfo.hashers
    val encoder: BinaryFormat = ledgerInfo.encoder
    val isClosed: Boolean get() = pw.isClosed

    fun close() {
        pw.closeCurrentSession()
    }

    val knownChainTypes: Outcome<List<Tag>, LoadFailure>
        get() = pw.getKnownChainHandleTypes()

    internal val knownChainIDs: Outcome<List<StorageID>, LoadFailure>
        get() = pw.getKnownChainHandleIDs()

    val knownChains: Outcome<List<ChainHandle>, LoadFailure>
        get() = pw.getKnownChainHandles()

    fun getIdentityByTag(tag: String): Outcome<Identity, LoadFailure> =
        pw.getLedgerIdentityByTag(tag)

    /**
     * Adds the specified adapter to known adapters and returns
     * true if the element has been added, false if the adapter
     * is already known.
     */
    fun addStorageAdapter(adapter: AbstractStorageAdapter<out LedgerData>): Boolean =
        pw.addAdapter(adapter).also { adapterAdded ->
            if (adapterAdded) {
                pw.registerSchema(adapter)
            }
        }


    fun <T : LedgerData> getChainHandleOf(clazz: KClass<in T>): Outcome<ChainHandle,
            LedgerFailure> =
        if (pw.hasAdapter(clazz)) {
            pw.getChainHandle(clazz.classDigest(hashers)).mapError(LoadFailure::intoLedger)
        } else {
            LedgerFailure.NoKnownStorageAdapter(
                "No known storage adapter for ${clazz.qualifiedName}").err()
        }

    fun <T : LedgerData> getChainHandleOf(
        adapter: AbstractStorageAdapter<out T>,
    ): Outcome<ChainHandle, LedgerFailure> =
        if (pw.hasAdapter(adapter.tag)) {
            pw.getChainHandle(adapter.hash).mapError(LoadFailure::intoLedger)
        } else {
            LedgerFailure.NoKnownStorageAdapter("No known storage adapter for $adapter").err()
        }


    fun <T : LedgerData> registerNewChainHandleOf(
        adapter: AbstractStorageAdapter<out T>,
        blockParams: BlockParams = ledgerInfo.factories.blockParamsFactory.create(),
        coinbaseParams: CoinbaseParams =
            ledgerInfo.factories.coinbaseParamsFactory.create(hashers.hashSize),
    ): Outcome<ChainHandle, LedgerFailure> =
        ChainHandle(
            ledgerInfo, pw.context, adapter.tag, adapter.hash, blockParams, coinbaseParams
        ).let { ch ->
            ch.addQueryManager(pw.chainManager(ch.chainId))
            addStorageAdapter(adapter)
            pw.tryAddChainHandle(ch).map { ch }.mapError(QueryFailure::intoLedger)
        }


    internal class Builder {
        fun withLedgerIdentity(identity: String): Outcome<LedgerByTag, LedgerBuilderFailure> =
            if (identity == "") {
                LedgerBuilderFailure.NoIdentitySupplied.err()
            } else {
                LedgerByTag(identity).ok()
            }

        fun byHashRetrieval(hash: Hash): Outcome<LedgerByHash, LedgerBuilderFailure> =
            if (hash == Hash.emptyHash) {
                LedgerBuilderFailure.NoIdentitySupplied.err()
            } else {
                LedgerByHash(hash).ok()
            }
    }
}
