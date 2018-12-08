package pt.um.lei.masb.blockchain.persistance

import com.orientechnologies.orient.core.db.ODatabaseType
import com.orientechnologies.orient.core.db.OrientDB
import com.orientechnologies.orient.core.db.OrientDBConfig
import com.orientechnologies.orient.core.db.document.ODatabaseDocument
import com.orientechnologies.orient.core.metadata.schema.OSchema
import com.orientechnologies.orient.core.metadata.schema.OType


/**
 * A Thread-safe wrapper into the DB context of the blockchain.
 */
object PersistenceWrapper {

    val db = OrientDB("embedded:./db", OrientDBConfig.defaultConfig())

    var session: ODatabaseDocument =
        if (db.exists("blockchain")) {
            db.open(
                "blockchain",
                "admin",
                "admin"
            )
        } else {
            db.create(
                "blockchain",
                ODatabaseType.PLOCAL
            )
            db.open(
                "blockchain",
                "admin",
                "admin"
            )
        }

    init {
        registerDefaultSchemas(session.metadata.schema)
    }

    private fun createSchema(
        schema: OSchema,
        provider: SchemaProvider
    ) {
        if (!schema.existsClass(provider.id)) {
            val cl = schema.createClass(provider.id)
            provider.properties.forEach {
                cl.createProperty(it.key, it.value)
            }
        } else {
            val cl = schema.getClass(provider.id)
            val (propsIn, propsNotIn) = cl
                .declaredProperties()
                .partition {
                    it.name !in provider.properties.keys
                }
            propsNotIn.forEach {
                cl.dropProperty(it.name)
            }
            val intersect = propsIn
                .map { it.name }
                .intersect(provider.properties.keys)
            val toAdd = provider.properties.keys.filter {
                it !in intersect
            }
            toAdd.forEach {
                cl.createProperty(
                    it,
                    provider.properties[it]
                )
            }
        }
    }

    private fun registerDefaultSchemas(
        schema: OSchema
    ) {
        val schemaProviders = listOf(
            SchemaProvider(
                "Humidity",
                mapOf(
                    "hum" to OType.DECIMAL,
                    "unit" to OType.BYTE
                )
            ),
            SchemaProvider(
                "Dummy",
                mapOf(
                    "origin" to OType.BYTE
                )
            ),
            SchemaProvider(
                "Luminosity",
                mapOf(
                    "lum" to OType.DECIMAL,
                    "unit" to OType.BYTE
                )
            ),
            SchemaProvider(
                "Noise",
                mapOf(
                    "noiseLevel" to OType.DECIMAL,
                    "peakOrBase" to OType.DECIMAL,
                    "unit" to OType.BYTE
                )
            ),
            SchemaProvider(
                "Other",
                mapOf(
                    "data" to OType.LINK
                )
            ),
            SchemaProvider(
                "Temperature",
                mapOf(
                    "temperature" to OType.DECIMAL,
                    "unit" to OType.BYTE
                )
            ),
            SchemaProvider(
                "PhysicalData",
                mapOf(
                    "instant" to OType.DECIMAL,
                    "data" to OType.LINK
                )
            ),
            SchemaProvider(
                "BlockChainId",
                mapOf(
                    "uuid" to OType.STRING,
                    "timestamp" to OType.STRING,
                    "id" to OType.STRING
                )
            ),
            SchemaProvider(
                "Block",
                mapOf(
                    "data" to OType.LINKLIST,
                    "coinbase" to OType.LINK,
                    "header" to OType.LINK,
                    "merkleTree" to OType.LINK
                )
            ),
            SchemaProvider(
                "BlockHeader",
                mapOf(
                    "blockChainId" to OType.LINK,
                    "difficulty" to OType.STRING,
                    "header" to OType.LINK,
                    "blockheight" to OType.LONG,
                    "hash" to OType.STRING,
                    "merkleRoot" to OType.STRING,
                    "previousHash" to OType.STRING,
                    "timestamp" to OType.STRING,
                    "nonce" to OType.LONG
                )
            ),
            SchemaProvider(
                "Coinbase",
                mapOf(
                    "payoutTXOs" to OType.LINKSET,
                    "payout" to OType.DECIMAL,
                    "hashId" to OType.STRING
                )
            ),
            SchemaProvider(
                "Ident",
                mapOf(
                    "privateKey" to OType.STRING,
                    "publicKey" to OType.STRING
                )
            ),
            SchemaProvider(
                "Transaction",
                mapOf(
                    "publicKey" to OType.STRING,
                    "data" to OType.LINK,
                    "signature" to OType.LINK
                )
            ),
            SchemaProvider(
                "TransactionOutput",
                mapOf(
                    "publicKey" to OType.STRING,
                    "prevCoinbase" to OType.STRING,
                    "hashId" to OType.STRING,
                    "txSet" to OType.LINKSET
                )
            )
        )
        schemaProviders.forEach {
            createSchema(
                schema,
                it
            )
        }
    }


    private fun reOpenIfNecessary() {
        if (session.isClosed) {
            session = db.open(
                "blockchain",
                "admin",
                "admin"
            )
        }
    }


    @Synchronized
    internal fun executeWithSuccessInCurrentSession(
        executable: (
            ODatabaseDocument
        ) -> Boolean
    ): Boolean =
        let {
            reOpenIfNecessary()
            executable(session)
        }


    @Synchronized
    internal fun executeInCurrentSession(
        executable: (
            ODatabaseDocument
        ) -> Unit
    ): PersistenceWrapper =
        apply {
            reOpenIfNecessary()
            executable(session)
        }


    @Synchronized
    internal fun <R> executeInSessionAndReturn(
        function: (
            ODatabaseDocument
        ) -> R
    ): R =
        let {
            reOpenIfNecessary()
            function(session)
        }


    @Synchronized
    internal fun closeCurrentSession(): PersistenceWrapper =
        apply {
            session.close()
        }

}
