package org.knowledger.collections.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.knowledger.collections.FixedSizeObjectPool
import org.knowledger.ledger.core.data.hash.Hash
import org.knowledger.testing.core.random

class TestPool {
    @Test
    fun `empty pool`() {
        val objectPool = FixedSizeObjectPool(0) {
            Hash.emptyHash
        }

        assertThrows<IndexOutOfBoundsException> { objectPool.lease() }
        assertThrows<IndexOutOfBoundsException> { objectPool.free(Hash.emptyHash) }
    }

    @Test
    fun `single element pool`() {
        val randomHash = random.random256Hash()
        val objectPool = FixedSizeObjectPool(1) {
            randomHash
        }

        assertThat(objectPool.lease()).isEqualTo(randomHash)
        assertDoesNotThrow { objectPool.free(randomHash) }
        assertThrows<IndexOutOfBoundsException> {
            objectPool.lease()
            objectPool.lease()
        }
    }

    @Test
    fun `tons of elements pool`() {
        val size = random.nextInt(20, 200)
        val pairs = Array(size) {
            Pair(random.random256Hash(), random.random256Hash())
        };
        val objectPool = run {
            var i = -1
            FixedSizeObjectPool(size) {
                i += 1
                pairs[i]
            }
        }
        for (i in 0 until size) {
            assertThat(objectPool.lease()).isEqualTo(pairs[size - 1 - i])
        }

        val permut = (0 until size).toList().shuffled(random)
        for (i in permut) {
            assertDoesNotThrow {
                objectPool.free(pairs[i])
            }
        }
    }

}