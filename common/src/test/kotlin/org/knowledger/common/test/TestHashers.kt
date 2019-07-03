package org.knowledger.common.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.knowledger.common.hash.AvailableHashAlgorithms

class TestHashers {
    val test = "testByteArray".toByteArray()

    @Nested
    inner class SHA {
        @Test
        fun `apply sha-256`() {
            assertThat(AvailableHashAlgorithms.SHA256Hasher.applyHash(test).print)
                .isEqualTo("6EC7BBB8088AD0582BA4CCA03667C20BA7A58F5A6CBAB23706B6F4260CAECC5A")
        }

        @Test
        fun `apply sha-512`() {
            assertThat(AvailableHashAlgorithms.SHA512Hasher.applyHash(test).print)
                .isEqualTo("3ED19606CF37AD45E9E6D152EB30BE6813576237E08829AFC90312A1FD908214D7B3B7C194520AC1808375AB83A4CE65712C98995083A55704985F91C419963E")
        }
    }

    @Nested
    inner class Blake2 {
        @Test
        fun `apply blake2s-256`() {
            assertThat(AvailableHashAlgorithms.Blake2s256Hasher.applyHash(test).print)
                .isEqualTo("71E7093BAC0CACE10B4E00ADFDFC48B6C4B5771D0B3522880C4F5F97BB551168")
        }

        /**
         * BEWARE: Not entirely verified externally to be matching some other reference implementation.
         */
        @Test
        fun `apply blake2b-256`() {
            assertThat(AvailableHashAlgorithms.Blake2b256Hasher.applyHash(test).print)
                .isEqualTo("6A1FAE02BBF98D1A96C99C38F616D4559E527E3CC1A719A996C093A7867C8FFA")
        }

        @Test
        fun `apply blake2b-512`() {
            assertThat(AvailableHashAlgorithms.Blake2b512Hasher.applyHash(test).print)
                .isEqualTo("9268B2FCE73C20D8E4CEFF7BB401968D77B0AA044F63025885E509714EA930F283E57D91919960B1EB573ED60EC303E15236469045F9570A44DB93A2AAE62405")
        }
    }


    @Nested
    inner class SHA3 {
        @Test
        fun `apply sha3-256`() {
            assertThat(AvailableHashAlgorithms.SHA3256Hasher.applyHash(test).print)
                .isEqualTo("8F42B68F1B239CD0F9F51EABFD43DAE5775CB1531E1CC64444115D1582518283")
        }

        @Test
        fun `apply sha3-512`() {
            assertThat(AvailableHashAlgorithms.SHA3512Hasher.applyHash(test).print)
                .isEqualTo("19B24FE949322476C9131D16F55D240B3B3CA288A8E0F379250101C3698ED06C8AC134DD4A80576051159DD3641B1421491223934C1F4E54D297E8FFA147DFE2")
        }
    }

    @Nested
    inner class Keccak {
        /**
         * BEWARE: Not entirely verified externally to be matching some other reference implementation.
         */
        @Test
        fun `apply keccak-256`() {
            assertThat(AvailableHashAlgorithms.Keccak256Hasher.applyHash(test).print)
                .isEqualTo("89AE94189AAB48578D1610872CDE9C5F647BF29494B54C5B7D4C7655627E34B7")
        }

        /**
         * BEWARE: Not entirely verified externally to be matching some other reference implementation.
         */
        @Test
        fun `apply keccak-512`() {
            assertThat(AvailableHashAlgorithms.Keccak512Hasher.applyHash(test).print)
                .isEqualTo("A473EA3FAF6BC58B04E41346CC729BFF2B1C8CCCF84CC183F97CB5AFF796EB49C071655D28B1C36A4A6819EB55CDCFBD920977717EEFB09BD2B34E6DFCE51138")
        }
    }
}