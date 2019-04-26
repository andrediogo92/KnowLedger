package pt.um.lei.masb.test.utils

import assertk.fail
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import mu.KLogger
import org.apache.commons.rng.simple.RandomSource
import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.data.PhysicalData
import pt.um.lei.masb.blockchain.data.TUnit
import pt.um.lei.masb.blockchain.data.TemperatureData
import pt.um.lei.masb.blockchain.data.TrafficFlowData
import pt.um.lei.masb.blockchain.json.BigDecimalJsonAdapter
import pt.um.lei.masb.blockchain.json.BigIntegerJsonAdapter
import pt.um.lei.masb.blockchain.json.HashJsonAdapter
import pt.um.lei.masb.blockchain.json.InstantJsonAdapter
import pt.um.lei.masb.blockchain.json.PublicKeyJsonAdapter
import pt.um.lei.masb.blockchain.ledger.Coinbase
import pt.um.lei.masb.blockchain.ledger.LedgerContract
import pt.um.lei.masb.blockchain.ledger.Transaction
import pt.um.lei.masb.blockchain.ledger.TransactionOutput
import pt.um.lei.masb.blockchain.ledger.crypt.Crypter
import pt.um.lei.masb.blockchain.ledger.crypt.SHA256Encrypter
import pt.um.lei.masb.blockchain.ledger.emptyHash
import pt.um.lei.masb.blockchain.service.Ident
import pt.um.lei.masb.blockchain.service.ServiceHandle
import pt.um.lei.masb.blockchain.service.results.LedgerResult
import pt.um.lei.masb.blockchain.service.results.LoadListResult
import pt.um.lei.masb.blockchain.service.results.LoadResult
import java.math.BigDecimal
import java.security.Security

internal val r = RandomSource.create(RandomSource.SPLIT_MIX_64)

internal val moshi by lazy {
    Moshi
        .Builder()
        .add(HashJsonAdapter())
        .add(PublicKeyJsonAdapter())
        .add(InstantJsonAdapter())
        .add(BigDecimalJsonAdapter())
        .add(BigIntegerJsonAdapter())
        .add(
            PolymorphicJsonAdapterFactory
                .of(BlockChainData::class.java, "type")
                .withSubtype(TemperatureData::class.java, "Temperature")
                .withSubtype(TrafficFlowData::class.java, "TrafficFlowData")
        )
        .build()
}

internal val crypter: Crypter =
    if (Security.getProvider("BC") == null) {
        Security.addProvider(
            org.bouncycastle.jce.provider.BouncyCastleProvider()
        )
        SHA256Encrypter
    } else {
        SHA256Encrypter
    }

internal fun randomDouble(): Double =
    r.nextDouble()

internal fun randomInt(): Int =
    r.nextInt()

internal fun randomInt(bound: Int): Int =
    r.nextInt(bound)

internal fun randomBytesIntoArray(byteArray: ByteArray) {
    r.nextBytes(byteArray)
}

internal fun randomByteArray(size: Int): ByteArray =
    ByteArray(size).also {
        randomBytesIntoArray(it)
    }

internal fun makeXTransactions(
    id: Array<Ident>,
    size: Int
): List<Transaction> {
    val ts: MutableList<Transaction> = mutableListOf()
    for (i in 0 until size) {
        val index = i % id.size
        ts.add(
            Transaction(
                id[index].privateKey,
                id[index].publicKey,
                PhysicalData(
                    TemperatureData(
                        BigDecimal(
                            randomDouble() * 100
                        ),
                        TUnit.CELSIUS
                    )
                )
            )
        )
    }
    return ts
}

internal fun makeXTransactions(
    id: Ident,
    size: Int
): List<Transaction> {
    val ts: MutableList<Transaction> = mutableListOf()
    for (i in 0 until size) {
        ts.add(
            Transaction(
                id.privateKey,
                id.publicKey,
                PhysicalData(
                    TemperatureData(
                        BigDecimal(
                            randomDouble() * 100
                        ),
                        TUnit.CELSIUS
                    )
                )
            )
        )
    }
    return ts
}


internal fun generateCoinbase(
    id: Array<Ident>,
    ts: List<Transaction>
): Coinbase {
    val sets = listOf(
        TransactionOutput(
            id[0].publicKey,
            emptyHash(),
            BigDecimal.ONE,
            ts[0].hashId,
            emptyHash()
        ),
        TransactionOutput(
            id[1].publicKey,
            emptyHash(),
            BigDecimal.ONE,
            ts[1].hashId,
            emptyHash()
        )
    )
    //First transaction output has
    //transaction 0.
    //Second is transaction 2
    //referencing transaction 0.
    //Third is transaction 4
    //referencing transaction 0.
    sets[0].addToPayout(
        BigDecimal.ONE,
        ts[2].hashId,
        ts[0].hashId
    )
    sets[0].addToPayout(
        BigDecimal.ONE,
        ts[4].hashId,
        ts[0].hashId
    )
    return Coinbase(
        sets.toSet() as MutableSet<TransactionOutput>,
        BigDecimal("3"),
        emptyHash()
    )
}

internal fun logActualToExpectedLists(
    explanationActual: String,
    actualList: List<Any>,
    explanationExpected: String,
    expectedList: List<Any>,
    logger: KLogger
) {
    logger.info {
        """
            |
            |$explanationActual
            |${actualList.joinToString(
            """,

            """.trimIndent()
        ) { it.toString() }}
            |
            |$explanationExpected
            |${expectedList.joinToString(
            """,

            """.trimIndent()
        ) { it.toString() }}
        """.trimMargin()
    }
}

internal fun applyHashInPairs(
    crypter: Crypter,
    hashes: Array<ByteArray>
): ByteArray {
    var previousHashes = hashes
    var newHashes: Array<ByteArray>
    var levelIndex = hashes.size
    while (levelIndex > 2) {
        if (levelIndex % 2 == 0) {
            levelIndex /= 2
            newHashes = Array(levelIndex) {
                crypter.applyHash(
                    previousHashes[it * 2] + previousHashes[it * 2 + 1]
                )
            }
        } else {
            levelIndex /= 2
            levelIndex++
            newHashes = Array(levelIndex) {
                if (it != levelIndex - 1) {
                    crypter.applyHash(
                        previousHashes[it * 2] + previousHashes[it * 2 + 1]
                    )
                } else {
                    crypter.applyHash(
                        previousHashes[it * 2] + previousHashes[it * 2]
                    )
                }
            }
        }
        previousHashes = newHashes
    }
    return crypter.applyHash(previousHashes[0] + previousHashes[1])
}

internal inline fun <T : LedgerContract> LoadListResult<T>.applyOrFail(
    block: List<T>.() -> Unit
) =
    when (this) {
        is LoadListResult.Success -> this.data.block()
        is LoadListResult.QueryFailure ->
            if (exception != null)
                org.junit.jupiter.api.fail(cause, exception)
            else
                fail(cause)
        is LoadListResult.NonExistentData -> fail(cause)
        is LoadListResult.NonMatchingCrypter -> fail(cause)
        is LoadListResult.UnrecognizedDataType -> fail(cause)
        is LoadListResult.Propagated -> fail(cause)
    }


internal inline fun <T : LedgerContract> LoadResult<T>.applyOrFail(
    block: T.() -> Unit
) =
    when (this) {
        is LoadResult.Success -> this.data.block()
        is LoadResult.QueryFailure ->
            if (exception != null)
                org.junit.jupiter.api.fail(cause, exception)
            else
                fail(cause)
        is LoadResult.NonExistentData -> fail(cause)
        is LoadResult.NonMatchingCrypter -> fail(cause)
        is LoadResult.UnrecognizedDataType -> fail(cause)
        is LoadResult.Propagated -> fail(cause)
    }


internal inline fun <T : ServiceHandle> LedgerResult<T>.applyOrFail(
    block: T.() -> Unit
) =
    when (this) {
        is LedgerResult.Success -> this.data.block()
        is LedgerResult.QueryFailure ->
            if (exception != null)
                org.junit.jupiter.api.fail(cause, exception)
            else
                fail(cause)
        is LedgerResult.NonExistentData -> fail(cause)
        is LedgerResult.NonMatchingCrypter -> fail(cause)
        is LedgerResult.Propagated -> fail(cause)
    }


internal fun <T : ServiceHandle> LedgerResult<T>.extractOrFail(): T =
    when (this) {
        is LedgerResult.Success -> this.data
        is LedgerResult.QueryFailure ->
            if (exception != null)
                org.junit.jupiter.api.fail(cause, exception)
            else
                fail(cause)
        is LedgerResult.NonExistentData -> fail(cause)
        is LedgerResult.NonMatchingCrypter -> fail(cause)
        is LedgerResult.Propagated -> fail(cause)
    }

internal fun StringBuilder.appendByLine(toPrint: Collection<String>): StringBuilder {
    for (thing in toPrint) {
        append(System.lineSeparator())
        append('\t')
        append(thing)
        append(',')
    }
    return this
}