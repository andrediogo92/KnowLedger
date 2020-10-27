package org.knowledger.ledger.crypto

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.knowledger.encoding.base16.hexEncoded
import org.knowledger.encoding.base64.base64Decoded
import org.knowledger.encoding.base64.base64Encoded
import org.knowledger.ledger.crypto.service.Identity
import org.tinylog.kotlin.Logger
import java.security.Key


/**
 * Tests for encoding/decoding [Key]s.
 */
class TestPubKeyCodec {
    private val pair = Identity("test")
    private val pr = pair.privateKey
    private val pub = pair.publicKey

    @Test
    fun `Test encode and decode of private and public keys`() {
        val encpr = pr.base64Encoded()
        val encpub = pub.base64Encoded()
        val decpr = EncodedPrivateKey(encpr.base64Decoded()).toPrivateKey()
        val decpub = EncodedPublicKey(encpub.base64Decoded()).toPublicKey()
        //Decode of encode matches decode
        assertThat(
            EncodedPublicKey(encpub.base64Decoded()).toPublicKey()
        ).isEqualTo(decpub)
        assertThat(
            EncodedPrivateKey(encpr.base64Decoded()).toPrivateKey()
        ).isEqualTo(decpr)
        //Re-encode matches original encode.
        assertThat(
            decpub.base64Encoded()
        ).isEqualTo(encpub)
        assertThat(
            decpr.base64Encoded()
        ).isEqualTo(encpr)
        //Encode matches Re-encode
        assertThat(
            decpub.base64Encoded()
        ).isEqualTo(
            pub.base64Encoded()
        )
        assertThat(
            decpr.base64Encoded()
        ).isEqualTo(
            pr.base64Encoded()
        )
        //Keys match.
        assertThat(decpub).isEqualTo(pub.toPublicKey())
        assertThat(decpr).isEqualTo(pr.toPrivateKey())

        Logger.debug {
            """
                |Key:
                |  private: $pr
                |  public: $pub
                |Hex:
                |  private: ${pr.hexEncoded()}
                |  public: ${pub.hexEncoded()}
                |Base64:
                |  private: $encpr
                |  public: $encpub
                |Hex to Base64 to Hex:
                |  private: ${decpr.encoded.hexEncoded()}
                |  public: ${decpub.encoded.hexEncoded()}
                |Hex to Base64 to Key:
                |  private: $decpr
                |  public: $decpub
            """.trimMargin()
        }
    }
}
