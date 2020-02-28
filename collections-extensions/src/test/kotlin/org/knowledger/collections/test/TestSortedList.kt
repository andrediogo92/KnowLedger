package org.knowledger.collections.test

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.knowledger.collections.SortedList
import org.knowledger.testing.core.random

class TestSortedList {

    @Nested
    inner class CornerCases {
        private val emptyList: SortedList<Int> =
            SortedList()

        @BeforeEach
        fun `clear list`() {
            emptyList.clear()
        }

        @Test
        fun `empty List Insertions`() {
            emptyList += 2
            emptyList.add(7)
            emptyList.add(0, 5)
            emptyList.addAll(listOf(2, 8, 9))
            emptyList + 3 + 11
            assertThat(emptyList.toTypedArray()).containsExactly(2, 3, 5, 7, 8, 9, 11)
        }

        @Test
        fun `empty List Removes`() {
            assertThat(emptyList.remove(0)).isFalse()
            emptyList + 3 + 2 + 1 + 11 + 5 + 25 + 32 + 16 + 14
            assertThat(emptyList.removeAll(listOf(13, 15))).isFalse()
            assertThat(emptyList.remove(11)).isTrue()
            emptyList -= 2
            emptyList - 1 - 11
            assertThat(emptyList.toTypedArray()).containsExactly(3, 5, 14, 16, 25, 32)
        }
    }

    @Nested
    inner class Random {
        private var randomList: SortedList<Int> =
            SortedList()

        @RepeatedTest(value = 30)
        fun `random Inserts`() {
            val randoms: List<Int> = random.randomInts().take(random.randomInt(3000)).toList()
            randomList.addAll(randoms)
            assertThat(randomList.toTypedArray()).containsExactly(*randoms.distinct().sorted().toTypedArray())
        }
    }
}