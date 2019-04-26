package pt.um.lei.masb.blockchain.persistance.loaders

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.persistance.results.DataResult

inline class Loadable<T : BlockChainData>(
    val load: (OElement) -> DataResult<T>
)


