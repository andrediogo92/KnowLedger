package org.knowledger.testing.core

import org.apache.commons.rng.RestorableUniformRandomProvider
import org.apache.commons.rng.simple.RandomSource

class TestRandom {
    private val r: RestorableUniformRandomProvider =
        RandomSource.create(RandomSource.SPLIT_MIX_64)

    fun randomDouble(): Double =
        r.nextDouble()

    fun randomInt(): Int =
        r.nextInt()

    fun randomInt(bound: Int = 1): Int =
        r.nextInt(bound)

    fun randomInts(): Sequence<Int> =
        generateSequence {
            randomInt()
        }

    fun randomInts(bound: Int = 1): Sequence<Int> =
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
}