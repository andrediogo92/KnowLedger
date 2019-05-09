package pt.um.masb.ledger.storage.loaders

import com.orientechnologies.orient.core.record.OElement
import pt.um.masb.common.Hash
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.storage.results.QueryResult
import pt.um.masb.ledger.LedgerContract
import pt.um.masb.ledger.service.ServiceHandle
import pt.um.masb.ledger.service.results.LedgerResult
import pt.um.masb.ledger.service.results.LoadResult
import pt.um.masb.ledger.storage.transactions.PersistenceWrapper

internal inline class DefaultLoadable<T : LedgerContract>(
    val load: (Hash, OElement) -> LoadResult<T>
)

internal inline class ChainLoadable<T : ServiceHandle>(
    val load: (Hash, PersistenceWrapper, OElement) -> LedgerResult<T>
)

internal inline class QueryLoadable<T : Any>(
    val load: (OElement) -> QueryResult<T>
)

typealias DataLoader = MutableMap<String, Loadable<out BlockChainData>>