package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.persistance.results.DataResult

class Loadable<T : BlockChainData>(
    val load: (OElement) -> DataResult<T>
)


