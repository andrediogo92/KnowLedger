package org.knowledger.ledger.storage.coinbase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.core.config.GlobalLedgerConfiguration.GLOBALCONTEXT
import org.knowledger.ledger.core.data.DataFormula
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.hash.Hashers.Companion.DEFAULT_HASHER
import org.knowledger.ledger.service.LedgerContainer
import org.knowledger.ledger.storage.HashUpdateable
import org.knowledger.ledger.storage.transaction.HashedTransaction
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutput
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutputImpl
import org.knowledger.ledger.storage.transaction.output.TransactionOutputImpl
import java.math.BigDecimal
import java.security.PublicKey

@Serializable
@SerialName("HashedCoinbase")
internal data class HashedCoinbaseImpl(
    val coinbase: CoinbaseImpl,
    @SerialName("hash")
    internal var _hash: Hash? = null,
    @Transient
    private var hasher: Hashers = DEFAULT_HASHER,
    @Transient
    private var cbor: Cbor = Cbor.plain
) : HashedCoinbase,
    HashUpdateable,
    Coinbase by coinbase {
    @Transient
    private var cachedSize: Long? = null

    override val approximateSize: Long
        get() = cachedSize ?: recalculateSize(hasher, cbor)

    override val hash: Hash
        get() = _hash ?: recalculateHash(hasher, cbor)

    /**
     * Internally used constructor to construct new [HashedCoinbaseImpl] from inside
     * ledger context.
     */
    internal constructor(
        difficulty: Difficulty, blockheight: Long,
        container: LedgerContainer
    ) : this(
        difficulty, blockheight,
        container.coinbaseParams,
        container.formula,
        container.hasher, container.cbor
    )

    constructor(
        transactionOutputs: MutableSet<HashedTransactionOutput>,
        payout: Payout, difficulty: Difficulty,
        blockheight: Long, extraNonce: Long,
        coinbaseParams: CoinbaseParams, formula: DataFormula,
        hash: Hash, hasher: Hashers, cbor: Cbor
    ) : this(
        CoinbaseImpl(
            _transactionOutputs = transactionOutputs,
            payout = payout, difficulty = difficulty,
            blockheight = blockheight, extraNonce = extraNonce,
            coinbaseParams = coinbaseParams, formula = formula
        ), _hash = hash, hasher = hasher, cbor = cbor
    )

    constructor(
        transactionOutputs: MutableSet<HashedTransactionOutput>,
        payout: Payout, difficulty: Difficulty,
        blockheight: Long, coinbaseParams: CoinbaseParams, formula: DataFormula,
        hash: Hash, hasher: Hashers, cbor: Cbor
    ) : this(
        CoinbaseImpl(
            _transactionOutputs = transactionOutputs,
            payout = payout, difficulty = difficulty,
            blockheight = blockheight,
            coinbaseParams = coinbaseParams, formula = formula
        ), _hash = hash, hasher = hasher, cbor = cbor
    )

    constructor(
        transactionOutputs: MutableSet<HashedTransactionOutput>,
        payout: Payout, difficulty: Difficulty,
        blockheight: Long, coinbaseParams: CoinbaseParams,
        formula: DataFormula, hasher: Hashers, cbor: Cbor
    ) : this(
        CoinbaseImpl(
            _transactionOutputs = transactionOutputs,
            payout = payout, difficulty = difficulty,
            blockheight = blockheight,
            coinbaseParams = coinbaseParams, formula = formula
        ), hasher = hasher, cbor = cbor
    )

    /**
     * New Hashed Coinbase Constructor.
     */
    constructor(
        difficulty: Difficulty, blockheight: Long,
        coinbaseParams: CoinbaseParams, dataFormula: DataFormula,
        hasher: Hashers, cbor: Cbor
    ) : this(
        transactionOutputs = mutableSetOf(),
        payout = Payout(BigDecimal.ZERO), difficulty = difficulty,
        blockheight = blockheight, coinbaseParams = coinbaseParams,
        formula = dataFormula, hasher = hasher, cbor = cbor
    )


    override fun updateHash(
        hasher: Hasher, cbor: Cbor
    ) {
        val bytes = coinbase.serialize(cbor)
        _hash = hasher.applyHash(bytes)
        cachedSize = cachedSize ?: bytes.size.toLong() +
                _hash!!.bytes.size.toLong()
    }

    override fun recalculateHash(
        hasher: Hasher, cbor: Cbor
    ): Hash {
        updateHash(hasher, cbor)
        return _hash as Hash
    }

    override fun recalculateSize(
        hasher: Hasher, cbor: Cbor
    ): Long {
        updateHash(hasher, cbor)
        return cachedSize as Long
    }

    override fun addToInput(
        newTransaction: HashedTransaction,
        latestKnown: HashedTransaction?,
        latestUTXO: HashedTransactionOutput?
    ) {
        val payout: Payout
        val lkHash: Hash
        val lUTXOHash: Hash = latestUTXO?.hash
            ?: Hash.emptyHash

        //None are known for this area.
        if (latestKnown == null) {
            payout = formula.calculateDiff(
                coinbaseParams.baseIncentive,
                coinbaseParams.timeIncentive,
                BigDecimal.ONE,
                coinbaseParams.valueIncentive,
                BigDecimal.ONE,
                newTransaction.data.dataConstant,
                coinbaseParams.dividingThreshold,
                GLOBALCONTEXT
            )
            lkHash = Hash.emptyHash
        } else {
            payout = coinbase.calculatePayout(
                newTransaction.data,
                latestKnown.data,
                formula,
                coinbaseParams
            )
            lkHash = latestKnown.hash
        }
        this.payout += payout
        addToOutputs(
            newTransaction.publicKey,
            lUTXOHash,
            newTransaction.hash,
            lkHash,
            payout
        )
    }

    /**
     * Adds a [payout] to a transaction output in the [publicKey]'s
     * owner's behalf.
     *
     * If a [TransactionOutputImpl] for this same [PublicKey] representing
     * an active participant already exists, appends the pair consisting
     * of [newTransaction] and [previousTransaction] to the set of
     * transactions counted into the calculation of the total payout to
     * this participant.
     * The respective [payout] associated with this transaction is
     * added to the total for this [TransactionOutputImpl].
     *
     * If a [TransactionOutputImpl] does not yet exist, a new [TransactionOutputImpl]
     * is created referencing the [previousUTXO].
     */
    private fun addToOutputs(
        publicKey: PublicKey,
        previousUTXO: Hash,
        newTransaction: Hash,
        previousTransaction: Hash,
        payout: Payout
    ) {
        transactionOutputs
            .firstOrNull { it.publicKey == publicKey }
            ?.let {
                cachedSize = approximateSize - it.approximateSize(cbor)
                it.addToPayout(
                    payout,
                    newTransaction,
                    previousTransaction
                )
                cachedSize = approximateSize + it.approximateSize(cbor)
            }
            ?: coinbase._transactionOutputs.add(
                HashedTransactionOutputImpl(
                    publicKey,
                    previousUTXO,
                    payout,
                    newTransaction,
                    previousTransaction,
                    hasher,
                    cbor
                ).also {
                    cachedSize = approximateSize + it.approximateSize(cbor)
                }
            )
    }

    override fun clone(): HashedCoinbase =
        copy()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HashedCoinbaseImpl) return false

        if (coinbase != other.coinbase) return false
        if (_hash != other._hash) return false

        return true
    }

    override fun hashCode(): Int {
        var result = coinbase.hashCode()
        result = 31 * result + (_hash?.hashCode() ?: 0)
        return result
    }


}