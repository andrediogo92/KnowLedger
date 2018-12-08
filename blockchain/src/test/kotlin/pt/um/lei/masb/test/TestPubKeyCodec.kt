package pt.um.lei.masb.test

import mu.KLogging
import org.junit.jupiter.api.Assertions.assertEquals
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
    companion object : KLogging()

    @Test
    fun testEncodeDecode() {
        val (pr, pub) = Ident.generateNewIdent()
        val encpr = getStringFromKey(pr)
        val encpub = getStringFromKey(pub)
        val decpr = stringToPrivateKey(encpr)
        val decpub = stringToPublicKey(encpub)
        //Decode of encode matches decode
        assertEquals(stringToPublicKey(encpub), decpub)
        assertEquals(stringToPrivateKey(encpr), decpr)
        //Re-encode matches original encode.
        assertEquals(getStringFromKey(decpub), encpub)
        assertEquals(getStringFromKey(decpr), encpr)
        //Encode matches Re-encode
        assertEquals(getStringFromKey(decpub), getStringFromKey(pub))
        assertEquals(getStringFromKey(decpr), getStringFromKey(pr))
        //Keys match.
        assertEquals(decpub, pub)
        assertEquals(decpr, pr)

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
}
