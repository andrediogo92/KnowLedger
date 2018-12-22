package pt.um.lei.masb.blockchain.persistance.schema

import com.orientechnologies.orient.core.metadata.schema.OType

internal object PreConfiguredSchemas {
    val schemas by lazy {
        listOf(
            PreConfiguredSchemaProvider(
                "Humidity",
                mapOf(
                    "hum" to OType.DECIMAL,
                    "unit" to OType.BYTE
                )
            ),
            PreConfiguredSchemaProvider(
                "Dummy",
                mapOf(
                    "origin" to OType.BYTE
                )
            ),
            PreConfiguredSchemaProvider(
                "Luminosity",
                mapOf(
                    "lum" to OType.DECIMAL,
                    "unit" to OType.BYTE
                )
            ),
            PreConfiguredSchemaProvider(
                "Noise",
                mapOf(
                    "noiseLevel" to OType.DECIMAL,
                    "peakOrBase" to OType.DECIMAL,
                    "unit" to OType.BYTE
                )
            ),
            PreConfiguredSchemaProvider(
                "Other",
                mapOf(
                    "data" to OType.LINK
                )
            ),
            PreConfiguredSchemaProvider(
                "Temperature",
                mapOf(
                    "temperature" to OType.DECIMAL,
                    "unit" to OType.BYTE
                )
            ),
            PreConfiguredSchemaProvider(
                "PhysicalData",
                mapOf(
                    "seconds" to OType.LONG,
                    "nanos" to OType.INTEGER,
                    "data" to OType.LINK
                )
            ),
            PreConfiguredSchemaProvider(
                "Block",
                mapOf(
                    "data" to OType.LINKLIST,
                    "coinbase" to OType.LINK,
                    "header" to OType.LINK,
                    "merkleTree" to OType.LINK
                )
            ),
            PreConfiguredSchemaProvider(
                "BlockChain",
                mapOf(
                    "blockChainId" to OType.LINK,
                    "categories" to OType.EMBEDDEDMAP,
                    "sidechains" to OType.LINKMAP
                )
            ),
            PreConfiguredSchemaProvider(
                "BlockChainId",
                mapOf(
                    "uuid" to OType.STRING,
                    "timestamp" to OType.STRING,
                    "id" to OType.STRING,
                    "hash" to OType.BINARY
                )
            ),
            PreConfiguredSchemaProvider(
                "BlockHeader",
                mapOf(
                    "blockChainId" to OType.BINARY,
                    "difficulty" to OType.BINARY,
                    "blockheight" to OType.LONG,
                    "hash" to OType.BINARY,
                    "merkleRoot" to OType.BINARY,
                    "previousHash" to OType.BINARY,
                    "seconds" to OType.LONG,
                    "nanos" to OType.INTEGER,
                    "nonce" to OType.LONG
                )
            ),
            PreConfiguredSchemaProvider(
                "CategoryTypes",
                mapOf(
                    "categoryTypes" to OType.EMBEDDEDLIST
                )
            ),
            PreConfiguredSchemaProvider(
                "Coinbase",
                mapOf(
                    "payoutTXOs" to OType.LINKSET,
                    "coinbase" to OType.DECIMAL,
                    "hashId" to OType.BINARY
                )
            ),
            PreConfiguredSchemaProvider(
                "Ident",
                mapOf(
                    "privateKey" to OType.BINARY,
                    "publicKey" to OType.BINARY
                )
            ),
            PreConfiguredSchemaProvider(
                "MerkleTree",
                mapOf(
                    "collapsedTree" to OType.LINKLIST,
                    "levelIndex" to OType.EMBEDDEDLIST
                )
            ),
            PreConfiguredSchemaProvider(
                "SideChain",
                mapOf(
                    "clazz" to OType.STRING,
                    "hash" to OType.BINARY,
                    "difficultyTarget" to OType.BINARY,
                    "lastRecalc" to OType.INTEGER,
                    "currentBlockheight" to OType.LONG
                )
            ),
            PreConfiguredSchemaProvider(
                "Transaction",
                mapOf(
                    "publicKey" to OType.BINARY,
                    "data" to OType.LINK,
                    "signature" to OType.LINK,
                    "hashId" to OType.BINARY
                )
            ),
            PreConfiguredSchemaProvider(
                "TransactionOutput",
                mapOf(
                    "publicKey" to OType.BINARY,
                    "prevCoinbase" to OType.BINARY,
                    "hashId" to OType.BINARY,
                    "payout" to OType.DECIMAL,
                    "txSet" to OType.LINKSET
                )
            )
        )
    }
}
