package pt.um.masb.common.storage.adapters

import pt.um.masb.common.data.BlockChainData

/**
 * Main contract describing an object capable of, loading, storing
 * and deriving a schema for a given [BlockChainData].
 */
internal interface StorageAdapter<T : BlockChainData> : Loadable<T>,
                                                        Storable<BlockChainData>,
                                                        SchemaProvider<T>