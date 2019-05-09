package pt.um.masb.ledger.storage.loaders

import pt.um.masb.common.data.BlockChainData

interface Loaders {
    val loaders: MutableMap<String, Loadable<out BlockChainData>>
}