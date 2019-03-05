package pt.um.lei.masb.blockchain.persistance.loaders

import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.data.Loadable

interface Loaders {
    val loaders: MutableMap<String, Loadable<out BlockChainData>>
}