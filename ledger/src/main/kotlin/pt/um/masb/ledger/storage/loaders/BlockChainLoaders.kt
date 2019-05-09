package pt.um.masb.ledger.storage.loaders

import com.orientechnologies.orient.core.record.OElement
import com.orientechnologies.orient.core.record.impl.OBlob
import pt.um.masb.common.Hash
import pt.um.masb.common.crypt.Crypter
import pt.um.masb.common.misc.base64encode
import pt.um.masb.common.misc.byteEncodeToPublicKey
import pt.um.masb.common.print
import pt.um.masb.common.storage.results.DataResult
import pt.um.masb.ledger.Block
import pt.um.masb.ledger.BlockHeader
import pt.um.masb.ledger.Coinbase
import pt.um.masb.ledger.Transaction
import pt.um.masb.ledger.TransactionOutput
import pt.um.masb.ledger.config.BlockParams
import pt.um.masb.ledger.config.LedgerId
import pt.um.masb.ledger.config.LedgerParams
import pt.um.masb.ledger.data.GeoCoords
import pt.um.masb.ledger.data.MerkleTree
import pt.um.masb.ledger.data.PhysicalData
import pt.um.masb.ledger.results.collapse
import pt.um.masb.ledger.results.intoLedger
import pt.um.masb.ledger.results.intoLoad
import pt.um.masb.ledger.results.tryOrLedgerQueryFailure
import pt.um.masb.ledger.results.tryOrLoadQueryFailure
import pt.um.masb.ledger.service.ChainHandle
import pt.um.masb.ledger.service.LedgerHandle
import pt.um.masb.ledger.service.LedgerService
import pt.um.masb.ledger.service.results.LedgerResult
import pt.um.masb.ledger.service.results.LoadListResult
import pt.um.masb.ledger.service.results.LoadResult
import pt.um.masb.ledger.storage.transactions.PersistenceWrapper
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.util.*

internal object BlockChainLoaders {
    internal val ledgerParamsLoader: (Crypter, Hash, OElement) -> LoadResult<LedgerParams> by lazy {
        { crypter: Crypter,
          hash: Hash,
          elem: OElement ->
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
                        return@tryOrLoadQueryFailure blockParams.intoLoad<LedgerParams>()
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
    }


    internal val ledgerLoader: ChainLoadable<LedgerHandle> by lazy {
        ChainLoadable<LedgerHandle>
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
    }


    internal val chainLoader: ChainLoadable<ChainHandle> by lazy {
        ChainLoadable<ChainHandle>
        { crypterHash: Hash,
          pw: PersistenceWrapper,
          elem: OElement ->
            tryOrLedgerQueryFailure {
                val clazz = elem.getProperty<String>(
                    "clazz"
                )
                val hash =
                    elem.getProperty<ByteArray>(
                        "hashId"
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
                val storedCrypterHash: ByteArray = params.getProperty("crypter")
                if (storedCrypterHash.contentEquals(crypterHash)) {
                    val crypter = LedgerService.crypters.getValue(base64encode(storedCrypterHash))
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
                    LedgerResult.NonMatchingCrypter(
                        """Non matching crypter at load chain:
                            | with crypterHash: ${crypterHash.print()}
                            | with storedHash: ${storedCrypterHash.print()}
                        """.trimMargin()
                    )
                }
            }
        }
    }


    internal val merkleLoader: DefaultLoadable<MerkleTree> by lazy {
        DefaultLoadable<MerkleTree>
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
    }


    internal val physicalDataLoader: DefaultLoadable<PhysicalData> by lazy {
        DefaultLoadable<PhysicalData>
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
                        return@tryOrLoadQueryFailure data.intoLoad<PhysicalData>()
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
    }


    internal val blockLoader: DefaultLoadable<Block> by lazy {
        DefaultLoadable<Block>
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
                    return@tryOrLoadQueryFailure listT.intoLoad<Block>()
                }
                val coinbase = coinbaseLoader.load(
                    ledgerId,
                    document.getProperty<OElement>(
                        "coinbase"
                    )
                )
                if (coinbase !is LoadResult.Success) {
                    return@tryOrLoadQueryFailure coinbase.intoLoad<Block>()
                }
                val header = blockHeaderLoader.load(
                    ledgerId,
                    document.getProperty<OElement>(
                        "header"
                    )
                )
                if (header !is LoadResult.Success) {
                    return@tryOrLoadQueryFailure header.intoLoad<Block>()
                }
                val merkleTree = merkleLoader.load(
                    ledgerId,
                    document.getProperty<OElement>(
                        "merkleTree"
                    )
                )
                if (merkleTree !is LoadResult.Success) {
                    return@tryOrLoadQueryFailure merkleTree.intoLoad<Block>()
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
    }


    internal val blockHeaderLoader: DefaultLoadable<BlockHeader> by lazy {
        DefaultLoadable<BlockHeader>
        { ledgerId: Hash,
          document: OElement ->
            tryOrLoadQueryFailure {
                val blid: ByteArray =
                    document.getProperty<ByteArray>(
                        "ledgerHash"
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
                        "hashId"
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
                    return@tryOrLoadQueryFailure params.intoLoad<BlockHeader>()
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
    }

    internal val blockParamsLoader: DefaultLoadable<BlockParams> by lazy {
        DefaultLoadable<BlockParams>
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
    }


    internal val coinbaseLoader: DefaultLoadable<Coinbase> by lazy {
        DefaultLoadable<Coinbase>
        { ledgerId, document ->
            tryOrLoadQueryFailure {
                val pTXOs = document.getProperty<Set<OElement>>(
                    "payoutTXOs"
                ).asSequence().map {
                    (transactionOutputLoader.load)(ledgerId, it)
                }.collapse()
                if (pTXOs !is LoadListResult.Success) {
                    return@tryOrLoadQueryFailure pTXOs.intoLoad<Coinbase>()
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

    }

    internal val idLoader: DefaultLoadable<LedgerId> by lazy {
        DefaultLoadable<LedgerId>
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
                val hash = elem.getProperty<ByteArray>("hashId")
                val crypter = LedgerService.crypters.getValue(base64encode(algo))
                val params = ledgerParamsLoader(
                    crypter,
                    hash,
                    elem.getProperty<OElement>("params")
                )
                params.intoLoad {
                    LedgerId(id, uuid, timestamp, this)
                }
            }
        }
    }


    internal val transactionLoader: DefaultLoadable<Transaction> by lazy {
        DefaultLoadable<Transaction>
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
                    return@tryOrLoadQueryFailure data.intoLoad<Transaction>()
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
    }


    internal val transactionOutputLoader: DefaultLoadable<TransactionOutput> by lazy {
        DefaultLoadable<TransactionOutput>
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
}