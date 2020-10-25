package org.knowledger.testing.core

import org.apache.commons.rng.RestorableUniformRandomProvider
import org.apache.commons.rng.simple.RandomSource
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import kotlin.random.Random

@OptIn(ExperimentalStdlibApi::class)
class TestRandom : Random() {
    private val r: RestorableUniformRandomProvider =
        RandomSource.create(RandomSource.SPLIT_MIX_64)

    fun randomString(size: Int): String = nextBytes(size).decodeToString()

    fun randomStrings(size: Int): Sequence<String> =
        generateSequence { nextBytes(size).decodeToString() }

    fun randomHash(hashers: Hashers = defaultHasher): Hash =
        hashers.applyHash(nextBytes(hashers.hashSize))

    fun random256Hash(): Hash = with(Hashers.Haraka256Hasher) {
        applyHash(nextBytes(hashSize))
    }

    fun random512Hash(): Hash = with(Hashers.Haraka512Hasher) {
        applyHash(nextBytes(hashSize))
    }

    fun randomHashes(hashers: Hashers = defaultHasher): Sequence<Hash> =
        generateSequence { randomHash(hashers) }

    fun random256Hashes(): Sequence<Hash> = generateSequence { random256Hash() }

    fun random512Hashes(): Sequence<Hash> = generateSequence { random512Hash() }

    override fun nextBoolean(): Boolean = r.nextBoolean()

    override fun nextFloat(): Float = r.nextFloat()

    override fun nextDouble(): Double = r.nextDouble()

    fun randomDoubles(): Sequence<Double> = generateSequence { nextDouble() }

    fun randomDoubles(product: (Double) -> Double): Sequence<Double> =
        generateSequence { product(nextDouble()) }

    override fun nextInt(): Int = r.nextInt()

    override fun nextInt(until: Int): Int = r.nextInt(until)

    override fun nextInt(from: Int, until: Int): Int =
        r.nextInt(until - from) + from

    fun randomInts(bound: Int = Int.MAX_VALUE): Sequence<Int> =
        generateSequence { nextInt(bound) }


    override fun nextBytes(size: Int): ByteArray =
        ByteArray(size).apply(this::nextBytes)

    override fun nextBytes(array: ByteArray): ByteArray =
        array.apply(r::nextBytes)

    override fun nextBytes(array: ByteArray, fromIndex: Int, toIndex: Int): ByteArray {
        val randomSlice = nextBytes(ByteArray(toIndex - fromIndex))
        for (index in fromIndex until toIndex) {
            array[index] = randomSlice[index - fromIndex]
        }
        return array
    }

    fun randomByteArrays(size: Int = 0): Sequence<ByteArray> =
        generateSequence { nextBytes(size) }

    override fun nextLong(): Long = r.nextLong()

    override fun nextLong(until: Long): Long = r.nextLong(until)

    override fun nextLong(from: Long, until: Long): Long = r.nextLong(until - from) + from


    fun randomLongs(bound: Long = Long.MAX_VALUE): Sequence<Long> =
        generateSequence { nextLong(bound) }

    override fun nextBits(bitCount: Int): Int = r.nextInt() ushr (32 - bitCount)
}