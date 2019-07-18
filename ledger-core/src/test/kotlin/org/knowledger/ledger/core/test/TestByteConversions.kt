package org.knowledger.ledger.core.test

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.containsExactly
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.knowledger.ledger.core.misc.bytes
import org.tinylog.kotlin.Logger
import java.math.BigDecimal
import java.time.Instant

class TestByteConversions {
    @Test
    fun `long to bytes conversions`() {
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
            8 -> byteArrayOf(
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x21
            )
            16 -> byteArrayOf(
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x21
            )
            else -> fail {
                "Unexpected Long Size: $lsize"
            }
        }
        val bytes512 = when (lsize) {
            4 -> byteArrayOf(0x00, 0x00, 0x02, 0x00)
            8 -> byteArrayOf(
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x02, 0x00
            )
            16 -> byteArrayOf(
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x02, 0x00
            )
            else -> fail {
                "Unexpected Long Size: $lsize"
            }
        }
        val bytes33564286 = when (lsize) {
            4 -> byteArrayOf(0x02, 0x00, 0x26, 0x7E)
            8 -> byteArrayOf(
                0x00, 0x00, 0x00, 0x00,
                0x02, 0x00, 0x26, 0x7E
            )
            16 -> byteArrayOf(
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x02, 0x00, 0x26, 0x7E
            )
            else -> fail {
                "Unexpected Long Size: $lsize"
            }
        }
        assertAll {
            assertThat(test0.bytes()).containsExactly(*bytes0)
            assertThat(test33.bytes()).containsExactly(*bytes33)
            assertThat(test512.bytes()).containsExactly(*bytes512)
            assertThat(test33564286.bytes()).containsExactly(*bytes33564286)
        }
    }

    @Test
    fun `int to bytes conversions`() {
        val isize = Int.SIZE_BYTES
        val test0 = 0
        val test33 = 33
        val test512 = 512
        val test64321 = 64321
        val bytes0 = ByteArray(isize) {
            0x00
        }
        val bytes33 = when (isize) {
            2 -> byteArrayOf(0x00, 0x21)
            4 -> byteArrayOf(
                0x00, 0x00, 0x00, 0x21
            )
            8 -> byteArrayOf(
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x21
            )
            else -> fail {
                "Unexpected Int Size: $isize"
            }
        }
        val bytes512 = when (isize) {
            2 -> byteArrayOf(0x02, 0x00)
            4 -> byteArrayOf(
                0x00, 0x00, 0x02, 0x00
            )
            8 -> byteArrayOf(
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x02, 0x00
            )
            else -> fail {
                "Unexpected Int Size: $isize"
            }
        }
        val bytes64321 = when (isize) {
            2 -> byteArrayOf(0xFB.toByte(), 0x41)
            4 -> byteArrayOf(
                0x00, 0x00, 0xFB.toByte(), 0x41
            )
            8 -> byteArrayOf(
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0xFB.toByte(), 0x41
            )
            else -> fail {
                "Unexpected Int Size: $isize"
            }
        }
        assertAll {
            assertThat(test0.bytes()).containsExactly(*bytes0)
            assertThat(test33.bytes()).containsExactly(*bytes33)
            assertThat(test512.bytes()).containsExactly(*bytes512)
            assertThat(test64321.bytes()).containsExactly(*bytes64321)
        }
    }

    @Test
    fun `double to bytes conversions`() {
        //IEEE 754 double precision -> fixed 8 bytes.
        val test0 = 0.0
        val test33 = "33.5".toDouble()
        Logger.debug(test33.toString())
        val test3Dot1415 = "3.1415".toDouble()
        Logger.debug(test3Dot1415.toString())
        val test1Dot56ToN11 = "1.56e-11".toDouble()
        Logger.debug(test1Dot56ToN11.toString())
        val bytes0 = ByteArray(8)
        val bytes33Dot5 = byteArrayOf(
            0x40, 0x40, 0xC0.toByte(), 0x00,
            0x00, 0x00, 0x00, 0x00
        )

        val bytes3Dot1415 = byteArrayOf(
            0x40, 0x09, 0x21, 0xCA.toByte(),
            0xC0.toByte(), 0x83.toByte(), 0x12, 0x6F
        )

        val bytes1Dot56ToN11 = byteArrayOf(
            0x3D, 0xB1.toByte(), 0x27, 0x02,
            0x77, 0x8C.toByte(), 0xC4.toByte(), 0x37
        )
        assertAll {
            assertThat(test0.bytes()).containsExactly(*bytes0)
            assertThat(test33.bytes()).containsExactly(*bytes33Dot5)
            assertThat(test3Dot1415.bytes()).containsExactly(*bytes3Dot1415)
            assertThat(test1Dot56ToN11.bytes()).containsExactly(*bytes1Dot56ToN11)
        }
    }

    @Test
    fun `instant to bytes conversions`() {
        val testSeconds = 85321234L
        val testNanos = 423
        val testInstant = Instant.ofEpochSecond(testSeconds, testNanos.toLong())
        val bytesSeconds = testSeconds.bytes()
        val bytesNanos = testNanos.bytes()

        assertThat(testInstant.bytes()).containsExactly(*(bytesSeconds + bytesNanos))
    }

    @Test
    fun `big decimal to bytes conversions`() {
        val testDecimal = BigDecimal("3.1415")
        val bytesDecimal = testDecimal.bytes()

        assertThat(bytesDecimal).containsExactly(*testDecimal.unscaledValue().toByteArray())
    }

}
