package pt.um.lei.masb.test

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import mu.KLogging
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import pt.um.lei.masb.blockchain.ledger.print
import pt.um.lei.masb.blockchain.utils.bytes
import pt.um.lei.masb.blockchain.utils.flattenBytes
import pt.um.lei.masb.test.utils.applyHashInPairs
import pt.um.lei.masb.test.utils.crypter
import pt.um.lei.masb.test.utils.randomByteArray
import pt.um.lei.masb.test.utils.randomInt

class TestUtils {
    @Test
    fun `bytes conversions`() {
        val lsize = Long.SIZE_BYTES
        val test0 = 0L
        val test33 = 33L
        val test512 = 512L
        val test33564286 = 33564286L
        val bytes0 = ByteArray(lsize) {
            0x00
        }
        val bytes33 = when (lsize) {
            4 -> byteArrayOf(0x00, 0x00, 0x00, 0x21)
            8 -> byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x21)
            16 -> byteArrayOf(
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x21
            )
            else -> fail {
                "Unexpected Long Size: $lsize"
            }
        }
        val bytes512 = when (lsize) {
            4 -> byteArrayOf(0x00, 0x00, 0x02, 0x00)
            8 -> byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x00)
            16 -> byteArrayOf(
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x02,
                0x00
            )
            else -> fail {
                "Unexpected Long Size: $lsize"
            }
        }
        val bytes33564286 = when (lsize) {
            4 -> byteArrayOf(0x02, 0x00, 0x26, 0x7E)
            8 -> byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x26, 0x7E)
            16 -> byteArrayOf(
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x02,
                0x00,
                0x26,
                0x7E
            )
            else -> fail {
                "Unexpected Long Size: $lsize"
            }
        }
        assertThat(test0.bytes()).containsExactly(*bytes0)
        assertThat(test33.bytes()).containsExactly(*bytes33)
        assertThat(test512.bytes()).containsExactly(*bytes512)
        assertThat(test33564286.bytes()).containsExactly(*bytes33564286)
    }

    @Nested
    inner class HashingOperations {
        val randomHashes = Array(randomInt(248) + 8) {
            randomByteArray(32)
        }

        @Test
        fun `byte flattening`() {
            val expected = randomHashes.reduce { acc, bytes ->
                acc + bytes
            }
            val expectedShort = expected.sliceArray(0..255)
            val test = flattenBytes(
                *randomHashes
            )
            val test2 = flattenBytes(
                randomHashes.sliceArray(0..6).toList(),
                randomHashes[7]
            )
            val test3 = flattenBytes(
                randomHashes
            )
            assertThat(expected.size).isEqualTo(test.size)
            assertThat(expectedShort.size).isEqualTo(test2.size)
            assertThat(expected.size).isEqualTo(test3.size)
            logger.debug {
                """
                |
                |Expected:
                |   - ${expected.print()}
                |Flatten via vararg byte arrays:
                |   - ${test.print()}
            """.trimMargin()
            }
            assertThat(expected).containsExactly(*test)
            logger.debug {
                """
                |
                |Expected:
                |   - ${expected.print()}
                |Flatten via direct AoA:
                |   - ${test3.print()}
            """.trimMargin()
            }
            assertThat(expected).containsExactly(*test3)
            logger.debug {
                """
                |
                |Expected:
                |   - ${expectedShort.print()}
                |Flatten via collection + vararg byte arrays:
                |   - ${test2.print()}
            """.trimMargin()
            }
            assertThat(expectedShort).containsExactly(*test2)
        }

        @Test
        fun `pair crypting balanced`() {
            val expected = crypter.applyHash(
                crypter.applyHash(
                    crypter.applyHash(
                        randomHashes[0] + randomHashes[1]
                    ) + crypter.applyHash(
                        randomHashes[2] + randomHashes[3]
                    )
                ) + crypter.applyHash(
                    crypter.applyHash(
                        randomHashes[4] + randomHashes[5]
                    ) + crypter.applyHash(
                        randomHashes[6] + randomHashes[7]
                    )
                )
            )

            val test = applyHashInPairs(
                crypter,
                arrayOf(
                    randomHashes[0],
                    randomHashes[1],
                    randomHashes[2],
                    randomHashes[3],
                    randomHashes[4],
                    randomHashes[5],
                    randomHashes[6],
                    randomHashes[7]
                )
            )

            logger.info {
                """
                |
                | Test: ${test.print()}
                | Expected: ${expected.print()}
            """.trimMargin()
            }

            assertThat(expected).containsExactly(*test)

        }

        @Test
        fun `pair crypting unbalanced`() {
            val expected = crypter.applyHash(
                crypter.applyHash(
                    crypter.applyHash(
                        randomHashes[0] + randomHashes[1]
                    ) + crypter.applyHash(
                        randomHashes[2] + randomHashes[3]
                    )
                ) + crypter.applyHash(
                    crypter.applyHash(
                        randomHashes[4] + randomHashes[5]
                    ) + crypter.applyHash(
                        randomHashes[4] + randomHashes[5]
                    )
                )
            )

            val test = applyHashInPairs(
                crypter,
                arrayOf(
                    randomHashes[0],
                    randomHashes[1],
                    randomHashes[2],
                    randomHashes[3],
                    randomHashes[4],
                    randomHashes[5]
                )
            )

            logger.info {
                """
                |
                | Test: ${test.print()}
                | Expected: ${expected.print()}
            """.trimMargin()
            }


            assertThat(expected).containsExactly(*test)

        }
    }



    companion object : KLogging()
}