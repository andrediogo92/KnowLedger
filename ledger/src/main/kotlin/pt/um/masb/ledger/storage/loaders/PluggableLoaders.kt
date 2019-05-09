package pt.um.masb.ledger.storage.loaders

data class PluggableLoaders(
    override val loaders: DataLoader
) : Loaders