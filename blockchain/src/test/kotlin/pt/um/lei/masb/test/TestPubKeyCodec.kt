package pt.um.lei.masb.test

import assertk.assert
import assertk.assertions.isEqualTo
import mu.KLogging
import org.junit.jupiter.api.Test
import pt.um.lei.masb.blockchain.Ident
import pt.um.lei.masb.blockchain.print
import pt.um.lei.masb.blockchain.utils.getStringFromKey
import pt.um.lei.masb.blockchain.utils.stringToPrivateKey
import pt.um.lei.masb.blockchain.utils.stringToPublicKey
import java.security.Key


/**
 * Tests for encoding/decoding [Key]s.
 */
class TestPubKeyCodec {
    val pair = Ident.generateNewIdent()
    val pr = pair.first
    val pub = pair.second

    @Test
    fun `Test encode and decode of private and public keys`() {
        val encpr = getStringFromKey(pr)
        val encpub = getStringFromKey(pub)
        val decpr = stringToPrivateKey(encpr)
        val decpub = stringToPublicKey(encpub)
        //Decode of encode matches decode
        assert(
            stringToPublicKey(encpub)
        ).isEqualTo(decpub)
        assert(
            stringToPrivateKey(encpr)
        ).isEqualTo(decpr)
        //Re-encode matches original encode.
        assert(
            getStringFromKey(decpub)
        ).isEqualTo(encpub)
        assert(
            getStringFromKey(decpr)
        ).isEqualTo(encpr)
        //Encode matches Re-encode
        assert(
            getStringFromKey(decpub)
        ).isEqualTo(
            getStringFromKey(pub)
        )
        assert(
            getStringFromKey(decpr)
        ).isEqualTo(
            getStringFromKey(pr)
        )
        //Keys match.
        assert(decpub).isEqualTo(pub)
        assert(decpr).isEqualTo(pr)

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
