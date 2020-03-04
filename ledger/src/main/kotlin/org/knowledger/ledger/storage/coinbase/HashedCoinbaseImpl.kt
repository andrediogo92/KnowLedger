package org.knowledger.ledger.storage.coinbase

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.cbor.Cbor
import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.hash.Hashers.Companion.DEFAULT_HASHER
import org.knowledger.ledger.crypto.hash.toEncoded
import org.knowledger.ledger.data.DataFormula
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.storage.HashUpdateable
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.Witness
import org.knowledger.ledger.storage.witness.HashedWitnessImpl

internal data class HashedCoinbaseImpl(
    val coinbase: CoinbaseImpl,
    internal var _hash: Hash? = null,
    private var hasher: Hashers = DEFAULT_HASHER,
    private var encoder: BinaryFormat = Cbor
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
        witnesses: MutableSortedList<Witness>,
        payout: Payout, difficulty: Difficulty, blockheight: Long,
        ledgerInfo: LedgerInfo, extraNonce: Long, hash: Hash
    ) : this(
        CoinbaseImpl(
            _witnesses = witnesses,
            coinbaseParams = ledgerInfo.coinbaseParams,
            _payout = payout, _difficulty = difficulty,
            _blockheight = blockheight,
            _extraNonce = extraNonce, formula = ledgerInfo.formula
        ), _hash = hash, hasher = ledgerInfo.hasher,
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
            coinbaseParams = coinbaseParams,
            _difficulty = difficulty,
            _blockheight = blockheight,
            formula = dataFormula
        ), hasher = hasher, encoder = encoder
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

    override fun findWitness(tx: Transaction): Int =
        witnesses.binarySearch {
            it.publicKey.compareTo(tx.publicKey.toEncoded())
        }


    override fun addToWitness(
        witness: Witness,
        newIndex: Int, newTransaction: Transaction,
        latestKnownIndex: Int,
        latestKnown: Transaction?,
        latestKnownBlockHash: Hash
    ) {
        val payout: Payout =
            checkInput(newTransaction, latestKnown)

        coinbase.updatePayout(payout)
        addToOutputs(
            witness = witness,
            payout = payout,
            newIndex = newIndex,
            newTransaction = newTransaction.hash,
            previousBlock = latestKnownBlockHash,
            previousIndex = latestKnownIndex,
            previousTransaction = latestKnownBlockHash
        )

    }

    override fun addToWitness(
        newIndex: Int, newTransaction: Transaction,
        previousWitnessIndex: Int, latestCoinbase: Hash,
        latestKnownIndex: Int,
        latestKnown: Transaction?,
        latestKnownBlockHash: Hash
    ) {
        val payout: Payout =
            checkInput(newTransaction, latestKnown)

        coinbase.updatePayout(payout)
        addToOutputs(
            publicKey = newTransaction.publicKey.toEncoded(),
            previousWitnessIndex = previousWitnessIndex,
            previousCoinbase = latestCoinbase,
            newIndex = newIndex,
            newTransaction = newTransaction.hash,
            previousBlock = latestKnownBlockHash,
            previousIndex = latestKnownIndex,
            previousTransaction = latestKnownBlockHash,
            payout = payout
        )
    }

    private fun checkInput(
        newTransaction: Transaction,
        latestKnown: Transaction?
    ): Payout =
        //None are known for this area.
        if (latestKnown == null) {
            coinbase.calculatePayout(
                newTransaction.data
            )
        } else {
            coinbase.calculatePayout(
                newTransaction.data,
                latestKnown.data
            )
        }

    /**
     * Adds a [payout] to a transaction output in the witness's
     * owner's behalf.
     *
     * The [witness] is guaranteed to exist.
     * Creates a new [TransactionOutput] with the transactions counted
     * into the calculation of the total payout to this participant.
     * The respective [payout] associated with this transaction is
     * added to the total for this [Witness].
     */
    private fun addToOutputs(
        witness: Witness, payout: Payout,
        newIndex: Int, newTransaction: Hash,
        previousBlock: Hash, previousIndex: Int,
        previousTransaction: Hash
    ) {
        cachedSize = approximateSize - witness.approximateSize(encoder)
        witness.addToPayout(
            payout = payout, newIndex = newIndex,
            newTransaction = newTransaction,
            previousBlock = previousBlock,
            previousIndex = previousIndex,
            previousTransaction = previousTransaction
        )
        cachedSize = approximateSize + witness.approximateSize(encoder)
    }

    /**
     * Adds a [payout] to a transaction output in the [publicKey]'s
     * owner's behalf.
     *
     * The [Witness] does not yet exist, so a new [Witness]
     * is created referencing the [previousCoinbase].
     */
    private fun addToOutputs(
        publicKey: EncodedPublicKey, payout: Payout,
        previousWitnessIndex: Int, previousCoinbase: Hash,
        newIndex: Int, newTransaction: Hash,
        previousBlock: Hash, previousIndex: Int,
        previousTransaction: Hash
    ) {
        coinbase.newTXO(
            HashedWitnessImpl(
                publicKey = publicKey,
                previousWitnessIndex = previousWitnessIndex,
                previousCoinbase = previousCoinbase, payout = payout,
                newIndex = newIndex, newTransaction = newTransaction,
                previousBlock = previousBlock,
                previousIndex = previousIndex,
                previousTransaction = previousTransaction,
                hasher = hasher, encoder = encoder
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