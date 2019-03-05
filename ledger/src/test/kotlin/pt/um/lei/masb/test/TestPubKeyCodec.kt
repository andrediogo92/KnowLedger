package pt.um.lei.masb.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import mu.KLogging
import org.junit.jupiter.api.Test
import pt.um.lei.masb.blockchain.service.Ident
import pt.um.lei.masb.blockchain.utils.getStringFromKey
import pt.um.lei.masb.blockchain.utils.stringToPrivateKey
import pt.um.lei.masb.blockchain.utils.stringToPublicKey
import java.security.Key


/**
 * Tests for encoding/decoding [Key]s.
 */
class TestPubKeyCodec {
    val pair = Ident("test")
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

        logger.debug {
            """
                |Key:
                |  private: $pr
                |  public: $pub
                |Hex:
                |  private: ${pr.encoded.print()}
                |  public: ${pub.encoded.print()}
                |Base64:
                |  private: $encpr
                |  public: $encpub
                |Hex to Base64 to Hex:
                |  private: ${decpr.encoded.print()}
                |  public: ${decpub.encoded.print()}
                |Hex to Base64 to Key:
                |  private: $decpr
                |  public: $decpub
            """.trimMargin()
        }
    }


    companion object : KLogging()

}
