package pt.um.lei.masb.blockchain.persistance.loaders

import pt.um.lei.masb.blockchain.DataLoader

data class PluggableLoaders(
    override val loaders: DataLoader
) : Loaders