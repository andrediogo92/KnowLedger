package pt.um.lei.masb.blockchain

import mu.KLogging
import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.data.DataFormula
import pt.um.lei.masb.blockchain.data.PhysicalData
import pt.um.lei.masb.blockchain.data.calculateDiff
import pt.um.lei.masb.blockchain.utils.DEFAULT_CRYPTER
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.security.PublicKey
import java.time.temporal.ChronoField

/**
 * The coinbase transaction. Pays out to contributors to the blockchain.
 * <pw>
 * The coinbase will be continually updated
 * to reflect changes to the block.
 */
class Coinbase<T : BlockChainData<T>>(
        val blockChainId: BlockChainId,
        val payoutTXO: MutableSet<TransactionOutput> = mutableSetOf(),
        private val payoutFormula: DataFormula = ::calculateDiff,
        private var coinbase: BigDecimal = BigDecimal.ZERO,
        private var _hashId: String = ""
) : Sizeable, Hashed {

    companion object : KLogging() {
        const val TIME_BASE = 5
        const val VALUE_BASE = 2
        const val BASE = 3
        const val THRESHOLD = 100000
        const val OTHER = 50
        const val DATA = 5
        val MATH_CONTEXT = MathContext(8, RoundingMode.HALF_EVEN)
    }

    init {
        if (_hashId == "") _hashId = recalculateHash()
    }


    /**
     * {@inheritDoc}
     */
    override val hashId: String
        get() = _hashId

    val coinbasePayout: BigDecimal
        get() = coinbase


    /**
     * Takes the new Transaction and attempts to calculate a fluctuation from
     * the previous Transaction of same type and in the same geographical area.
     *
     * Adds a payout for the transaction's agent in a transaction output.
     * @param newT                  Transaction to contribute to payout.
     * @param latestKnown           Transaction to compare for fluctuation.
     * @param latestUTXO            Last unspent transaction output for
     *                              the new Transaction's publisher.
     *                              <pw>
     *                              If it's the first time for this identity, supply
     *                              null.
     */
    fun addToInput(newT: Transaction<T>,
                   latestKnown: Transaction<T>?,
                   latestUTXO: TransactionOutput?) {
        val payout: BigDecimal
        val lkHash: String
        val lUTXOHash: String = latestUTXO?.hashId ?: ""
        //None are known for this area.
        if (latestKnown == null) {
            payout = payoutFormula(BASE,
                                   TIME_BASE,
                                   BigDecimal.ONE,
                                   VALUE_BASE,
                                   BigDecimal.ONE,
                                   newT.data.dataConstant,
                                   THRESHOLD,
                                   MATH_CONTEXT)
            lkHash = ""
        } else {
            payout = calculatePayout(newT.data,
                                     latestKnown.data)
            lkHash = latestKnown.hashId
        }
        coinbase = coinbase.add(payout)
        addToOutputs(newT.publicKey,
                     lUTXOHash,
                     newT.hashId,
                     lkHash,
                     payout)
        _hashId = recalculateHash()
    }

    private fun recalculateHash(): String =
            DEFAULT_CRYPTER.applyHash("${payoutTXO.joinToString("") { it.hashId }}$coinbase")


    private fun getTimeDelta(dt: PhysicalData<T>, dt2: PhysicalData<T>): BigDecimal {
        val stamp1 = BigDecimal(dt.instant.epochSecond * 1000 +
                                dt.instant.get(ChronoField.MILLI_OF_SECOND))
        val stamp2 = BigDecimal(dt2.instant.epochSecond * 1000 +
                                dt2.instant.get(ChronoField.MILLI_OF_SECOND))
        return stamp1.subtract(stamp2)
            .divide(stamp1, MathContext(8, RoundingMode.HALF_EVEN))
    }

    /**
     * @param publicKey Public Key of transaction publisher.
     * @param prevUTXO  Previous known UTXO's hash.
     * @param newT      Transaction to contribute to payout's hash.
     * @param prev      Transaction compared for fluctuation's hash,
     *                  might be empty.
     * @param payout    Payout amount to publisher.
     */
    private fun addToOutputs(publicKey: PublicKey,
                             prevUTXO: String,
                             newT: String,
                             prev: String,
                             payout: BigDecimal) {
        payoutTXO.firstOrNull { it.publicKey == publicKey }
            .let {
                it?.addToPayout(payout, newT, prev) ?: payoutTXO.add(TransactionOutput(publicKey,
                                                                                       prevUTXO,
                                                                                       payout,
                                                                                       newT,
                                                                                       prev))
            }
    }


    private fun calculatePayout(dt: PhysicalData<T>, dt2: PhysicalData<T>): BigDecimal =
            payoutFormula(BASE,
                          TIME_BASE,
                          getTimeDelta(dt, dt2),
                          VALUE_BASE,
                          dt.calculateDiff(dt2.data),
                          dt.dataConstant,
                          THRESHOLD,
                          MATH_CONTEXT)

}
