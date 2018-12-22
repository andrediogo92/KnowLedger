package pt.um.lei.masb.blockchain

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.data.Loadable
import pt.um.lei.masb.blockchain.persistance.PersistenceWrapper

inline class DefaultLoadable<T : BlockChainContract>(
    val load: (Hash, OElement) -> T
)

inline class ChainLoadable<T : BlockChainContract>(
    val load: (PersistenceWrapper, OElement) -> T
)

typealias DataLoader = MutableMap<String, Loadable<out BlockChainData>>

typealias BlockChainLoader = MutableMap<String, DefaultLoadable<out BlockChainContract>>

typealias ChainLoader = MutableMap<String, ChainLoadable<out BlockChainContract>>