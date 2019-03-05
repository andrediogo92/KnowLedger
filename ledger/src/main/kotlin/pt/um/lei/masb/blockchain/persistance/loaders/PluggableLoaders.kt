package pt.um.lei.masb.blockchain.persistance.loaders

data class PluggableLoaders(
    override val loaders: DataLoader
) : Loaders