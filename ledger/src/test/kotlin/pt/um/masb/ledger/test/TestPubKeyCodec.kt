package pt.um.masb.ledger.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.tinylog.kotlin.Logger
import pt.um.masb.common.misc.getStringFromKey
import pt.um.masb.common.misc.hexString
import pt.um.masb.common.misc.stringToPrivateKey
import pt.um.masb.common.misc.stringToPublicKey
import pt.um.masb.ledger.service.Identity
import java.security.Key


/**
 * Tests for encoding/decoding [Key]s.
 */
class TestPubKeyCodec {
    val pair = Identity("test")
    val pr = pair.privateKey
    val pub = pair.publicKey

    @Test
    fun `Test encode and decode of private and public keys`() {
        val encpr = getStringFromKey(pr)
        val encpub = getStringFromKey(pub)
        val decpr = stringToPrivateKey(encpr)
        val decpub = stringToPublicKey(encpub)
        //Decode of encode matches decode
        assertThat(
            stringToPublicKey(encpub)
        ).isEqualTo(decpub)
        assertThat(
            stringToPrivateKey(encpr)
        ).isEqualTo(decpr)
        //Re-encode matches original encode.
        assertThat(
            getStringFromKey(decpub)
        ).isEqualTo(encpub)
        assertThat(
            getStringFromKey(decpr)
        ).isEqualTo(encpr)
        //Encode matches Re-encode
        assertThat(
            getStringFromKey(decpub)
        ).isEqualTo(
            getStringFromKey(pub)
        )
        assertThat(
            getStringFromKey(decpr)
        ).isEqualTo(
            getStringFromKey(pr)
        )
        //Keys match.
        assertThat(decpub).isEqualTo(pub)
        assertThat(decpr).isEqualTo(pr)

        Logger.debug {
            """
                |Key:
                |  private: $pr
                |  public: $pub
                |Hex:
                |  private: ${pr.encoded.hexString}
                |  public: ${pub.encoded.hexString}
                |Base64:
                |  private: $encpr
                |  public: $encpub
                |Hex to Base64 to Hex:
                |  private: ${decpr.encoded.hexString}
                |  public: ${decpub.encoded.hexString}
                |Hex to Base64 to Key:
                |  private: $decpr
                |  public: $decpub
            """.trimMargin()
        }
    }
}
