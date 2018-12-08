package pt.um.lei.masb.blockchain.data

import pt.um.lei.masb.blockchain.Sizeable
import java.io.Serializable

interface BlockChainData : SelfInterval,
                           DataCategory,
                           Sizeable,
                           Serializable,
                           Storable