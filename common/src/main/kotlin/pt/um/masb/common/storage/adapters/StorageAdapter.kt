package pt.um.masb.common.storage.adapters

import pt.um.masb.common.data.BlockChainData

interface StorageAdapter<T : BlockChainData> : Loadable<T>, Storable