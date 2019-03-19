package pt.um.lei.masb.blockchain.persistance.loaders

import com.orientechnologies.orient.core.record.OElement
import com.orientechnologies.orient.core.record.impl.OBlob
import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.data.MerkleTree
import pt.um.lei.masb.blockchain.data.PhysicalData
import pt.um.lei.masb.blockchain.ledger.*
import pt.um.lei.masb.blockchain.persistance.PersistenceWrapper
import pt.um.lei.masb.blockchain.persistance.results.DataResult
import pt.um.lei.masb.blockchain.service.ChainHandle
import pt.um.lei.masb.blockchain.service.LedgerHandle
import pt.um.lei.masb.blockchain.service.LedgerService
import pt.um.lei.masb.blockchain.service.results.LedgerResult
import pt.um.lei.masb.blockchain.service.results.LoadListResult
import pt.um.lei.masb.blockchain.service.results.LoadResult
import pt.um.lei.masb.blockchain.utils.*
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.util.*

internal object BlockChainLoaders {
    internal val ledgerParamsLoader: (Crypter, Hash, OElement) -> LoadResult<LedgerParams> =
        { crypter, hash, elem ->
            tryOrLoadQueryFailure {
                val crypterHash = elem.getProperty<ByteArray>("crypter")
                if (crypter.id.contentEquals(crypterHash)) {
                    LoadResult.NonMatchingCrypter<LedgerParams>(
                        """Non matching crypter at load params:
                            | with crypterHash: ${crypter.id.print()}
                            | with storedHash: ${crypterHash.print()}
                        """.trimMargin()
                    )
                } else {
                    val recalcTime = elem.getProperty<Long>("recalcTime")
                    val recalcTrigger = elem.getProperty<Long>("recalcTrigger")
                    val blockParams = blockParamsLoader.load(
                        hash,
                        elem.getProperty<OElement>("blockParams")
                    )
                    if (blockParams !is LoadResult.Success) {
                        return@tryOrLoadQueryFailure blockParams.intoLoad<BlockParams, LedgerParams>()
                    }
                    LoadResult.Success(
                        LedgerParams(
                            crypter, recalcTime,
                            recalcTrigger,
                            blockParams.data
                        )
                    )
                }
            }
        }


    internal val ledgerLoader = ChainLoadable<LedgerHandle>
    { crypterHash: Hash,
      pw: PersistenceWrapper,
      elem: OElement ->
        val ledgerId = idLoader.load(
            crypterHash,
            elem
        )
        ledgerId.intoLedger {
            LedgerHandle(
                pw, this
            )
        }
    }


    internal val chainLoader = ChainLoadable<ChainHandle>
    { crypterHash: Hash,
      pw: PersistenceWrapper,
      elem: OElement ->
        tryOrLedgerQueryFailure {
            val clazz = elem.getProperty<String>(
                "clazz"
            )
            val hash =
                elem.getProperty<ByteArray>(
                    "hash"
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
            val storedCrypterHash = params.getProperty<ByteArray>("crypter")
            if (storedCrypterHash!!.contentEquals(crypterHash)) {
                val crypter = LedgerService.crypters[base64encode(storedCrypterHash)]
                if (crypter != null) {
                    val ledgerParams = ledgerParamsLoader(
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


    internal val merkleLoader = DefaultLoadable<MerkleTree>
    { _, document: OElement ->
        tryOrLoadQueryFailure {
            val collapsedTree = document
                .getProperty<List<Hash>>(
                    "collapsedTree"
                )
            val levelIndex = document
                .getProperty<List<Int>>(
                    "levelIndex"
                )
            LoadResult.Success(
                MerkleTree(
                    collapsedTree,
                    levelIndex
                )
            )
        }
    }


    internal val physicalDataLoader = DefaultLoadable<PhysicalData>
    { ledgerId: Hash,
      document: OElement ->
        tryOrLoadQueryFailure {
            val dataElem = document.getProperty<OElement>("data")
            val dataName = dataElem.schemaType?.get()?.name
            val loader = dataName?.let {
                LedgerService.getFromLoaders(
                    ledgerId,
                    dataName
                )
            }
            if (dataName != null && loader != null) {
                val data = (loader.load)(dataElem)
                if (data !is DataResult.Success) {
                    return@tryOrLoadQueryFailure data.intoLoad<BlockChainData, PhysicalData>()
                }
                val instant = Instant.ofEpochSecond(
                    document.getProperty("seconds"),
                    document.getProperty("nanos")
                )
                if (document.propertyNames.contains("latitude")) {
                    LoadResult.Success(
                        PhysicalData(
                            instant,
                            GeoCoords(
                                document.getProperty("latitude"),
                                document.getProperty("longitude"),
                                document.getProperty("altitude")
                            ),
                            data.data
                        )
                    )
                } else {
                    LoadResult.Success(
                        PhysicalData(
                            instant,
                            data.data
                        )
                    )
                }
            } else {
                LoadResult.UnrecognizedDataType<PhysicalData>(
                    "Data property was unrecognized in physical data loader: $dataElem"
                )
            }
        }
    }


    internal val blockLoader = DefaultLoadable<Block>
    { ledgerId: Hash,
      document: OElement ->
        tryOrLoadQueryFailure {
            val data: MutableList<OElement> =
                document.getProperty(
                    "data"
                )
            val listT = data.asSequence().map {
                (transactionLoader.load)(
                    ledgerId,
                    it
                )
            }.collapse()
            if (listT !is LoadListResult.Success) {
                return@tryOrLoadQueryFailure listT.intoLoad<Transaction, Block>()
            }
            val coinbase = coinbaseLoader.load(
                ledgerId,
                document.getProperty<OElement>(
                    "coinbase"
                )
            )
            if (coinbase !is LoadResult.Success) {
                return@tryOrLoadQueryFailure coinbase.intoLoad<Coinbase, Block>()
            }
            val header = blockHeaderLoader.load(
                ledgerId,
                document.getProperty<OElement>(
                    "header"
                )
            )
            if (header !is LoadResult.Success) {
                return@tryOrLoadQueryFailure header.intoLoad<BlockHeader, Block>()
            }
            val merkleTree = merkleLoader.load(
                ledgerId,
                document.getProperty<OElement>(
                    "merkleTree"
                )
            )
            if (merkleTree !is LoadResult.Success) {
                return@tryOrLoadQueryFailure merkleTree.intoLoad<MerkleTree, Block>()
            }
            LoadResult.Success(
                Block(
                    listT.data.toMutableList(),
                    coinbase.data,
                    header.data,
                    merkleTree.data
                )
            )
        }

    }


    internal val blockHeaderLoader = DefaultLoadable<BlockHeader>
    { ledgerId: Hash,
      document: OElement ->
        tryOrLoadQueryFailure {
            val blid =
                document.getProperty<ByteArray>(
                    "ledgerId"
                )
            assert(blid.contentEquals(ledgerId))
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
            val params = blockParamsLoader.load(
                ledgerId,
                document.getProperty<OElement>("params")
            )
            if (params !is LoadResult.Success) {
                return@tryOrLoadQueryFailure params.intoLoad<BlockParams, BlockHeader>()
            }
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
            LoadResult.Success(
                BlockHeader(
                    blid,
                    BigInteger(difficulty),
                    blockheight,
                    hash,
                    merkleRoot,
                    previousHash,
                    params.data,
                    instant,
                    nonce
                )
            )

        }

    }

    internal val blockParamsLoader = DefaultLoadable<BlockParams>
    { _, elem ->
        tryOrLoadQueryFailure {
            LoadResult.Success(
                BlockParams(
                    elem.getProperty<Long>("blockMemSize"),
                    elem.getProperty<Long>("blockLength")
                )
            )
        }
    }


    internal val coinbaseLoader = DefaultLoadable<Coinbase>
    { ledgerId: Hash,
      document: OElement ->
        tryOrLoadQueryFailure {
            val pTXOs = document.getProperty<Set<OElement>>(
                "payoutTXOs"
            ).asSequence().map {
                (transactionOutputLoader.load)(ledgerId, it)
            }.collapse()
            if (pTXOs !is LoadListResult.Success) {
                return@tryOrLoadQueryFailure pTXOs.intoLoad<TransactionOutput, Coinbase>()
            }
            val coinbase = document.getProperty<BigDecimal>(
                "coinbase"
            )
            val hashId =
                document.getProperty<ByteArray>(
                    "hashId"
                )

            LoadResult.Success(
                Coinbase(
                    pTXOs.data.toMutableSet(),
                    coinbase,
                    hashId
                )
            )
        }
    }

    internal val idLoader = DefaultLoadable<LedgerId>
    { algo, elem ->
        tryOrLoadQueryFailure {
            val uuid = UUID.fromString(
                elem.getProperty<String>("uuid")
            )
            val timestamp = Instant.ofEpochSecond(
                elem.getProperty<Long>("seconds"),
                elem.getProperty<Int>("nanos").toLong()
            )
            val id = elem.getProperty<String>("id")
            val hash = elem.getProperty<ByteArray>("hash")
            val crypter = LedgerService.crypters[base64encode(algo)]
            if (crypter != null) {
                val params = ledgerParamsLoader(
                    crypter,
                    hash,
                    elem.getProperty<OElement>("params")
                )
                params.intoLoad {
                    LedgerId(id, uuid, timestamp, this)
                }
            } else {
                LoadResult.UnregisteredCrypter(
                    "Unregistered Crypter at load ledgerId: ${algo.print()}"
                )
            }
        }
    }


    internal val transactionLoader = DefaultLoadable<Transaction>
    { ledgerId: Hash,
      document: OElement ->
        tryOrLoadQueryFailure {
            val publicKey = byteEncodeToPublicKey(
                document.getProperty<ByteArray>(
                    "publicKey"
                )
            )
            val data = physicalDataLoader.load(
                ledgerId,
                document.getProperty<OElement>(
                    "data"
                )
            )
            if (data !is LoadResult.Success) {
                return@tryOrLoadQueryFailure data.intoLoad<PhysicalData, Transaction>()
            }
            val signature = document.getProperty<OBlob>(
                "signature"
            ).toStream()
            LoadResult.Success(
                Transaction(
                    publicKey,
                    data.data,
                    signature
                )
            )
        }
    }


    internal val transactionOutputLoader = DefaultLoadable<TransactionOutput>
    { _: Hash,
      document: OElement ->
        tryOrLoadQueryFailure {
            val publicKey = byteEncodeToPublicKey(
                document.getProperty<ByteArray>(
                    "publicKey"
                )
            )
            val prevCoinbase =
                document.getProperty<ByteArray>(
                    "prevCoinbase"
                )

            val hashId =
                document.getProperty<ByteArray>(
                    "hashId"
                )

            val payout = document.getProperty<BigDecimal>(
                "payout"
            )
            val txSet =
                document.getProperty<MutableSet<ByteArray>>(
                    "txSet"
                )
            LoadResult.Success(
                TransactionOutput(
                    publicKey,
                    prevCoinbase,
                    hashId,
                    payout,
                    txSet
                )
            )
        }
    }
}