package org.knowledger.ledger.storage.coinbase

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.config.GlobalLedgerConfiguration.GLOBALCONTEXT
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.hash.Hashers.Companion.DEFAULT_HASHER
import org.knowledger.ledger.data.DataFormula
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.storage.HashUpdateable
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.transaction.HashedTransaction
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutput
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutputImpl
import org.knowledger.ledger.storage.transaction.output.TransactionOutputImpl
import java.math.BigDecimal
import java.security.PublicKey

internal data class HashedCoinbaseImpl(
    val coinbase: CoinbaseImpl,
    internal var _hash: Hash? = null,
    private var hasher: Hashers = DEFAULT_HASHER,
    private var encoder: BinaryFormat = Cbor.plain
) : HashedCoinbase,
    HashUpdateable,
    Coinbase by coinbase {
    private var cachedSize: Long? = null

    override val approximateSize: Long
        get() = cachedSize ?: recalculateSize(hasher, encoder)

    override val hash: Hash
        get() = _hash ?: recalculateHash(hasher, encoder)

    /**
     * Internally used constructor to construct new
     * [HashedCoinbaseImpl] from inside ledger context.
     */
    internal constructor(
        info: LedgerInfo
    ) : this(
        coinbase = CoinbaseImpl(info),
        hasher = info.hasher,
        encoder = info.encoder
    )

    /**
     * Direct Coinbase Constructor for storage.
     */
    internal constructor(
        transactionOutputs: MutableSet<TransactionOutput>,
        payout: Payout, difficulty: Difficulty, blockheight: Long,
        ledgerInfo: LedgerInfo, extraNonce: Long, hash: Hash
    ) : this(
        CoinbaseImpl(
            _transactionOutputs = transactionOutputs,
            _payout = payout, _difficulty = difficulty,
            _blockheight = blockheight,
            coinbaseParams = ledgerInfo.coinbaseParams,
            _extraNonce = extraNonce, formula = ledgerInfo.formula
        ),
        _hash = hash, hasher = ledgerInfo.hasher,
        encoder = ledgerInfo.encoder
    )

    /**
     * New Hashed Coinbase Constructor.
     */
    internal constructor(
        difficulty: Difficulty, blockheight: Long,
        coinbaseParams: CoinbaseParams, dataFormula: DataFormula,
        hasher: Hashers, encoder: BinaryFormat
    ) : this(
        CoinbaseImpl(
            _transactionOutputs = mutableSetOf(),
            _payout = Payout(BigDecimal.ZERO), _difficulty = difficulty,
            _blockheight = blockheight, coinbaseParams = coinbaseParams,
            formula = dataFormula
        ),
        hasher = hasher, encoder = encoder
    )


    override fun newNonce() {
        coinbase.newNonce()
        updateHash(hasher, encoder)
    }

    override fun updateHash(
        hasher: Hashers, encoder: BinaryFormat
    ) {
        val bytes = coinbase.serialize(encoder)
        _hash = hasher.applyHash(bytes)
        cachedSize = cachedSize ?: bytes.size.toLong() +
                hasher.digester.digestLength
    }

    override fun recalculateHash(
        hasher: Hashers, encoder: BinaryFormat
    ): Hash {
        updateHash(hasher, encoder)
        return _hash as Hash
    }

    override fun recalculateSize(
        hasher: Hashers, encoder: BinaryFormat
    ): Long {
        updateHash(hasher, encoder)
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
        coinbase.updatePayout(payout)
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
                cachedSize = approximateSize - it.approximateSize(encoder)
                it.addToPayout(
                    payout,
                    newTransaction,
                    previousTransaction
                )
                cachedSize = approximateSize + it.approximateSize(encoder)
            }
            ?: coinbase.newTXO(
                HashedTransactionOutputImpl(
                    publicKey,
                    previousUTXO,
                    payout,
                    newTransaction,
                    previousTransaction,
                    hasher,
                    encoder
                ).also {
                    cachedSize = approximateSize + it.approximateSize(encoder)
                }
            )
    }

    override fun clone(): HashedCoinbaseImpl =
        copy(
            coinbase = coinbase.clone()
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HashedCoinbase) return false

        if (coinbase != other) return false
        if (_hash != other.hash) return false

        return true
    }

    override fun hashCode(): Int {
        var result = coinbase.hashCode()
        result = 31 * result + (_hash?.hashCode() ?: 0)
        return result
    }


}