package pt.um.lei.masb.blockchain.persistance

import kotlinx.serialization.Serializable
import pt.um.lei.masb.blockchain.data.Loadable

@Serializable
class Loader(
    internal val loaders: MutableMap<String, Loadable<*>>
)