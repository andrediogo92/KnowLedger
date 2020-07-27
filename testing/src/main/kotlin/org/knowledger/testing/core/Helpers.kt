package org.knowledger.testing.core

import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.testing.ledger.RandomData

typealias DataGenerator = () -> LedgerData

val random: TestRandom = TestRandom()
val defaultHasher: Hashers = Hashers.DEFAULT_HASHER

fun randomData(): LedgerData =
    RandomData(5, 5)

fun applyHashInPairs(hashers: Hashers, hashes: Array<Hash>): Hash {
    var previousHashes = hashes
    var newHashes: Array<Hash>
    var levelIndex = hashes.size
    while (levelIndex > 2) {
        if (levelIndex % 2 == 0) {
            levelIndex /= 2
            newHashes = Array(levelIndex) {
                hashers.applyHash(
                    previousHashes[it * 2] + previousHashes[it * 2 + 1]
                )
            }
        } else {
            levelIndex /= 2
            levelIndex++
            newHashes = Array(levelIndex) {
                if (it != levelIndex - 1) {
                    hashers.applyHash(
                        previousHashes[it * 2] + previousHashes[it * 2 + 1]
                    )
                } else {
                    hashers.applyHash(
                        previousHashes[it * 2] + previousHashes[it * 2]
                    )
                }
            }
        }
        previousHashes = newHashes
    }
    return hashers.applyHash(previousHashes[0] + previousHashes[1])
}