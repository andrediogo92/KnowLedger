package org.knowledger.ledger.crypto

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.knowledger.ledger.core.data.toHexString
import org.knowledger.ledger.crypto.hash.Hashers
import org.tinylog.Logger

class TestHashers {
    val test = "testByteArray".toByteArray()
    val fixed32 = ByteArray(32, Int::toByte)
    val fixed64 = ByteArray(64, Int::toByte)

    private fun logOutputs(algorithm: String, test: String, expected: String) {
        Logger.debug {
            """ $algorithm
                |test: $test
                |expected: $expected
            """.trimMargin()
        }
    }

    @Nested
    inner class SHA {

        @Test
        fun `apply SHA-256`() {
            val expected32 = "6EC7BBB8088AD0582BA4CCA03667C20BA7A58F5A6CBAB23706B6F4260CAECC5A"
            val test = Hashers.SHA256Hasher.applyHash(test).toHexString()
            assertThat(test).isEqualTo(expected32)
            logOutputs("SHA-256", test, expected32)
        }

        @Test
        fun `apply SHA-512`() {
            val expected64 =
                "3ED19606CF37AD45E9E6D152EB30BE6813576237E08829AFC90312A1FD908214D7B3B7C194520AC1808375AB83A4CE65712C98995083A55704985F91C419963E"
            val test = Hashers.SHA512Hasher.applyHash(test).toHexString()
            assertThat(test).isEqualTo(expected64)
            logOutputs("SHA-512", test, expected64)
        }
    }

    @Nested
    inner class HARAKA {
        @Test
        fun `apply HARAKA-256`() {
            val expected32 = "8027CCB87949774B78D0545FB72BF70C695C2A0923CBD47BBA1159EFBF2B2C1C"
            val test = Hashers.Haraka256Hasher.applyHash(fixed32).toHexString()
            assertThat(test).isEqualTo(expected32)
            logOutputs("HARAKA-256", test, expected32)
        }

        @Test
        fun `apply HARAKA-512`() {
            val expected64 = "BE7F723B4E80A99813B292287F306F625A6D57331CAE5F34DD9277B0945BE2AA"
            val test = Hashers.Haraka512Hasher.applyHash(fixed64).toHexString()
            assertThat(test).isEqualTo(expected64)
            logOutputs("HARAKA-512", test, expected64)
        }
    }

    @Nested
    inner class BLAKE2 {
        @Test
        fun `apply BLAKE2S-256`() {
            val test = Hashers.Blake2s256Hasher.applyHash(test).toHexString()
            val expected32 = "71E7093BAC0CACE10B4E00ADFDFC48B6C4B5771D0B3522880C4F5F97BB551168"
            assertThat(test).isEqualTo(expected32)
            logOutputs("BLAKE2S-256", test, expected32)
        }

        /**
         * BEWARE: Not entirely verified externally to be matching some other reference implementation.
         */
        @Test
        fun `apply BLAKE2B-256`() {
            val test = Hashers.Blake2b256Hasher.applyHash(test).toHexString()
            val expected32 = "6A1FAE02BBF98D1A96C99C38F616D4559E527E3CC1A719A996C093A7867C8FFA"
            assertThat(test).isEqualTo(expected32)
            logOutputs("BLAKE2B-256", test, expected32)
        }

        @Test
        fun `apply BLAKE2B-512`() {
            val test = Hashers.Blake2b512Hasher.applyHash(test).toHexString()
            val expected64 =
                "9268B2FCE73C20D8E4CEFF7BB401968D77B0AA044F63025885E509714EA930F283E57D91919960B1EB573ED60EC303E15236469045F9570A44DB93A2AAE62405"
            assertThat(test).isEqualTo(expected64)
            logOutputs("BLAKE2B-512", test, expected64)
        }
    }


    @Nested
    inner class SHA3 {
        @Test
        fun `apply SHA3-256`() {
            val test = Hashers.SHA3256Hasher.applyHash(test).toHexString()
            val expected32 = "8F42B68F1B239CD0F9F51EABFD43DAE5775CB1531E1CC64444115D1582518283"
            assertThat(test).isEqualTo(expected32)
            logOutputs("SHA3-256", test, expected32)
        }

        @Test
        fun `apply SHA3-512`() {
            val test = Hashers.SHA3512Hasher.applyHash(test).toHexString()
            val expected64 =
                "19B24FE949322476C9131D16F55D240B3B3CA288A8E0F379250101C3698ED06C8AC134DD4A80576051159DD3641B1421491223934C1F4E54D297E8FFA147DFE2"
            assertThat(test).isEqualTo(expected64)
            logOutputs("SHA3-512", test, expected64)
        }
    }

    @Nested
    inner class KECCAK {
        /**
         * BEWARE: Not entirely verified externally to be matching some other reference implementation.
         */
        @Test
        fun `apply KECCAK-256`() {
            val test = Hashers.Keccak256Hasher.applyHash(test).toHexString()
            val expected32 = "89AE94189AAB48578D1610872CDE9C5F647BF29494B54C5B7D4C7655627E34B7"
            assertThat(test).isEqualTo(expected32)
            logOutputs("KECCAK-256", test, expected32)
        }

        /**
         * BEWARE: Not entirely verified externally to be matching some other reference implementation.
         */
        @Test
        fun `apply KECCAK-512`() {
            val test = Hashers.Keccak512Hasher.applyHash(test).toHexString()
            val expected64 =
                "A473EA3FAF6BC58B04E41346CC729BFF2B1C8CCCF84CC183F97CB5AFF796EB49C071655D28B1C36A4A6819EB55CDCFBD920977717EEFB09BD2B34E6DFCE51138"
            assertThat(test).isEqualTo(expected64)
            logOutputs("KECCAK-512", test, expected64)
        }
    }
}