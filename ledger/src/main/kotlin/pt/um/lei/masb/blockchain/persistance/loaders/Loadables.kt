package pt.um.lei.masb.blockchain.persistance.loaders

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.data.Loadable
import pt.um.lei.masb.blockchain.ledger.Hash
import pt.um.lei.masb.blockchain.ledger.LedgerContract
import pt.um.lei.masb.blockchain.persistance.PersistenceWrapper
import pt.um.lei.masb.blockchain.service.ServiceHandle
import pt.um.lei.masb.blockchain.service.results.LedgerResult
import pt.um.lei.masb.blockchain.service.results.LoadResult

internal inline class DefaultLoadable<T : LedgerContract>(
    val load: (Hash, OElement) -> LoadResult<T>
)

internal inline class ChainLoadable<T : ServiceHandle>(
    val load: (Hash, PersistenceWrapper, OElement) -> LedgerResult<T>
)

typealias DataLoader = MutableMap<String, Loadable<out BlockChainData>>