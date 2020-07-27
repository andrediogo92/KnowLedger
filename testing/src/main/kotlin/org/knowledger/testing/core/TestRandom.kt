package org.knowledger.testing.core

import org.apache.commons.rng.RestorableUniformRandomProvider
import org.apache.commons.rng.simple.RandomSource
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers

@OptIn(ExperimentalStdlibApi::class)
class TestRandom {
    private val r: RestorableUniformRandomProvider =
        RandomSource.create(RandomSource.SPLIT_MIX_64)

    fun randomString(size: Int): String =
        randomByteArray(size).decodeToString()

    fun randomStrings(size: Int): Sequence<String> =
        generateSequence {
            randomByteArray(size).decodeToString()
        }

    fun randomHash(hashers: Hashers = defaultHasher): Hash =
        hashers.applyHash(randomByteArray(hashers.hashSize))

    fun random256Hash(): Hash =
        Hashers.Haraka256Hasher.applyHash(
            randomByteArray(Hashers.Haraka256Hasher.hashSize)
        )

    fun random512Hash(): Hash =
        Hashers.Haraka512Hasher.applyHash(
            randomByteArray(Hashers.Haraka512Hasher.hashSize)
        )

    fun randomHashes(hashers: Hashers = defaultHasher): Sequence<Hash> =
        generateSequence { randomHash(hashers) }

    fun random256Hashes(): Sequence<Hash> =
        generateSequence { random256Hash() }

    fun random512Hashes(): Sequence<Hash> =
        generateSequence { random512Hash() }

    fun randomDouble(): Double =
        r.nextDouble()

    fun randomDoubles(): Sequence<Double> =
        generateSequence { randomDouble() }

    fun randomDoubles(product: (Double) -> Double): Sequence<Double> =
        generateSequence { product(randomDouble()) }


    fun randomInt(bound: Int = Int.MAX_VALUE): Int =
        r.nextInt(bound)

    fun randomInts(bound: Int = Int.MAX_VALUE): Sequence<Int> =
        generateSequence { randomInt(bound) }


    fun randomBytesIntoArray(byteArray: ByteArray) {
        r.nextBytes(byteArray)
    }

    fun randomByteArray(size: Int = 0): ByteArray =
        ByteArray(size).also(::randomBytesIntoArray)

    fun randomByteArrays(size: Int = 0): Sequence<ByteArray> =
        generateSequence { randomByteArray(size) }

    fun randomLong(bound: Long = Long.MAX_VALUE): Long =
        r.nextLong(bound)

    fun randomLongs(bound: Long = Long.MAX_VALUE): Sequence<Long> =
        generateSequence { randomLong(bound) }
}