package pt.um.lei.masb.test.utils

import mu.KLogger
import pt.um.lei.masb.blockchain.Coinbase
import pt.um.lei.masb.blockchain.Transaction
import pt.um.lei.masb.blockchain.TransactionOutput
import pt.um.lei.masb.blockchain.data.PhysicalData
import pt.um.lei.masb.blockchain.data.TUnit
import pt.um.lei.masb.blockchain.data.TemperatureData
import pt.um.lei.masb.blockchain.emptyHash
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.SHA256Encrypter
import java.math.BigDecimal
import java.security.PrivateKey
import java.security.PublicKey
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
    id: Array<Pair<PrivateKey, PublicKey>>,
    size: Int
): List<Transaction> {
    val ts: MutableList<Transaction> = mutableListOf()
    for (i in 0 until size) {
        val index = i % id.size
        ts.add(
            Transaction(
                id[index].first,
                id[index].second,
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
    id: Pair<PrivateKey, PublicKey>,
    size: Int
): List<Transaction> {
    val ts: MutableList<Transaction> = mutableListOf()
    for (i in 0 until size) {
        ts.add(
            Transaction(
                id.first,
                id.second,
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
    id: Array<Pair<PrivateKey, PublicKey>>,
    ts: List<Transaction>
): Coinbase {
    val sets = listOf(
        TransactionOutput(
            id[0].second,
            emptyHash(),
            BigDecimal.ONE,
            ts[0].hashId,
            emptyHash()
        ),
        TransactionOutput(
            id[1].second,
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
