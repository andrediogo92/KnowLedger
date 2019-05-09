package pt.um.masb.ledger.storage.loaders

import com.orientechnologies.orient.core.record.OElement
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.storage.results.DataResult

inline class Loadable<T : BlockChainData>(
    val load: (OElement) -> DataResult<T>
)


