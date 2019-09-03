package org.knowledger.ledger.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.knowledger.ledger.core.misc.base64Encoded
import org.knowledger.ledger.core.misc.toHexString
import org.knowledger.ledger.core.misc.toPrivateKey
import org.knowledger.ledger.core.misc.toPublicKey
import org.knowledger.ledger.crypto.service.Identity
import org.tinylog.kotlin.Logger
import java.security.Key


/**
 * Tests for encoding/decoding [Key]s.
 */
class
TestPubKeyCodec {
    val pair = Identity("test")
    val pr = pair.privateKey
    val pub = pair.publicKey

    @Test
    fun `Test encode and decode of private and public keys`() {
        val encpr = pr.base64Encoded()
        val encpub = pub.base64Encoded()
        val decpr = encpr.toPrivateKey()
        val decpub = encpub.toPublicKey()
        //Decode of encode matches decode
        assertThat(
            encpub.toPublicKey()
        ).isEqualTo(decpub)
        assertThat(
            encpr.toPrivateKey()
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
