package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import kotlinx.serialization.Serializable

@Serializable
class Loadable<T : BlockChainData>(
    val load: (OElement) -> T
)


