package pt.um.lei.masb.blockchain.persistance.loaders

import com.orientechnologies.orient.core.record.OElement
import com.orientechnologies.orient.core.record.impl.OBlob
import pt.um.lei.masb.blockchain.*
import pt.um.lei.masb.blockchain.data.MerkleTree
import pt.um.lei.masb.blockchain.data.PhysicalData
import pt.um.lei.masb.blockchain.persistance.PersistenceWrapper
import pt.um.lei.masb.blockchain.utils.GeoCoords
import pt.um.lei.masb.blockchain.utils.byteEncodeToPublicKey
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant

internal object BlockChainLoaders {
    val chains: ChainLoader = mutableMapOf(


        "BlockChain" to ChainLoadable
        { pw: PersistenceWrapper,
          elem: OElement ->
            val blidLoader = LoaderManager.idLoader
            val blockChainId = blidLoader(
                elem.getProperty<OElement>(
                    "blockChainId"
                )
            )
            val categoryLoader = LoaderManager.categoryLoader
            val categories = elem
                .getProperty<MutableMap<String, OElement>>(
                    "categories"
                ).mapValues {
                    categoryLoader(it.value)
                }
            val sideLoader =
                LoaderManager.getFromChains<SideChain>(
                    "SideChain"
                )
            val sidechains = elem
                .getProperty<Map<String, OElement>>(
                    "sidechains"
                ).mapValues {
                    (sideLoader.load)(pw, it.value)
                }
            BlockChain(
                pw,
                blockChainId,
                categories.toMutableMap(),
                sidechains.toMutableMap()
            )
        },


        "SideChain" to ChainLoadable
        { pw: PersistenceWrapper,
          elem: OElement ->
            val clazz = elem.getProperty<String>(
                "clazz"
            )
            val hash = elem.getProperty<ByteArray>(
                "hash"
            )
            val difficulty = BigInteger(
                elem.getProperty<ByteArray>(
                    "difficultyTarget"
                )
            )
            val lastRecalc = elem.getProperty<Int>(
                "lastRecalc"
            )
            val currentBlockheight = elem.getProperty<Long>(
                "currentBlockheight"
            )
            SideChain(
                pw,
                clazz,
                hash,
                difficulty,
                lastRecalc,
                currentBlockheight
            )
        }
    )

    val defaults: BlockChainLoader = mutableMapOf(


        "MerkleTree" to DefaultLoadable
        { blockChainId: Hash,
          document: OElement ->
            val collapsedTree = document
                .getProperty<List<Hash>>(
                    "collapsedTree"
                )
            val levelIndex = document
                .getProperty<List<Int>>(
                    "levelIndex"
                )
            MerkleTree(
                collapsedTree,
                levelIndex
            )
        },

        "CategoryTypes" to DefaultLoadable
        { _: Hash,
          document: OElement ->
            val types = document.getProperty<List<String>>(
                "categoryTypes"
            )
            CategoryTypes(types)
        },


        "PhysicalData" to DefaultLoadable
        { blockChainId: Hash,
          document: OElement ->
            val dataElem = document.getProperty<OElement>("data")
            val dataName = dataElem.schemaType?.get()?.name
            val loader = dataName?.let {
                LoaderManager.getFromLoaders(
                    blockChainId,
                    dataName
                )
            }
            if (dataName != null && loader != null) {
                val data = (loader.load)(dataElem)
                val instant = Instant.ofEpochSecond(
                    document.getProperty("seconds"),
                    document.getProperty("nanos")
                )
                if (document.propertyNames.contains("latitude")) {
                    PhysicalData(
                        instant,
                        GeoCoords(
                            document.getProperty("latitude"),
                            document.getProperty("longitude"),
                            document.getProperty("altitude")
                        ),
                        data
                    )
                } else {
                    PhysicalData(
                        instant,
                        data
                    )

                }
            } else {
                throw LoadFailedException(
                    "Data property was unrecognized in loaders: $dataElem"
                )
            }
        },


        "Block" to DefaultLoadable
        { blockChainId: Hash,
          document: OElement ->
            val data = document
                .getProperty<List<OElement>>(
                    "data"
                )
            val loaderT = LoaderManager.getFromDefault<Transaction>(
                "Transaction"
            )
            val loaderC = LoaderManager.getFromDefault<Coinbase>(
                "Coinbase"
            )
            val loaderH = LoaderManager.getFromDefault<BlockHeader>(
                "BlockHeader"
            )
            val loaderM = LoaderManager.getFromDefault<MerkleTree>(
                "MerkleTree"
            )
            val listT = data.map {
                (loaderT.load)(
                    blockChainId,
                    it
                )
            }.toMutableList()
            val coinbase = loaderC.load(
                blockChainId,
                document.getProperty<OElement>(
                    "coinbase"
                )
            )
            val header = loaderH.load(
                blockChainId,
                document.getProperty<OElement>(
                    "header"
                )
            )
            val merkleTree = loaderM.load(
                blockChainId,
                document.getProperty<OElement>(
                    "merkleTree"
                )
            )
            Block(
                listT,
                coinbase,
                header,
                merkleTree
            )

        },


        "BlockHeader" to DefaultLoadable
        { blockChainId: Hash,
          document: OElement ->
            val blid =
                document.getProperty<ByteArray>(
                    "blockChainId"
                )
            assert(blid!!.contentEquals(blockChainId))
            val difficulty =
                document.getProperty<ByteArray>(
                    "difficulty"
                )
            val blockheight = document.getProperty<Long>(
                "blockheight"
            )
            val hash =
                document.getProperty<ByteArray>(
                    "hash"
                )
            val merkleRoot =
                document.getProperty<ByteArray>(
                    "merkleRoot"
                )
            val previousHash =
                document.getProperty<ByteArray>(
                    "previousHash"
                )
            val seconds = document.getProperty<Long>(
                "seconds"
            )
            val nanos = document.getProperty<Int>(
                "nanos"
            )
            val nonce = document.getProperty<Long>(
                "nonce"
            )
            val instant = Instant.ofEpochSecond(
                seconds,
                nanos.toLong()
            )
            BlockHeader(
                blid,
                BigInteger(difficulty),
                blockheight,
                hash,
                merkleRoot,
                previousHash,
                instant,
                nonce
            )

        },


        "Coinbase" to DefaultLoadable
        { blockChainId: Hash,
          document: OElement ->
            val loaderTO =
                LoaderManager.getFromDefault<TransactionOutput>(
                    "TransactionOutput"
                )
            val pTXOs = document.getProperty<Set<OElement>>(
                "payoutTXOs"
            ).map {
                (loaderTO.load)(blockChainId, it)
            }.toMutableSet()
            val coinbase = document.getProperty<BigDecimal>(
                "coinbase"
            )
            val hashId = document.getProperty<ByteArray>(
                "hashId"
            )
            Coinbase(
                pTXOs,
                coinbase,
                hashId
            )
        },


        "Transaction" to DefaultLoadable
        { blockChainId: Hash,
          document: OElement ->
            val publicKey = byteEncodeToPublicKey(
                document.getProperty<ByteArray>(
                    "publicKey"
                )
            )
            val dataL = LoaderManager.getFromDefault<PhysicalData>(
                "PhysicalData"
            )
            val data = dataL.load(
                blockChainId,
                document.getProperty<OElement>(
                    "data"
                )
            )
            val signature = document.getProperty<OBlob>(
                "signature"
            ).toStream()
            Transaction(
                publicKey,
                data,
                signature
            )
        },


        "TransactionOutput" to DefaultLoadable
        { blockChainId: Hash,
          document: OElement ->
            val publicKey = byteEncodeToPublicKey(
                document.getProperty<ByteArray>(
                    "publicKey"
                )
            )
            val prevCoinbase = document.getProperty<ByteArray>(
                "prevCoinbase"
            )
            val hashId = document.getProperty<ByteArray>(
                "hashId"
            )
            val payout = document.getProperty<BigDecimal>(
                "payout"
            )
            val txSet =
                document.getProperty<MutableSet<ByteArray>>(
                    "txSet"
                )
            TransactionOutput(
                publicKey,
                prevCoinbase,
                hashId,
                payout,
                txSet
            )
        }

    )
}