package pt.um.lei.masb.blockchain.persistance.loaders

import pt.um.lei.masb.blockchain.data.BlockChainData

interface Loaders {
    val loaders: MutableMap<String, Loadable<out BlockChainData>>
}