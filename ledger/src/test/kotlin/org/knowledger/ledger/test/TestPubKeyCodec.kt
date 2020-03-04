package org.knowledger.ledger.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.knowledger.base64.base64Decoded
import org.knowledger.base64.base64Encoded
import org.knowledger.ledger.core.base.hash.toHexString
import org.knowledger.ledger.crypto.EncodedPrivateKey
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.crypto.toPrivateKey
import org.knowledger.ledger.crypto.toPublicKey
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
        assertThat(decpub).isEqualTo(pub)
        assertThat(decpr).isEqualTo(pr)

        Logger.debug {
            """
                |Key:
                |  private: $pr
                |  public: $pub
                |Hex:
                |  private: ${pr.encoded.toHexString()}
                |  public: ${pub.encoded.toHexString()}
                |Base64:
                |  private: $encpr
                |  public: $encpub
                |Hex to Base64 to Hex:
                |  private: ${decpr.encoded.toHexString()}
                |  public: ${decpub.encoded.toHexString()}
                |Hex to Base64 to Key:
                |  private: $decpr
                |  public: $decpub
            """.trimMargin()
        }
    }
}
