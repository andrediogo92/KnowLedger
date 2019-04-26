package pt.um.lei.masb.blockchain.data

import pt.um.lei.masb.blockchain.ledger.Hashable
import pt.um.lei.masb.blockchain.ledger.Sizeable
import pt.um.lei.masb.blockchain.persistance.Storable
import java.io.Serializable

interface BlockChainData : SelfInterval,
                           DataCategory,
                           Sizeable,
                           Hashable,
                           Serializable,
                           Storable