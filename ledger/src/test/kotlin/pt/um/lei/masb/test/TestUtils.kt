package pt.um.lei.masb.test

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import mu.KLogging
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import pt.um.lei.masb.blockchain.ledger.print
import pt.um.lei.masb.blockchain.utils.bytes
import pt.um.lei.masb.blockchain.utils.flattenBytes
import pt.um.lei.masb.test.utils.applyHashInPairs
import pt.um.lei.masb.test.utils.crypter
import pt.um.lei.masb.test.utils.r

class TestUtils {
    val randomHashes = Array(8) {
        ByteArray(32).also {
            r.nextBytes(it)
        }
    }


    @Test
    fun `Test bytes conversions`() {
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

    @Test
    fun `Test byte flattening`() {
        val expected = randomHashes.reduce { acc, bytes ->
            acc + bytes
        }
        val test = flattenBytes(
            *randomHashes
        )
        assertThat(expected.size).isEqualTo(test.size)
        assertThat(expected).containsExactly(*test)
    }

    @Test
    fun `Test pair crypting balanced`() {
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
               | Test: ${test.print()}
               | Expected: ${expected.print()}
            """.trimMargin()
        }

        assertThat(expected).containsExactly(*test)

    }

    @Test
    fun `Test pair crypting unbalanced`() {
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
               | Test: ${test.print()}
               | Expected: ${expected.print()}
            """.trimMargin()
        }


        assertThat(expected).containsExactly(*test)

    }

    companion object : KLogging()
}