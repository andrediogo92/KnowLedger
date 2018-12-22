package pt.um.lei.masb.test

import assertk.assert
import assertk.assertions.isEqualTo
import mu.KLogging
import org.junit.jupiter.api.Test
import pt.um.lei.masb.blockchain.print
import pt.um.lei.masb.blockchain.utils.SHA256Encrypter
import pt.um.lei.masb.blockchain.utils.base64encode

class TestCrypters {
    companion object : KLogging()

    @Test
    fun `Test SHA256 encryption and base64 encoding`() {
        val shaer = SHA256Encrypter()
        val check = shaer.applyHash(
            "thisissampletext"
        )
        val print = check.print()
        val base64 = base64encode(check)
        assert(
            print
        ).isEqualTo(
            "EBA03E5F71E9C15C63BC9114AE14ABB7475C73FDBB4A92FACA6877B0CE601F5B"
        )
        assert(
            base64
        ).isEqualTo(
            "66A+X3HpwVxjvJEUrhSrt0dcc/27SpL6ymh3sM5gH1s="
        )

        logger.debug {
            """
                | Input: "thisissampletext"
                | Hex: $print
                | Base64: $base64
            """.trimMargin()
        }
    }
}