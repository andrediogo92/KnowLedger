package pt.um.lei.masb.test.utils

import assertk.fail
import mu.KLogger
import pt.um.lei.masb.blockchain.data.PhysicalData
import pt.um.lei.masb.blockchain.data.TUnit
import pt.um.lei.masb.blockchain.data.TemperatureData
import pt.um.lei.masb.blockchain.ledger.Coinbase
import pt.um.lei.masb.blockchain.ledger.LedgerContract
import pt.um.lei.masb.blockchain.ledger.Transaction
import pt.um.lei.masb.blockchain.ledger.TransactionOutput
import pt.um.lei.masb.blockchain.ledger.emptyHash
import pt.um.lei.masb.blockchain.service.Ident
import pt.um.lei.masb.blockchain.service.ServiceHandle
import pt.um.lei.masb.blockchain.service.results.LedgerResult
import pt.um.lei.masb.blockchain.service.results.LoadListResult
import pt.um.lei.masb.blockchain.service.results.LoadResult
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.SHA256Encrypter
import java.math.BigDecimal
import java.security.SecureRandom
import java.security.Security

internal val r = SecureRandom.getInstanceStrong()

internal val crypter: Crypter =
    if (Security.getProvider("BC") == null) {
        Security.addProvider(
            org.bouncycastle.jce.provider.BouncyCastleProvider()
        )
        SHA256Encrypter()
    } else {
        SHA256Encrypter()
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
                            r.nextDouble() * 100
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
                            r.nextDouble() * 100
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
) {
    when (this) {
        is LoadListResult.Success -> this.data.block()
        is LoadListResult.QueryFailure ->
            if (exception != null)
                org.junit.jupiter.api.fail(cause, exception)
            else
                fail(cause)
        is LoadListResult.NonExistentData -> fail(cause)
        is LoadListResult.NonMatchingCrypter -> fail(cause)
        is LoadListResult.UnregisteredCrypter -> fail(cause)
        is LoadListResult.UnrecognizedDataType -> fail(cause)
    }
}

internal inline fun <T : LedgerContract> LoadResult<T>.applyOrFail(
    block: T.() -> Unit
) {
    when (this) {
        is LoadResult.Success -> this.data.block()
        is LoadResult.QueryFailure ->
            if (exception != null)
                org.junit.jupiter.api.fail(cause, exception)
            else
                fail(cause)
        is LoadResult.NonExistentData -> fail(cause)
        is LoadResult.NonMatchingCrypter -> fail(cause)
        is LoadResult.UnregisteredCrypter -> fail(cause)
        is LoadResult.UnrecognizedDataType -> fail(cause)
    }
}

internal inline fun <T : ServiceHandle> LedgerResult<T>.applyOrFail(
    block: T.() -> Unit
) {
    when (this) {
        is LedgerResult.Success -> this.data.block()
        is LedgerResult.QueryFailure ->
            if (exception != null)
                org.junit.jupiter.api.fail(cause, exception)
            else
                fail(cause)
        is LedgerResult.NonExistentData -> fail(cause)
        is LedgerResult.NonMatchingCrypter -> fail(cause)
        is LedgerResult.UnregisteredCrypter -> fail(cause)
    }
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
        is LedgerResult.UnregisteredCrypter -> fail(cause)
    }
