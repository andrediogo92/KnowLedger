package org.knowledger.ledger.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.knowledger.common.misc.getStringFromKey
import org.knowledger.common.misc.hexString
import org.knowledger.common.misc.stringToPrivateKey
import org.knowledger.common.misc.stringToPublicKey
import org.knowledger.ledger.service.Identity
import org.tinylog.kotlin.Logger
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
        val encpr = pr.getStringFromKey()
        val encpub = pub.getStringFromKey()
        val decpr = encpr.stringToPrivateKey()
        val decpub = encpub.stringToPublicKey()
        //Decode of encode matches decode
        assertThat(
            encpub.stringToPublicKey()
        ).isEqualTo(decpub)
        assertThat(
            encpr.stringToPrivateKey()
        ).isEqualTo(decpr)
        //Re-encode matches original encode.
        assertThat(
            decpub.getStringFromKey()
        ).isEqualTo(encpub)
        assertThat(
            decpr.getStringFromKey()
        ).isEqualTo(encpr)
        //Encode matches Re-encode
        assertThat(
            decpub.getStringFromKey()
        ).isEqualTo(
            pub.getStringFromKey()
        )
        assertThat(
            decpr.getStringFromKey()
        ).isEqualTo(
            pr.getStringFromKey()
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
