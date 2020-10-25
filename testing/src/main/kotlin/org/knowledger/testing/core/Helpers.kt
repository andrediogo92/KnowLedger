package org.knowledger.testing.core

import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers

typealias DataGenerator = () -> LedgerData

val random: TestRandom = TestRandom()
val defaultHasher: Hashers = Hashers.DEFAULT_HASHER

fun applyHashInPairs(hashers: Hashers, hashes: Array<Hash>): Hash {
    var previousHashes = hashes
    var newHashes: Array<Hash>
    var levelIndex = hashes.size
    while (levelIndex > 2) {
        if (levelIndex % 2 == 0) {
            levelIndex /= 2
            newHashes = Array(levelIndex) { index ->
                hashers.applyHash(previousHashes[index * 2] + previousHashes[index * 2 + 1])
            }
        } else {
            levelIndex /= 2
            levelIndex++
            newHashes = Array(levelIndex) { index ->
                if (index != levelIndex - 1) {
                    hashers.applyHash(previousHashes[index * 2] + previousHashes[index * 2 + 1])
                } else {
                    hashers.applyHash(previousHashes[index * 2] + previousHashes[index * 2])
                }
            }
        }
        previousHashes = newHashes
    }
    return hashers.applyHash(previousHashes[0] + previousHashes[1])
}