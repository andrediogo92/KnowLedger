package pt.um.lei.masb.blockchain.persistance

import com.orientechnologies.orient.core.db.document.ODatabaseDocument
import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.Block
import pt.um.lei.masb.blockchain.BlockChain
import pt.um.lei.masb.blockchain.BlockChainId
import pt.um.lei.masb.blockchain.BlockHeader
import pt.um.lei.masb.blockchain.Hash
import pt.um.lei.masb.blockchain.Transaction
import pt.um.lei.masb.blockchain.data.Loadable
import pt.um.lei.masb.blockchain.data.Storable
import pt.um.lei.masb.blockchain.data.logger
import pt.um.lei.masb.blockchain.persistance.PersistenceWrapper.session
import java.security.PublicKey

typealias Loaders = MutableMap<Hash, Loader>

//TODO all the queries.


/**
 * Not to be used directly.
 * Requires knowledge of inner workings of DB.
 *
 * Requires:
 * - The [session] in which to execute the query.
 * - A [query] with the command to execute
 * and it's arguments. Note that first two arguments
 * should be a [BlockChainId] and a type id [String]
 * used to register that class of data as per
 * [Storable].
 *
 * Returns an optional result as [T]?
 */
private fun <T> getUniqueResult(
    session: ODatabaseDocument,
    query: GenericQuery,
    loader: Loadable<T>
): T? =
    try {
        val rs = session.query(
            query.query,
            query.params
        )
        if (rs.hasNext()) {
            (loader)(rs.next().toElement())
        } else {
            null
        }
    } catch (e: Exception) {
        logger.warn { e.message }
        null
    }


/**
 * Not to be used directly.
 * Requires knowledge of inner workings of DB.
 *
 * Requires:
 * - The [session] in which to execute the query.
 * - A [query] with the command to execute
 * and it's arguments. Note that first two arguments
 * should be a [BlockChainId] and a type id [String]
 * used to register that class of data as per
 * [Storable].
 *
 * Returns an optional result as a potentially empty
 * [List] of [T]
 */
private fun <T> getResults(
    session: ODatabaseDocument,
    query: GenericQuery,
    loader: Loadable<T>
): List<T> =
    try {
        val ls = mutableListOf<T>()
        session.query(
            query.query,
            query.params
        ).forEach { r ->
            r.element.map {
                (loader)(it)
            }.ifPresent {
                ls.add(it)
            }
        }
        ls
    } catch (e: Exception) {
        logger.warn(e.message)
        emptyList()
    }


/**
 * Requires an active [session], the [element] to
 * persist and the [logger] to write errors.
 *
 * Returns whether the element was successfully saved.
 */
private fun persistEntity(
    session: ODatabaseDocument,
    element: OElement
): Boolean =
    try {
        val r = session.save<OElement>(element)
        r == element
    } catch (e: Exception) {
        logger.error { e.message }
        false
    }


/**
 * Should be called when querying is finished to reset the persistence context.
 */
internal fun clearTransactionsContext() =
    PersistenceWrapper.closeCurrentSession()

private fun <R> queryUniqueResult(
    id: String,
    query: GenericQuery
): R? =
    PersistenceWrapper.executeInSessionAndReturn {
        getUniqueResult(
            it,
            query,
            LoaderManager.getFromDefault<R>(id)
        )
    }

private fun <R> queryResults(
    id: String,
    query: GenericQuery
): List<R>? =
    PersistenceWrapper.executeInSessionAndReturn {
        getResults(
            it,
            query,
            LoaderManager.getFromDefault<R>(id)
        )
    }


internal fun persistEntity(
    element: OElement
): Boolean =
    PersistenceWrapper
        .executeWithSuccessInCurrentSession {
            persistEntity(
                session,
                element
            )
        }


// ------------------------------
// Blockchain transactions.
//
// ------------------------------

/**
 *
 */
internal fun getBlockChain(
    blockChainId: BlockChainId
): BlockChain? =
    queryUniqueResult(
        "BlockChain",
        GenericQuery(
            "",
            mapOf(
                "" to ""
            )
        )
    )

internal fun getBlockChain(
    hash: Hash
): BlockChain? =
    queryUniqueResult(
        "BlockChain",
        GenericQuery(
            "",
            mapOf(
                "" to ""
            )
        )
    )

// ------------------------------
// Blockheader transactions.
//
// ------------------------------
internal fun getBlockHeaderByHash(
    hash: Hash
): BlockHeader? =
    queryUniqueResult(
        "BlockHeader",
        GenericQuery(
            "",
            mapOf(
                "" to ""
            )
        )
    )


internal fun getBlockHeaderByBlockHeight(
    height: Long
): BlockHeader? =
    queryUniqueResult(
        "BlockHeader",
        GenericQuery(
            "",
            mapOf(
                "" to ""
            )
        )
    )


internal fun getBlockHeaderByPrevHeaderHash(
    hash: Hash
): BlockHeader? =
    queryUniqueResult(
        "BlockHeader",
        GenericQuery(
            "",
            mapOf(
                "" to ""
            )
        )
    )

internal fun getLatestBlockHeader(): BlockHeader? =
    queryUniqueResult(
        "BlockHeader",
        GenericQuery(
            "",
            mapOf(
                "" to ""
            )
        )
    )

// ------------------------------
// Block transactions.
//
// ------------------------------
internal fun getBlockByBlockHeight(
    blockheight: Long
): Block? =
    queryUniqueResult(
        "Block",
        GenericQuery(
            "",
            mapOf(
                "" to ""
            )
        )
    )


internal fun getBlockByHeaderHash(
    hash: Hash
): Block? =
    queryUniqueResult(
        "Block",
        GenericQuery(
            "",
            mapOf(
                "" to ""
            )
        )
    )

internal fun getLatestBlock(): Block? =
    queryUniqueResult(
        "Block",
        GenericQuery(
            "",
            mapOf(
                "" to ""
            )
        )
    )

internal fun getBlockByPrevHeaderHash(
    hash: Hash
): Block? =
    queryUniqueResult(
        "Block",
        GenericQuery(
            "",
            mapOf(
                "" to ""
            )
        )
    )

// ------------------------------
// Ident transaction.
//
// ------------------------------
internal val IDENT: OElement?
    get() = PersistenceWrapper
        .executeInSessionAndReturn {
            val rs = it.query(
                "",
                ""
            )
            if (rs.hasNext()) {
                rs.next().toElement()
            } else {
                null
            }
        }


// ------------------------------
// Transactions over transactions.
// Go figure.
//
// Execution must be runtime determined.
// ------------------------------
fun getTransactionsFromAgent(
    publicKey: PublicKey
): List<Transaction>? =
    queryUniqueResult(
        "Transaction",
        GenericQuery(
            "",
            mapOf(
                "" to ""
            )
        )
    )

fun getTransactionByHash(
    hash: String
): Transaction? =
    queryUniqueResult(
        "Transaction",
        GenericQuery(
            "",
            mapOf(
                "" to ""
            )
        )
    )


//Execution must be runtime determined.
fun getTransactionsOrderedByTimestamp(
): List<Transaction>? =
    queryUniqueResult(
        "Transaction",
        GenericQuery(
            "",
            mapOf(
                "" to ""
            )
        )
    )
