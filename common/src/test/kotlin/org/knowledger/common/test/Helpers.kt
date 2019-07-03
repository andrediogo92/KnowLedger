package org.knowledger.common.test

import org.apache.commons.rng.RestorableUniformRandomProvider
import org.apache.commons.rng.simple.RandomSource
import org.knowledger.common.config.LedgerConfiguration
import org.knowledger.common.hash.Hash
import org.knowledger.common.hash.Hasher
import java.security.Security

val r: RestorableUniformRandomProvider =
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

internal val crypter: Hasher =
    if (Security.getProvider("BC") == null) {
        Security.addProvider(
            org.bouncycastle.jce.provider.BouncyCastleProvider()
        )
        LedgerConfiguration.DEFAULT_CRYPTER
    } else {
        LedgerConfiguration.DEFAULT_CRYPTER
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