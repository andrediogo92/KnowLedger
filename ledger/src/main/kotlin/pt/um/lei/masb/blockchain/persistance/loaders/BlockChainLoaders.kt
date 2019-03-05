package pt.um.lei.masb.blockchain.persistance.loaders

import com.orientechnologies.orient.core.record.OElement
import com.orientechnologies.orient.core.record.impl.OBlob
import pt.um.lei.masb.blockchain.data.MerkleTree
import pt.um.lei.masb.blockchain.data.PhysicalData
import pt.um.lei.masb.blockchain.ledger.*
import pt.um.lei.masb.blockchain.persistance.PersistenceWrapper
import pt.um.lei.masb.blockchain.service.ChainHandle
import pt.um.lei.masb.blockchain.service.LedgerHandle
import pt.um.lei.masb.blockchain.service.ServiceHandle
import pt.um.lei.masb.blockchain.service.results.DEADCODE
import pt.um.lei.masb.blockchain.service.results.DataResult
import pt.um.lei.masb.blockchain.service.results.LedgerResult
import pt.um.lei.masb.blockchain.service.results.ListResult
import pt.um.lei.masb.blockchain.service.results.collapse
import pt.um.lei.masb.blockchain.service.results.intoData
import pt.um.lei.masb.blockchain.service.results.intoLedger
import pt.um.lei.masb.blockchain.utils.GeoCoords
import pt.um.lei.masb.blockchain.utils.base64encode
import pt.um.lei.masb.blockchain.utils.byteEncodeToPublicKey
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant

internal object BlockChainLoaders {
    val chains: ChainLoader = mutableMapOf(

        "LedgerHandle" to ChainLoadable
        { crypterHash: Hash,
          pw: PersistenceWrapper,
          elem: OElement ->
            val lidLoader = LoaderManager.idLoader
            val ledgerId = lidLoader(
                crypterHash,
                elem
            )
            ledgerId.intoLedger {
                LedgerHandle(
                    pw, this
                )
            }
        },


        "ChainHandle" to ChainLoadable
        { crypterHash: Hash,
          pw: PersistenceWrapper,
          elem: OElement ->
            tryOrLedgerQueryFailure {
                val clazz = elem.getProperty<String>(
                    "clazz"
                )
                val hash = Hash(
                    elem.getProperty<ByteArray>(
                        "hash"
                    )
                )
                val difficulty = BigInteger(
                    elem.getProperty<ByteArray>(
                        "difficultyTarget"
                    )
                )
                val lastRecalc = elem.getProperty<Long>(
                    "lastRecalc"
                )
                val currentBlockheight = elem.getProperty<Long>(
                    "currentBlockheight"
                )
                val params = elem.getProperty<OElement>(
                    "params"
                )
                val storedCrypterHash = Hash(params.getProperty<ByteArray>("crypter"))
                if (storedCrypterHash.hash.contentEquals(crypterHash.hash)) {
                    val crypter = LoaderManager.crypters[base64encode(storedCrypterHash.hash)]
                    if (crypter != null) {
                        val ledgerParams = LoaderManager.paramLoader(
                            crypter,
                            hash,
                            params
                        )
                        ledgerParams.intoLedger {
                            ChainHandle(
                                pw,
                                this,
                                clazz,
                                hash,
                                difficulty,
                                lastRecalc,
                                currentBlockheight
                            )
                        }
                    } else {
                        LedgerResult.UnregisteredCrypter(
                            "Load chain with crypterHash: ${storedCrypterHash.print()}"
                        )
                    }
                } else {
                    LedgerResult.NonMatchingCrypter(
                        """Non matching crypter at load chain:
                            | with crypterHash: ${crypterHash.print()}
                            | with storedHash: ${storedCrypterHash.print()}
                        """.trimMargin()
                    )
                }
            }
        }
    )

    val defaults: BlockChainLoader = mutableMapOf(


        "MerkleTree" to DefaultLoadable
        { ledgerId: Hash,
          document: OElement ->
            tryOrDataQueryFailure {
                val collapsedTree = document
                    .getProperty<List<Hash>>(
                        "collapsedTree"
                    )
                val levelIndex = document
                    .getProperty<List<Int>>(
                        "levelIndex"
                    )
                DataResult.Success(
                    MerkleTree(
                        collapsedTree,
                        levelIndex
                    )
                )
            }
        },


        "PhysicalData" to DefaultLoadable
        { blockChainId: Hash,
          document: OElement ->
            tryOrDataQueryFailure {
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
                        DataResult.Success(
                            PhysicalData(
                                instant,
                                GeoCoords(
                                    document.getProperty("latitude"),
                                    document.getProperty("longitude"),
                                    document.getProperty("altitude")
                                ),
                                data
                            )
                        )
                    } else {
                        DataResult.Success(
                            PhysicalData(
                                instant,
                                data
                            )
                        )
                    }
                } else {
                    DataResult.UnrecognizedDataType<PhysicalData>(
                        "Data property was unrecognized in physical data loader: $dataElem"
                    )
                }
            }
        },


        "Block" to DefaultLoadable
        { blockChainId: Hash,
          document: OElement ->
            tryOrDataQueryFailure {
                val data: MutableList<OElement> =
                    document.getProperty(
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
                }.collapse()
                if (listT !is ListResult.Success) {
                    return@tryOrDataQueryFailure listT.intoData<Transaction, Block>()
                }
                val coinbase = loaderC.load(
                    blockChainId,
                    document.getProperty<OElement>(
                        "coinbase"
                    )
                )
                if (coinbase !is DataResult.Success) {
                    return@tryOrDataQueryFailure coinbase.intoData<Coinbase, Block> {
                        DEADCODE()
                    }
                }
                val header = loaderH.load(
                    blockChainId,
                    document.getProperty<OElement>(
                        "header"
                    )
                )
                if (header !is DataResult.Success) {
                    return@tryOrDataQueryFailure header.intoData<BlockHeader, Block> {
                        DEADCODE()
                    }
                }
                val merkleTree = loaderM.load(
                    blockChainId,
                    document.getProperty<OElement>(
                        "merkleTree"
                    )
                )
                if (merkleTree !is DataResult.Success) {
                    return@tryOrDataQueryFailure merkleTree.intoData<MerkleTree, Block> {
                        DEADCODE()
                    }
                }
                DataResult.Success(
                    Block(
                        listT.data.toMutableList(),
                        coinbase.data,
                        header.data,
                        merkleTree.data
                    )
                )
            }

        },


        "BlockHeader" to DefaultLoadable
        { blockChainId: Hash,
          document: OElement ->
            val blid =
                document.getProperty<ByteArray>(
                    "ledgerId"
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

    inline fun <T : ServiceHandle> tryOrLedgerQueryFailure(
        run: () -> LedgerResult<T>
    ): LedgerResult<T> =
        try {
            run()
        } catch (e: Exception) {
            LedgerResult.QueryFailure(
                e.message ?: "", e
            )
        }

    inline fun <T : LedgerContract> tryOrDataQueryFailure(
        run: () -> DataResult<T>
    ): DataResult<T> =
        try {
            run()
        } catch (e: Exception) {
            DataResult.QueryFailure(
                e.message ?: "", e
            )
        }

}