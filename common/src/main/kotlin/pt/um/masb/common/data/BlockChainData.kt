package pt.um.masb.common.data

import pt.um.masb.common.Hashable
import pt.um.masb.common.Sizeable
import pt.um.masb.common.storage.adapters.Storable
import java.io.Serializable

interface BlockChainData : SelfInterval,
                           DataCategory,
                           Sizeable,
                           Hashable,
                           Serializable,
                           Storable