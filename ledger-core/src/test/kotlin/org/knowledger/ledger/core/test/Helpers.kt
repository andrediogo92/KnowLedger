package org.knowledger.ledger.core.test

import org.apache.commons.rng.RestorableUniformRandomProvider
import org.apache.commons.rng.simple.RandomSource
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hasher

private val r: RestorableUniformRandomProvider =
    RandomSource.create(RandomSource.SPLIT_MIX_64)

fun randomDouble(): Double =
    r.nextDouble()

fun randomInt(): Int =
    r.nextInt()

fun randomInt(bound: Int): Int =
    r.nextInt(bound)

fun randomBytesIntoArray(byteArray: ByteArray) {
    r.nextBytes(byteArray)
}

fun randomByteArray(size: Int): ByteArray =
    ByteArray(size).also {
        randomBytesIntoArray(it)
    }


fun applyHashInPairs(
    crypter: Hasher,
    hashes: Array<Hash>
): Hash {
    var previousHashes = hashes
    var newHashes: Array<Hash>
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