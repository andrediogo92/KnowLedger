package pt.um.lei.masb.blockchain.data

import pt.um.lei.masb.blockchain.ledger.Sizeable
import pt.um.lei.masb.blockchain.persistance.Storable
import pt.um.lei.masb.blockchain.utils.Hashable
import java.io.Serializable

interface BlockChainData : SelfInterval,
                           DataCategory,
                           Sizeable,
                           Hashable,
                           Serializable,
                           Storable