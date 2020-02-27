package org.knowledger.testing.core

import org.apache.commons.rng.RestorableUniformRandomProvider
import org.apache.commons.rng.simple.RandomSource
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.hash.Hashers

class TestRandom {
    private val r: RestorableUniformRandomProvider =
        RandomSource.create(RandomSource.SPLIT_MIX_64)

    @UseExperimental(ExperimentalStdlibApi::class)
    fun randomString(size: Int): String =
        randomByteArray(size).decodeToString()

    @UseExperimental(ExperimentalStdlibApi::class)
    fun randomStrings(size: Int): Sequence<String> =
        generateSequence {
            randomByteArray(size).decodeToString()
        }

    fun random256Hash(): Hash =
        Hashers.Haraka256Hasher.applyHash(
            randomByteArray(32)
        )

    fun random512Hash(): Hash =
        Hashers.Haraka512Hasher.applyHash(
            randomByteArray(64)
        )

    fun random256Hashes(): Sequence<Hash> =
        generateSequence {
            random256Hash()
        }

    fun random512Hashes(): Sequence<Hash> =
        generateSequence {
            random512Hash()
        }

    fun randomDouble(): Double =
        r.nextDouble()

    fun randomDoubles(): Sequence<Double> =
        generateSequence {
            randomDouble()
        }

    fun randomDoubles(product: (Double) -> Double): Sequence<Double> =
        generateSequence {
            product(randomDouble())
        }


    fun randomInt(bound: Int = Int.MAX_VALUE): Int =
        r.nextInt(bound)

    fun randomInts(bound: Int = Int.MAX_VALUE): Sequence<Int> =
        generateSequence {
            randomInt(bound)
        }


    fun randomBytesIntoArray(byteArray: ByteArray) {
        r.nextBytes(byteArray)
    }

    fun randomByteArray(size: Int = 0): ByteArray =
        ByteArray(size).also(::randomBytesIntoArray)

    fun randomByteArrays(size: Int = 0): Sequence<ByteArray> =
        generateSequence {
            randomByteArray(size)
        }

    fun randomLong(bound: Long = Long.MAX_VALUE): Long =
        r.nextLong(bound)

    fun randomLongs(bound: Long = Long.MAX_VALUE): Sequence<Long> =
        generateSequence {
            randomLong(bound)
        }
}